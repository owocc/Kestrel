package com.bettershell.app.portforward

import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.nio.charset.StandardCharsets

/**
 * 单个服务器的 SSH 复用会话，专门服务于端口映射。
 *
 * 与用户交互式终端完全隔离：端口转发不需要 PTY，因此这里只建立一个裸 SSH
 * 会话，并挂载任意多条本地转发（同一服务器多条映射共享同一个 Session）。
 *
 * 所有方法均为阻塞式，调用方必须在 IO 线程执行。
 */
internal class SshTunnelSession(private val server: ServerConfig) {

    private val jsch: JSch = JSch()
    private var session: Session? = null

    val isConnected: Boolean
        get() = session?.isConnected == true

    /**
     * 建立并认证 SSH 会话，认证方式与交互式终端保持一致
     * （密码 / 私钥 + 可选 Passphrase）。
     */
    fun connect() {
        if (session?.isConnected == true) {
            return
        }

        if (server.authType == AuthType.PRIVATE_KEY && server.privateKey.isNotBlank()) {
            val keyBytes = server.privateKey.toByteArray(StandardCharsets.UTF_8)
            val passBytes = if (server.passphrase.isNotBlank()) {
                server.passphrase.toByteArray(StandardCharsets.UTF_8)
            } else {
                null
            }
            jsch.addIdentity("port-forward-key", keyBytes, null, passBytes)
        }

        val newSession = jsch.getSession(server.username, server.host, server.port)
        if (server.authType == AuthType.PASSWORD && server.password.isNotBlank()) {
            newSession.setPassword(server.password)
        }

        val config = java.util.Properties()
        config["StrictHostKeyChecking"] = "no"
        config["PreferredAuthentications"] =
            if (server.authType == AuthType.PASSWORD) "password,keyboard-interactive" else "publickey,password"
        newSession.setConfig(config)
        newSession.timeout = 15000

        newSession.connect()
        session = newSession
    }

    /**
     * 添加一条本地转发：手机 127.0.0.1:localPort -> 服务器 remoteHost:remotePort。
     *
     * 本地监听固定绑定在回环地址，不对外暴露；
     * @param localPort 传 0 时由系统自动分配，返回值为实际生效端口。
     */
    fun addLocalForward(remotePort: Int, localPort: Int): Int {
        val active = session ?: throw IllegalStateException("SSH 会话尚未建立")
        // 目标主机固定为服务器自身的回环地址：把"这台服务器上的服务"映射到手机本地
        return active.setPortForwardingL(LOOPBACK, localPort, LOOPBACK, remotePort)
    }

    /** 移除指定本地端口的转发规则（幂等，失败不抛出）。 */
    fun removeLocalForward(localPort: Int) {
        try {
            session?.delPortForwardingL(LOOPBACK, localPort)
        } catch (_: Exception) {
            // 会话已断开或规则不存在时忽略，后续 disconnect 会整体回收
        }
    }

    /** 断开会话并释放所有本地监听端口。 */
    fun disconnect() {
        try {
            session?.disconnect()
        } catch (_: Exception) {
            // 忽略断开过程中的异常
        } finally {
            session = null
        }
    }

    private companion object {
        const val LOOPBACK = "127.0.0.1"
    }
}
