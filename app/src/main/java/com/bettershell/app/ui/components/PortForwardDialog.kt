package com.bettershell.app.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.portforward.PortForwardManager
import com.bettershell.app.portforward.PortForwardTunnel
import com.bettershell.app.portforward.PortForwardValidation
import com.bettershell.app.portforward.TunnelStatus
import com.bettershell.app.ui.screens.textFieldColors
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.isAppInDarkTheme

private val StatusGreen = AccentGreen
private val StatusAmber = Color(0xFFF59E0B)
private val StatusRed = Color(0xFFEF4444)

/**
 * 「端口映射」对话框：输入远程端口与本地端口，用当前服务器的凭据建立 SSH 本地转发。
 *
 * 对话框只负责输入与状态展示；隧道本身由 [PortForwardManager] 与前台服务持有，
 * 因此关闭窗口不会中断映射。
 */
@Composable
fun PortForwardDialog(
    server: ServerConfig,
    manager: PortForwardManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme
    val allTunnels by manager.tunnels.collectAsState()
    val serverTunnels = remember(allTunnels, server.id) {
        allTunnels.filter { it.serverId == server.id }
    }
    val connecting = serverTunnels.any { it.status == TunnelStatus.CONNECTING }

    var remotePortText by remember { mutableStateOf("") }
    var localPortText by remember { mutableStateOf("") }
    var fieldError by remember { mutableStateOf<String?>(null) }
    var notificationDenied by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationDenied = !granted
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** 校验输入 -> 冲突检测 -> 交给管理器建立隧道 */
    fun submitMapping() {
        fieldError = null

        val remoteResult = PortForwardValidation.parseRemotePort(remotePortText)
        val remoteError = remoteResult.exceptionOrNull()?.message
        if (remoteError != null) {
            fieldError = remoteError
            return
        }
        val localResult = PortForwardValidation.parseLocalPort(localPortText)
        val localError = localResult.exceptionOrNull()?.message
        if (localError != null) {
            fieldError = localError
            return
        }

        val remotePort = remoteResult.getOrThrow()
        val localPort = localResult.getOrNull()
        val conflict = PortForwardValidation.findConflict(
            tunnels = manager.tunnels.value,
            serverId = server.id,
            remotePort = remotePort,
            localPort = localPort
        )
        if (conflict != null) {
            fieldError = conflict
            return
        }

        requestNotificationPermission()
        val result = manager.start(server, remotePort, localPort ?: 0)
        val startError = result.exceptionOrNull()?.message
        if (startError != null) {
            fieldError = startError
        } else {
            remotePortText = ""
            localPortText = ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 560.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ==================== 标题 ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Column {
                            Text(
                                text = "端口映射",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = server.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "把服务器 127.0.0.1 上的服务映射到手机本地 127.0.0.1 端口，关闭本窗口不会中断映射。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ==================== 端口输入 ====================
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = remotePortText,
                        onValueChange = { input ->
                            remotePortText = input.filter { it.isDigit() }.take(5)
                            fieldError = null
                        },
                        label = { Text("远程端口") },
                        placeholder = { Text("如 8000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = localPortText,
                        onValueChange = { input ->
                            localPortText = input.filter { it.isDigit() }.take(5)
                            fieldError = null
                        },
                        label = { Text("本地端口") },
                        placeholder = { Text("留空自动分配") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                fieldError?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = StatusRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (notificationDenied) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "未授予通知权限：隧道仍会运行，但后台保活通知不可见。",
                        color = StatusAmber,
                        fontSize = 12.sp
                    )
                }

                // ==================== 当前映射列表 ====================
                if (serverTunnels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "当前映射",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    serverTunnels.forEach { tunnel ->
                        TunnelRow(
                            tunnel = tunnel,
                            isDark = isDark,
                            onCopy = { copyAddress(context, tunnel.accessAddress) },
                            onStop = { manager.stop(tunnel.id) },
                            onRetry = {
                                fieldError = null
                                requestNotificationPermission()
                                manager.start(server, tunnel.remotePort, tunnel.requestedLocalPort)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ==================== 底部操作 ====================
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("关闭")
                    }
                    Button(
                        onClick = { submitMapping() },
                        enabled = !connecting,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (connecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("连接中…")
                        } else {
                            Text("启动映射")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TunnelRow(
    tunnel: PortForwardTunnel,
    isDark: Boolean,
    onCopy: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit
) {
    val rowBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val titleColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val statusColor = when (tunnel.status) {
        TunnelStatus.ACTIVE -> StatusGreen
        TunnelStatus.CONNECTING -> StatusAmber
        TunnelStatus.ERROR -> StatusRed
        TunnelStatus.STOPPED -> subtitleColor
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = rowBg
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.size(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val localText = if (tunnel.localPort > 0) {
                        "127.0.0.1:${tunnel.localPort}"
                    } else {
                        "本地端口分配中"
                    }
                    Text(
                        text = "$localText → 服务器:${tunnel.remotePort}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                    Text(
                        text = when (tunnel.status) {
                            TunnelStatus.ACTIVE -> "已激活，可在手机浏览器中访问"
                            TunnelStatus.CONNECTING -> "正在建立 SSH 隧道…"
                            TunnelStatus.ERROR -> tunnel.errorMessage ?: "启动失败"
                            TunnelStatus.STOPPED -> "已停止"
                        },
                        fontSize = 12.sp,
                        color = if (tunnel.status == TunnelStatus.ERROR) StatusRed else subtitleColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tunnel.status == TunnelStatus.ACTIVE) {
                    TextButton(onClick = onCopy) { Text("复制地址") }
                }
                if (tunnel.status == TunnelStatus.ERROR) {
                    TextButton(onClick = onRetry) {
                        Text("重试", color = StatusGreen)
                    }
                }
                TextButton(onClick = onStop) {
                    Text(
                        text = if (tunnel.status == TunnelStatus.ERROR) "移除" else "停止",
                        color = StatusRed
                    )
                }
            }
        }
    }
}

private fun copyAddress(context: Context, address: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("端口映射地址", address))
    Toast.makeText(context, "已复制 $address", Toast.LENGTH_SHORT).show()
}
