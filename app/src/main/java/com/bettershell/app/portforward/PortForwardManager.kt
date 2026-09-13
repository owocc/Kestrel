package com.bettershell.app.portforward

import android.content.Context
import com.bettershell.app.data.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * SSH 端口映射总控（进程内单例）。
 *
 * 职责：
 * 1. 维护当前所有隧道的状态流，供 UI 与前台服务共同订阅；
 * 2. 按服务器复用 SSH 会话，把本地转发规则挂载到对应会话上；
 * 3. 后台健康检查，会话掉线时把隧道标记为 ERROR 并回收本地端口；
 * 4. 在隧道存活期间维持前台服务（常驻通知），全部结束后释放。
 */
class PortForwardManager private constructor(private val appContext: Context) {

    companion object {
        @Volatile
        private var INSTANCE: PortForwardManager? = null

        fun getInstance(context: Context): PortForwardManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PortForwardManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val HEALTH_CHECK_INTERVAL_MS = 5000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tunnels = MutableStateFlow<List<PortForwardTunnel>>(emptyList())
    val tunnels: StateFlow<List<PortForwardTunnel>> = _tunnels.asStateFlow()

    /** serverId -> 可复用 SSH 会话（每个服务器一条，互不阻塞） */
    private val serverSessions = ConcurrentHashMap<String, ServerSession>()

    private var watchdog: Job? = null

    @Volatile
    private var foregroundServiceRequested = false

    private inner class ServerSession {
        val mutex = Mutex()
        var session: SshTunnelSession? = null
    }

    fun tunnelsForServer(serverId: String): List<PortForwardTunnel> {
        return _tunnels.value.filter { it.serverId == serverId }
    }

    fun activeTunnelCountForServer(serverId: String): Int {
        return _tunnels.value.count { it.serverId == serverId && it.isLive }
    }

    /**
     * 启动一条端口映射：手机 127.0.0.1:localPort -> 服务器 127.0.0.1:remotePort。
     *
     * @param localPort 0 表示自动分配本地端口
     * @return 成功时返回隧道 id；校验/冲突失败时返回 failure
     */
    fun start(server: ServerConfig, remotePort: Int, localPort: Int): Result<String> {
        val conflict = PortForwardValidation.findConflict(
            tunnels = _tunnels.value,
            serverId = server.id,
            remotePort = remotePort,
            localPort = localPort.takeIf { it > 0 }
        )
        if (conflict != null) {
            return Result.failure(IllegalStateException(conflict))
        }

        val tunnelId = UUID.randomUUID().toString()
        val tunnel = PortForwardTunnel(
            id = tunnelId,
            serverId = server.id,
            serverName = server.name,
            remotePort = remotePort,
            requestedLocalPort = localPort,
            localPort = localPort,
            status = TunnelStatus.CONNECTING
        )

        // 启动新映射时顺手清掉该服务器此前的失败记录，避免列表越积越长
        _tunnels.update { list ->
            list.filterNot { it.serverId == server.id && it.status == TunnelStatus.ERROR } + tunnel
        }
        // 用户此刻必然处于前台，先占住前台服务，保证连接与后台存活
        startForegroundService()

        scope.launch {
            try {
                val activeSession = ensureSession(server)
                val actualLocalPort = activeSession.addLocalForward(remotePort, localPort)

                // 连接期间用户可能已点"停止"：此时立即回收刚建立的监听端口
                if (_tunnels.value.none { it.id == tunnelId }) {
                    activeSession.removeLocalForward(actualLocalPort)
                    releaseServerSessionIfIdle(server.id)
                    stopServiceIfIdle()
                    return@launch
                }

                _tunnels.update { list ->
                    list.map {
                        if (it.id == tunnelId) {
                            it.copy(
                                status = TunnelStatus.ACTIVE,
                                localPort = actualLocalPort,
                                errorMessage = null
                            )
                        } else {
                            it
                        }
                    }
                }
                ensureWatchdog()
            } catch (e: Exception) {
                _tunnels.update { list ->
                    list.map {
                        if (it.id == tunnelId) {
                            it.copy(
                                status = TunnelStatus.ERROR,
                                errorMessage = PortForwardValidation.describeError(e)
                            )
                        } else {
                            it
                        }
                    }
                }
                releaseServerSessionIfIdle(server.id)
                stopServiceIfIdle()
            }
        }
        return Result.success(tunnelId)
    }

    /** 停止并移除一条隧道（对 ERROR 记录则为"移除"）。 */
    fun stop(tunnelId: String) {
        val tunnel = _tunnels.value.find { it.id == tunnelId } ?: return
        _tunnels.update { list -> list.filterNot { it.id == tunnelId } }
        scope.launch {
            if (tunnel.localPort > 0) {
                serverSessions[tunnel.serverId]?.session?.removeLocalForward(tunnel.localPort)
            }
            releaseServerSessionIfIdle(tunnel.serverId)
            stopServiceIfIdle()
        }
    }

    fun stopAllForServer(serverId: String) {
        val targets = _tunnels.value.filter { it.serverId == serverId }
        if (targets.isEmpty()) return
        _tunnels.update { list -> list.filterNot { it.serverId == serverId } }
        scope.launch {
            targets.forEach { tunnel ->
                if (tunnel.localPort > 0) {
                    serverSessions[serverId]?.session?.removeLocalForward(tunnel.localPort)
                }
            }
            releaseServerSessionIfIdle(serverId)
            stopServiceIfIdle()
        }
    }

    fun stopAll() {
        _tunnels.value = emptyList()
        scope.launch {
            serverSessions.keys.toList().forEach { serverId ->
                releaseServerSession(serverId)
            }
            stopServiceIfIdle()
        }
    }

    /** 由前台服务在销毁时回调，重新同步"是否已请求前台服务"的标记。 */
    fun onForegroundServiceStopped() {
        foregroundServiceRequested = false
    }

    // ==================== 内部实现 ====================

    private suspend fun ensureSession(server: ServerConfig): SshTunnelSession {
        val holder = serverSessions.getOrPut(server.id) { ServerSession() }
        return holder.mutex.withLock {
            val existing = holder.session
            if (existing != null && existing.isConnected) {
                existing
            } else {
                existing?.disconnect()
                val fresh = SshTunnelSession(server)
                fresh.connect()
                holder.session = fresh
                fresh
            }
        }
    }

    private fun releaseServerSession(serverId: String) {
        serverSessions.remove(serverId)?.session?.disconnect()
    }

    private fun releaseServerSessionIfIdle(serverId: String) {
        val stillLive = _tunnels.value.any { it.serverId == serverId && it.isLive }
        if (!stillLive) {
            releaseServerSession(serverId)
        }
    }

    /**
     * 健康检查：会话掉线时回收该服务器的全部隧道，避免本地端口被"僵尸监听"占用。
     */
    private fun ensureWatchdog() {
        if (watchdog?.isActive == true) return
        watchdog = scope.launch {
            try {
                while (isActive) {
                    delay(HEALTH_CHECK_INTERVAL_MS)
                    val brokenServerIds = serverSessions
                        .filterValues { holder -> holder.session?.isConnected == false }
                        .keys
                        .toList()

                    brokenServerIds.forEach { serverId ->
                        releaseServerSession(serverId)
                        _tunnels.update { list ->
                            list.map {
                                if (it.serverId == serverId && it.isLive) {
                                    it.copy(
                                        status = TunnelStatus.ERROR,
                                        errorMessage = "SSH 会话已断开，请重新启动映射"
                                    )
                                } else {
                                    it
                                }
                            }
                        }
                    }

                    if (_tunnels.value.none { it.isLive }) break
                }
            } finally {
                watchdog = null
            }
        }
    }

    private fun startForegroundService() {
        if (foregroundServiceRequested) return
        try {
            PortForwardService.start(appContext)
            foregroundServiceRequested = true
        } catch (e: Exception) {
            // Android 12+ 在后台可能禁止启动前台服务；此时隧道依旧可用，只是不保证后台存活
            e.printStackTrace()
        }
    }

    private fun stopServiceIfIdle() {
        if (_tunnels.value.any { it.isLive }) return
        try {
            PortForwardService.stop(appContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        foregroundServiceRequested = false
    }
}
