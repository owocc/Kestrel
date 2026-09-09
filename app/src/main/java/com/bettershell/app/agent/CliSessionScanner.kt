package com.bettershell.app.agent

import com.bettershell.app.data.ServerConfig
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

@Serializable
data class DiscoveredCliSession(
    val cli: String,
    val sessionId: String,
    val title: String,
    val cwd: String = "~",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 远程 CLI 历史会话探测与导入器 (对齐 Waku / Egoist 的 /resume 架构)
 * 能够远程扫描 ~/.claude, ~/.codex, ~/.omp, ~/.opencode 等目录中现存的 CLI 历史会话，
 * 并将其导入至 BetterShell 多会话列表中继续执行
 */
object CliSessionScanner {

    private val json = Json { ignoreUnknownKeys = true }

    private val PROBE_CLI_SESSIONS_SCRIPT = """
        python3 -c '
import os, json, glob

sessions = []
home = os.path.expanduser("~")

# 1. Claude Code 会话历史探测
claude_dir = os.path.join(home, ".claude")
if os.path.exists(claude_dir):
    patterns = [
        os.path.join(claude_dir, "*.jsonl"),
        os.path.join(claude_dir, "sessions", "*.json*"),
        os.path.join(claude_dir, "projects", "**", "*.json*")
    ]
    for pattern in patterns:
        for f in glob.glob(pattern, recursive=True)[:15]:
            try:
                base = os.path.basename(f)
                sid = base.replace(".jsonl", "").replace(".json", "")
                mtime = int(os.path.getmtime(f) * 1000)
                sessions.append({
                    "cli": "claude",
                    "sessionId": sid,
                    "title": "Claude: " + (sid[:18] if len(sid) > 18 else sid),
                    "cwd": "~",
                    "updatedAt": mtime
                })
            except: pass

# 2. Codex CLI 会话历史探测
codex_dir = os.path.join(home, ".codex")
if os.path.exists(codex_dir):
    patterns = [
        os.path.join(codex_dir, "sessions", "*.json*"),
        os.path.join(codex_dir, "*.jsonl")
    ]
    for pattern in patterns:
        for f in glob.glob(pattern, recursive=True)[:15]:
            try:
                base = os.path.basename(f)
                sid = base.replace(".jsonl", "").replace(".json", "")
                mtime = int(os.path.getmtime(f) * 1000)
                sessions.append({
                    "cli": "codex",
                    "sessionId": sid,
                    "title": "Codex: " + (sid[:18] if len(sid) > 18 else sid),
                    "cwd": "~",
                    "updatedAt": mtime
                })
            except: pass

# 3. OMP (Oh My Pi) 会话历史探测
omp_dir = os.path.join(home, ".omp", "sessions")
if os.path.exists(omp_dir):
    for f in os.listdir(omp_dir)[:15]:
        try:
            full_p = os.path.join(omp_dir, f)
            sid = f.replace(".json", "")
            mtime = int(os.path.getmtime(full_p) * 1000)
            sessions.append({
                "cli": "omp",
                "sessionId": sid,
                "title": "OMP: " + (sid[:18] if len(sid) > 18 else sid),
                "cwd": "~",
                "updatedAt": mtime
            })
        except: pass

# 4. OpenCode 会话历史探测
opencode_dir = os.path.join(home, ".opencode", "sessions")
if os.path.exists(opencode_dir):
    for f in os.listdir(opencode_dir)[:15]:
        try:
            full_p = os.path.join(opencode_dir, f)
            sid = f.replace(".json", "")
            mtime = int(os.path.getmtime(full_p) * 1000)
            sessions.append({
                "cli": "opencode",
                "sessionId": sid,
                "title": "OpenCode: " + (sid[:18] if len(sid) > 18 else sid),
                "cwd": "~",
                "updatedAt": mtime
            })
        except: pass

# 按最后修改时间降序排序
sessions.sort(key=lambda x: x.get("updatedAt", 0), reverse=True)
print("BETTERSHELL_CLI_SESSIONS:" + json.dumps(sessions[:30]))
' 2>/dev/null
    """.trimIndent()

    suspend fun scanRemoteSessions(server: ServerConfig): List<DiscoveredCliSession> = withContext(Dispatchers.IO) {
        if (server.isMock) {
            return@withContext listOf(
                DiscoveredCliSession(
                    cli = "claude",
                    sessionId = "claude-session-20260908-01",
                    title = "Claude: 重构后端 API 路由",
                    cwd = "~/projects/better-shell",
                    updatedAt = System.currentTimeMillis() - 3600000
                ),
                DiscoveredCliSession(
                    cli = "omp",
                    sessionId = "omp-session-refactor-worker",
                    title = "OMP: 会话持久化与上下文处理",
                    cwd = "~/workspace/multica-core",
                    updatedAt = System.currentTimeMillis() - 7200000
                ),
                DiscoveredCliSession(
                    cli = "codex",
                    sessionId = "codex-rollout-9912",
                    title = "Codex: 单元测试用例生成",
                    cwd = "~/dev/agent-engine",
                    updatedAt = System.currentTimeMillis() - 86400000
                )
            )
        }

        var jschSession: Session? = null
        var channel: ChannelExec? = null
        try {
            val jsch = JSch()
            if (server.privateKey.isNotBlank()) {
                val passphraseBytes = if (server.passphrase.isNotBlank()) server.passphrase.toByteArray() else null
                jsch.addIdentity("cli_scan_key", server.privateKey.toByteArray(), null, passphraseBytes)
            }

            jschSession = jsch.getSession(server.username, server.host, server.port).apply {
                if (server.password.isNotBlank()) {
                    setPassword(server.password)
                }
                setConfig("StrictHostKeyChecking", "no")
                timeout = 10000
                connect(10000)
            }

            channel = (jschSession.openChannel("exec") as ChannelExec).apply {
                setCommand(PROBE_CLI_SESSIONS_SCRIPT)
            }

            val outputStream = ByteArrayOutputStream()
            channel.outputStream = outputStream
            channel.connect(10000)

            val startTime = System.currentTimeMillis()
            while (!channel.isClosed && System.currentTimeMillis() - startTime < 10000) {
                Thread.sleep(100)
            }

            val rawOutput = outputStream.toString(StandardCharsets.UTF_8.name())
            val marker = "BETTERSHELL_CLI_SESSIONS:"
            val index = rawOutput.indexOf(marker)
            if (index != -1) {
                val jsonPart = rawOutput.substring(index + marker.length).trim().lines().firstOrNull() ?: "[]"
                return@withContext json.decodeFromString<List<DiscoveredCliSession>>(jsonPart)
            }
            return@withContext emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        } finally {
            try {
                channel?.disconnect()
                jschSession?.disconnect()
            } catch (_: Exception) {}
        }
    }
}
