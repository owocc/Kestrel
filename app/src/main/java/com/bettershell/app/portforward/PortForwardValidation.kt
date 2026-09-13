package com.bettershell.app.portforward

import com.jcraft.jsch.JSchException
import java.net.BindException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 端口映射的纯逻辑校验与错误翻译。
 *
 * 这里刻意不依赖任何 Android / JSch 运行时状态，便于单元测试覆盖：
 * UI 只负责把用户输入与当前隧道列表交给这些函数，并展示它们的中文结果。
 */
object PortForwardValidation {

    /** 非 root 应用无法绑定特权端口，因此本地端口下限定为 1024 */
    const val MIN_LOCAL_PORT = 1024
    const val MAX_PORT = 65535

    /**
     * 解析"远程端口"（服务器上被转发的服务端口），必填。
     */
    fun parseRemotePort(raw: String): Result<Int> {
        val text = raw.trim()
        if (text.isEmpty()) {
            return Result.failure(IllegalArgumentException("请输入远程端口"))
        }
        val port = text.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("远程端口必须是数字"))
        if (port !in 1..MAX_PORT) {
            return Result.failure(IllegalArgumentException("远程端口需在 1 - $MAX_PORT 之间"))
        }
        return Result.success(port)
    }

    /**
     * 解析"本地端口"（手机上监听的端口），留空表示自动分配。
     */
    fun parseLocalPort(raw: String): Result<Int?> {
        val text = raw.trim()
        if (text.isEmpty()) {
            return Result.success(null)
        }
        val port = text.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("本地端口必须是数字"))
        if (port !in MIN_LOCAL_PORT..MAX_PORT) {
            return Result.failure(
                IllegalArgumentException("本地端口需在 $MIN_LOCAL_PORT - $MAX_PORT 之间（留空可自动分配）")
            )
        }
        return Result.success(port)
    }

    /**
     * 检测冲突：同服务器同远程端口重复映射，或本地端口已被其它活动隧道占用。
     *
     * @return 冲突说明；无冲突返回 null
     */
    fun findConflict(
        tunnels: List<PortForwardTunnel>,
        serverId: String,
        remotePort: Int,
        localPort: Int?
    ): String? {
        val live = tunnels.filter { it.isLive }
        live.firstOrNull { it.serverId == serverId && it.remotePort == remotePort }?.let { exist ->
            return "该远程端口已映射到 127.0.0.1:${exist.localPort}"
        }
        if (localPort != null) {
            live.firstOrNull { it.localPort == localPort }?.let { exist ->
                return "本地端口 $localPort 已被「${exist.serverName}」的映射占用"
            }
        }
        return null
    }

    /**
     * 把连接阶段的异常翻译成用户可读的中文提示。
     */
    fun describeError(error: Throwable): String {
        val raw = (error.message ?: "").lowercase()
        return when {
            error is BindException || raw.contains("address already in use") || raw.contains("bind") ->
                "本地端口已被占用，请换一个本地端口"

            error is UnknownHostException ->
                "无法解析主机地址，请检查服务器配置"

            error is SocketTimeoutException || raw.contains("timeout") || raw.contains("timed out") ->
                "连接超时，请检查网络与主机地址"

            error is ConnectException ->
                "无法连接服务器，请检查网络与主机地址"

            raw.contains("auth fail") || raw.contains("auth cancel") || raw.contains("userauth") ->
                "认证失败，请检查密码或私钥"

            raw.contains("forwarding") || raw.contains("administratively prohibited") ->
                "服务器拒绝端口转发（请检查 sshd 的 AllowTcpForwarding 配置）"

            error is JSchException && raw.contains("session is down") ->
                "SSH 会话已断开，请重新启动映射"

            error is SocketException ->
                "网络连接异常，请稍后重试"

            error.message.isNullOrBlank() -> "端口映射启动失败"
            else -> error.message ?: "端口映射启动失败"
        }
    }
}
