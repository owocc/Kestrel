package com.bettershell.app.portforward

import java.util.UUID

/**
 * 端口映射隧道状态机：
 * CONNECTING -> ACTIVE -> STOPPED
 *            \-> ERROR （认证失败/端口占用/网络中断等）
 */
enum class TunnelStatus {
    CONNECTING,
    ACTIVE,
    ERROR,
    STOPPED
}

/**
 * 一条 SSH 本地端口转发隧道（ssh -L 的等价物）。
 *
 * @param requestedLocalPort 用户请求的本地端口，0 表示让系统自动分配
 * @param localPort 实际生效的本地监听端口（自动分配后由 JSch 返回真实值）
 */
data class PortForwardTunnel(
    val id: String = UUID.randomUUID().toString(),
    val serverId: String,
    val serverName: String,
    val remoteHost: String = "127.0.0.1",
    val remotePort: Int,
    val requestedLocalPort: Int,
    val localPort: Int,
    val status: TunnelStatus = TunnelStatus.CONNECTING,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 手机侧访问地址：本地监听固定绑定在回环地址上 */
    val accessAddress: String get() = "127.0.0.1:$localPort"

    /** 该隧道是否处于"占用连接资源"的生命周期内 */
    val isLive: Boolean get() = status == TunnelStatus.CONNECTING || status == TunnelStatus.ACTIVE
}
