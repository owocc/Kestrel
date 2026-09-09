package com.bettershell.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.terminal.ConnectionState
import com.bettershell.app.terminal.ServerSessionItem
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.isAppInDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Terminal (交互终端) 多会话管理与切换抽屉
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalSessionSwitcherBottomSheet(
    server: ServerConfig,
    sessions: List<ServerSessionItem>,
    activeSessionId: String,
    onSelectSession: (String) -> Unit,
    onCreateSession: () -> Unit,
    onRenameSession: (ServerSessionItem, String) -> Unit,
    onCloseSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme
    var sessionToRename by remember { mutableStateOf<ServerSessionItem?>(null) }
    var renameText by remember { mutableStateOf("") }
    var sessionToCloseId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Terminal,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "终端会话 (${sessions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${server.username}@${server.host}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            onCreateSession()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("新建", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 终端会话列表
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sessions.forEach { session ->
                    val isActive = session.id == activeSessionId
                    val cardBg = if (isActive) {
                        AccentGreen.copy(alpha = 0.12f)
                    } else {
                        if (isDark) Color(0xFF1E1E1E) else Color(0xFFF9FAFB)
                    }
                    val border = if (isActive) {
                        BorderStroke(1.5.dp, AccentGreen)
                    } else {
                        BorderStroke(1.dp, if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB))
                    }

                    val isConnected = session.terminalSession.connectionState.value is ConnectionState.Connected

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSession(session.id)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        border = border
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // 状态小圆点
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isConnected) AccentGreen else Color.Gray)
                                    )
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isActive) AccentGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (server.persistentTerminalSession) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AccentGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "TMUX",
                                                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = AccentGreen,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (session.tmuxSessionName != null) "Tmux: ${session.tmuxSessionName}" else "标准 Shell 终端",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (isActive) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = "Active",
                                        tint = AccentGreen,
                                        modifier = Modifier.size(20.dp).padding(end = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        sessionToRename = session
                                        renameText = session.title
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                                if (sessions.size > 1) {
                                    IconButton(
                                        onClick = { sessionToCloseId = session.id },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Close", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 重命名弹窗
    if (sessionToRename != null) {
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("重命名终端会话") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("终端标题") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = renameText.trim()
                        if (clean.isNotBlank()) {
                            onRenameSession(sessionToRename!!, clean)
                        }
                        sessionToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text("确定", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { sessionToRename = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 关闭确认弹窗
    if (sessionToCloseId != null) {
        AlertDialog(
            onDismissRequest = { sessionToCloseId = null },
            title = { Text("关闭终端会话") },
            text = { Text("确定要关闭此终端会话吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onCloseSession(sessionToCloseId!!)
                        sessionToCloseId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("关闭", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { sessionToCloseId = null }) {
                    Text("取消")
                }
            }
        )
    }
}
