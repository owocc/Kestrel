package com.bettershell.app.portforward

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.bettershell.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 端口映射的前台服务：只负责"保活 + 常驻通知 + 一键停止"。
 *
 * 真正的 SSH 隧道由 [PortForwardManager] 持有；本服务订阅其状态流刷新通知，
 * 当隧道全部结束后自动退出，避免留下空通知。
 *
 * 采用 specialUse 类型（而非 dataSync），因为 Android 15+ 对 dataSync
 * 前台服务设有 6 小时运行上限，会强制掐断长时间存活的隧道。
 */
class PortForwardService : Service() {

    companion object {
        private const val CHANNEL_ID = "port_forward"
        private const val NOTIFICATION_ID = 4711
        private const val ACTION_START = "com.bettershell.app.portforward.action.START"
        private const val ACTION_STOP_ALL = "com.bettershell.app.portforward.action.STOP_ALL"

        fun start(context: Context) {
            val intent = Intent(context, PortForwardService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PortForwardService::class.java))
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var manager: PortForwardManager

    override fun onCreate() {
        super.onCreate()
        manager = PortForwardManager.getInstance(this)
        createNotificationChannel()

        scope.launch {
            manager.tunnels.collect { tunnels ->
                if (tunnels.none { it.isLive }) {
                    // 隧道已全部结束：撤下通知并退出服务
                    stopSelf()
                } else {
                    NotificationManagerCompat.from(this@PortForwardService)
                        .notify(NOTIFICATION_ID, buildNotification(tunnels))
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALL) {
            manager.stopAll()
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification(manager.tunnels.value)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        manager.onForegroundServiceStopped()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "端口映射",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "保持 SSH 端口映射隧道在后台运行"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun buildNotification(tunnels: List<PortForwardTunnel>): Notification {
        val live = tunnels.filter { it.isLive }
        val summary = when {
            live.isEmpty() -> "正在停止…"
            live.size == 1 -> describeTunnel(live.first())
            else -> "${live.size} 条隧道运行中"
        }
        val detail = live.joinToString("\n") { describeTunnel(it) }

        val contentIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
            PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val stopAllIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, PortForwardService::class.java).setAction(ACTION_STOP_ALL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle("Kestrel 端口映射")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail.ifBlank { summary }))
            .setContentIntent(contentIntent)
            .addAction(0, "全部停止", stopAllIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun describeTunnel(tunnel: PortForwardTunnel): String {
        val local = if (tunnel.localPort > 0) {
            "127.0.0.1:${tunnel.localPort}"
        } else {
            "本地端口分配中"
        }
        return "$local → ${tunnel.serverName}:${tunnel.remotePort}"
    }
}
