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
 * 后台单次 SSH 快速执行探针 (用于首页服务器管理中快速扫描 Agent)
 */
object SshAgentScanner {

    suspend fun scanServer(server: ServerConfig): String = withContext(Dispatchers.IO) {
        if (server.isMock) {
            return@withContext """BETTERSHELL_AGENT_PROBE_RESULT:[{"id":"omp","cmd":"omp","path":"/usr/local/bin/omp","version":"18.1.14"}]"""
        }

        var jschSession: Session? = null
        var channel: ChannelExec? = null
        try {
            val jsch = JSch()
            if (server.privateKey.isNotBlank()) {
                val passphraseBytes = if (server.passphrase.isNotBlank()) server.passphrase.toByteArray() else null
                jsch.addIdentity("server_key", server.privateKey.toByteArray(), null, passphraseBytes)
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
                setCommand(AgentProbeScript.BASH_PROBE_SCRIPT)
            }

            val outputStream = ByteArrayOutputStream()
            channel.outputStream = outputStream
            channel.connect(10000)

            // 等待执行完成 (超时上限 12s)
            val startTime = System.currentTimeMillis()
            while (!channel.isClosed && System.currentTimeMillis() - startTime < 12000) {
                Thread.sleep(100)
            }

            return@withContext outputStream.toString(StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            return@withContext "ERROR: ${e.message}"
        } finally {
            try {
                channel?.disconnect()
                jschSession?.disconnect()
            } catch (_: Exception) {}
        }
    }
}
