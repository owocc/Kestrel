package com.bettershell.app.agent

import com.bettershell.app.data.ServerConfig
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * 远程服务器文件检索服务 (支持 @ 文件引用候选列表)
 * 在当前 Work 会话的 cwd 路径下高效检索文件树并加入缓存
 */
object RemoteFileScanner {

    private val cache = mutableMapOf<String, List<String>>()

    suspend fun listFiles(server: ServerConfig, cwd: String): List<String> = withContext(Dispatchers.IO) {
        val resolvedCwd = if (cwd.isNotBlank() && cwd != "~") cwd else "."
        val cacheKey = "${server.id}_$resolvedCwd"

        if (cache.containsKey(cacheKey)) {
            return@withContext cache[cacheKey] ?: emptyList()
        }

        if (server.isMock) {
            val mockFiles = listOf(
                "README.md",
                "package.json",
                "Cargo.toml",
                "src/main.rs",
                "src/agent.rs",
                "src/index.ts",
                "src/components/App.tsx",
                "src/components/Header.tsx",
                "app/build.gradle.kts",
                "app/src/main/AndroidManifest.xml",
                "config/settings.json",
                "scripts/deploy.sh",
                ".gitignore",
                "server.py",
                "tests/test_basic.py"
            )
            cache[cacheKey] = mockFiles
            return@withContext mockFiles
        }

        var jschSession: Session? = null
        var channel: ChannelExec? = null
        try {
            val jsch = JSch()
            if (server.privateKey.isNotBlank()) {
                val passphraseBytes = if (server.passphrase.isNotBlank()) server.passphrase.toByteArray() else null
                jsch.addIdentity("file_scan_key", server.privateKey.toByteArray(), null, passphraseBytes)
            }

            jschSession = jsch.getSession(server.username, server.host, server.port).apply {
                if (server.password.isNotBlank()) {
                    setPassword(server.password)
                }
                setConfig("StrictHostKeyChecking", "no")
                timeout = 8000
                connect(8000)
            }

            val cmd = "cd \"$resolvedCwd\" 2>/dev/null && find . -maxdepth 3 -not -path '*/.*' -not -path '*/node_modules/*' -not -path '*/target/*' -not -path '*/build/*' | sed 's|^\\./||' | head -n 100"

            channel = (jschSession.openChannel("exec") as ChannelExec).apply {
                setCommand(cmd)
            }

            val outputStream = ByteArrayOutputStream()
            channel.outputStream = outputStream
            channel.connect(8000)

            val startTime = System.currentTimeMillis()
            while (!channel.isClosed && System.currentTimeMillis() - startTime < 6000) {
                Thread.sleep(80)
            }

            val lines = outputStream.toString(StandardCharsets.UTF_8.name())
                .lines()
                .map { it.trim() }
                .filter { it.isNotBlank() && it != "." }

            val result = if (lines.isNotEmpty()) lines else listOf("README.md")
            cache[cacheKey] = result
            return@withContext result
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext listOf("README.md")
        } finally {
            try {
                channel?.disconnect()
                jschSession?.disconnect()
            } catch (_: Exception) {}
        }
    }

    fun clearCache(serverId: String) {
        cache.keys.filter { it.startsWith(serverId) }.forEach { cache.remove(it) }
    }
}
