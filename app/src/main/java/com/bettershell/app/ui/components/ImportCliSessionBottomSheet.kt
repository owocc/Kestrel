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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.bettershell.app.agent.CliSessionScanner
import com.bettershell.app.agent.DiscoveredCliSession
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.AccentOrange
import com.bettershell.app.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

/**
 * 远程 CLI 会话恢复与导入抽屉 (对标 Waku /egoist 的 /resume 功能)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportCliSessionBottomSheet(
    server: ServerConfig,
    onImportSession: (cli: String, sessionId: String, title: String, cwd: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme
    val coroutineScope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(true) }
    var discoveredSessions by remember { mutableStateOf<List<DiscoveredCliSession>>(emptyList()) }

    var manualCliType by remember { mutableStateOf("claude") }
    var manualSessionId by remember { mutableStateOf("") }
    var manualCwd by remember { mutableStateOf("~") }

    fun refreshScan() {
        isScanning = true
        coroutineScope.launch {
            discoveredSessions = CliSessionScanner.scanRemoteSessions(server)
            isScanning = false
        }
    }

    LaunchedEffect(server.id) {
        refreshScan()
    }

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
                            .background(AccentOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Download,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "导入 / 恢复 CLI 会话",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "对标 Waku resume：无缝导入远端 Claude/Codex/OMP 历史会话",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { refreshScan() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 扫描状态展示
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = AccentOrange, modifier = Modifier.size(32.dp))
                        Text(
                            text = "正在检索远端 ~/.claude, ~/.codex, ~/.omp 历史会话...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (discoveredSessions.isNotEmpty()) {
                    Text(
                        text = "探测到的 CLI 历史会话 (${discoveredSessions.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        discoveredSessions.forEach { item ->
                            val cliColor = when (item.cli.lowercase()) {
                                "claude" -> Color(0xFFD97706)
                                "codex" -> Color(0xFF10B981)
                                "omp" -> Color(0xFF6366F1)
                                else -> Color(0xFF3B82F6)
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onImportSession(item.cli, item.sessionId, item.title, item.cwd)
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF9FAFB),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = cliColor.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = item.cli.uppercase(),
                                                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = cliColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Session ID: ${item.sessionId}  ·  路径: ${item.cwd}",
                                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onImportSession(item.cli, item.sessionId, item.title, item.cwd)
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("导入恢复", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                            Text(
                                text = "未扫描到现存的历史 CLI 会话文件",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "可在下方直接输入已有 Session ID 强制进行恢复挂载",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 手动输入 Session ID 恢复区域
            Text(
                text = "手动指定 Session ID 恢复",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            // CLI 选择单选胶囊
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("claude" to "Claude Code", "codex" to "Codex", "omp" to "OMP", "opencode" to "OpenCode").forEach { (type, label) ->
                    val isSelected = manualCliType == type
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { manualCliType = type },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentOrange.copy(alpha = 0.15f) else (if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)),
                        border = BorderStroke(1.dp, if (isSelected) AccentOrange else Color.Transparent)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = TextStyle(fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = manualSessionId,
                onValueChange = { manualSessionId = it },
                label = { Text("Session ID / Resume Token") },
                placeholder = { Text("例如：ses_019488bc_42a 或 uuid") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = manualCwd,
                onValueChange = { manualCwd = it },
                label = { Text("工作目录 (CWD)") },
                placeholder = { Text("例如：~/my-project") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val sid = manualSessionId.trim()
                    if (sid.isNotBlank()) {
                        val title = "$manualCliType: ${sid.take(12)}"
                        onImportSession(manualCliType, sid, title, manualCwd.trim().ifBlank { "~" })
                        onDismiss()
                    }
                },
                enabled = manualSessionId.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("导入并挂载该会话", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
