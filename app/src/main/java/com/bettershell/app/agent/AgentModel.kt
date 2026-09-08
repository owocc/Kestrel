package com.bettershell.app.agent

import kotlinx.serialization.Serializable

@Serializable
enum class AgentType(val id: String, val displayName: String, val binaryName: String) {
    OMP("omp", "Oh My Pi (omp)", "omp"),
    CLAUDE("claude", "Claude Code", "claude"),
    CODEX("codex", "Codex", "codex"),
    OPENCODE("opencode", "OpenCode", "opencode"),
    PI("pi", "Pi Agent", "pi"),
    CURSOR("cursor", "Cursor Agent", "cursor-agent"),
    KIMI("kimi", "Kimi CLI", "kimi"),
    CUSTOM("custom", "Custom Agent", "");

    companion object {
        fun fromBinary(binary: String): AgentType {
            return entries.firstOrNull { it.binaryName.equals(binary, ignoreCase = true) } ?: CUSTOM
        }
    }
}

@Serializable
enum class ThinkingLevel(val value: String, val displayName: String) {
    AUTO("auto", "自动 (Auto)"),
    OFF("off", "关闭 (Off)"),
    LOW("low", "轻度 (Low)"),
    MEDIUM("medium", "标准 (Medium)"),
    HIGH("high", "深入 (High)"),
    MAX("max", "极强 (Max)")
}

@Serializable
data class DiscoveredAgent(
    val id: String,
    val type: AgentType,
    val command: String,
    val path: String,
    val version: String = "",
    val isAvailable: Boolean = true,
    val models: List<String> = emptyList(),
    val selectedModel: String = "",
    val thinkingLevel: ThinkingLevel = ThinkingLevel.AUTO
)

/**
 * 远程服务器 Agent 自动探测探测脚本 (对标 Multica daemon probeAgentCLIs)
 * 执行一次脚本，输出结构化 JSON，涵盖所有可用 CLI 路径与版本信息
 */
object AgentProbeScript {
    val BASH_PROBE_SCRIPT = """
        python3 -c '
import json, shutil, subprocess, sys

agents = [
    {"id": "omp", "cmd": "omp"},
    {"id": "claude", "cmd": "claude"},
    {"id": "codex", "cmd": "codex"},
    {"id": "opencode", "cmd": "opencode"},
    {"id": "pi", "cmd": "pi"},
    {"id": "cursor", "cmd": "cursor-agent"},
    {"id": "kimi", "cmd": "kimi"}
]

found = []
for a in agents:
    path = shutil.which(a["cmd"])
    if path:
        version = ""
        try:
            res = subprocess.run([path, "--version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=2)
            version = res.stdout.strip() or res.stderr.strip()
            version = version.split("\n")[0][:40]
        except:
            pass
        found.append({
            "id": a["id"],
            "cmd": a["cmd"],
            "path": path,
            "version": version
        })

print("BETTERSHELL_AGENT_PROBE_RESULT:" + json.dumps(found))
' 2>/dev/null || (
    FOUND="["
    FIRST=1
    for cmd in omp claude codex opencode pi cursor-agent kimi; do
        PATH_BIN=${'$'}(which "${'$'}{cmd}" 2>/dev/null)
        if [ -n "${'$'}{PATH_BIN}" ]; then
            if [ ${'$'}{FIRST} -eq 0 ]; then FOUND="${'$'}{FOUND},"; fi
            FOUND="${'$'}{FOUND}{\"id\":\"${'$'}{cmd}\",\"cmd\":\"${'$'}{cmd}\",\"path\":\"${'$'}{PATH_BIN}\",\"version\":\"\"}"
            FIRST=0
        fi
    done
    FOUND="${'$'}{FOUND}]"
    echo "BETTERSHELL_AGENT_PROBE_RESULT:${'$'}{FOUND}"
)
    """.trimIndent()
}
