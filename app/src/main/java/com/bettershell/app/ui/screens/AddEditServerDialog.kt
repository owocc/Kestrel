package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.theme.AccentGreen
import java.util.UUID

@Composable
fun AddEditServerDialog(
    initialServer: ServerConfig? = null,
    onDismiss: () -> Unit,
    onSave: (ServerConfig) -> Unit
) {
    var name by remember { mutableStateOf(initialServer?.name ?: "") }
    var host by remember { mutableStateOf(initialServer?.host ?: "") }
    var portText by remember { mutableStateOf(initialServer?.port?.toString() ?: "22") }
    var username by remember { mutableStateOf(initialServer?.username ?: "root") }
    var authType by remember { mutableStateOf(initialServer?.authType ?: AuthType.PASSWORD) }
    var password by remember { mutableStateOf(initialServer?.password ?: "") }
    var privateKey by remember { mutableStateOf(initialServer?.privateKey ?: "") }
    var startupScript by remember { mutableStateOf(initialServer?.startupScript ?: "") }
    var showPassword by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialServer == null) "添加 SSH 服务器" else "编辑服务器设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Server Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("服务器名称") },
                    placeholder = { Text("例如: AI Agent 节点 / Cloud GPU") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Host & Port
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("主机地址 (IP / 域名)") },
                        placeholder = { Text("192.168.1.100") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it.filter { char -> char.isDigit() } },
                        label = { Text("端口") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(90.dp),
                        colors = textFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    placeholder = { Text("root / ubuntu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auth Type Selector
                Text(
                    text = "认证方式",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuthTypeChip(
                        selected = authType == AuthType.PASSWORD,
                        label = "密码认证",
                        icon = Icons.Rounded.Password,
                        onClick = { authType = AuthType.PASSWORD },
                        modifier = Modifier.weight(1f)
                    )
                    AuthTypeChip(
                        selected = authType == AuthType.PRIVATE_KEY,
                        label = "密钥认证",
                        icon = Icons.Rounded.Key,
                        onClick = { authType = AuthType.PRIVATE_KEY },
                        modifier = Modifier.weight(1f)
                    )
                    AuthTypeChip(
                        selected = authType == AuthType.DEMO_MOCK,
                        label = "本地模拟",
                        icon = Icons.Rounded.PlayCircle,
                        onClick = { authType = AuthType.DEMO_MOCK },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auth detail
                when (authType) {
                    AuthType.PASSWORD -> {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("SSH 密码") },
                            placeholder = { Text("输入服务器登录密码") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors()
                        )
                    }
                    AuthType.PRIVATE_KEY -> {
                        OutlinedTextField(
                            value = privateKey,
                            onValueChange = { privateKey = it },
                            label = { Text("私钥 (OpenSSH / RSA / Ed25519)") },
                            placeholder = { Text("-----BEGIN OPENSSH PRIVATE KEY-----\n...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            colors = textFieldColors()
                        )
                    }
                    AuthType.DEMO_MOCK -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 本地模拟模式：无需远程服务器，直接在手机上运行轻量级交互终端环境，随时体验 Agent 交互和展开式面板设计。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Startup Script Section (自定义sh代码)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = "Startup Script",
                        tint = AccentGreen,
                        modifier = Modifier.padding(2.dp)
                    )
                    Text(
                        text = "自定义启动脚本 (Startup Script)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "连接建立后自动在终端运行的 Shell 代码（如激活环境、启动 Agent、或 cd 到工作空间）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = startupScript,
                    onValueChange = { startupScript = it },
                    placeholder = {
                        Text(
                            "# 示例:\ncd ~/agent-workspace\nsource venv/bin/activate\npython3 -m agent --status",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val server = ServerConfig(
                                id = initialServer?.id ?: UUID.randomUUID().toString(),
                                name = name.ifBlank { if (host.isNotBlank()) host else "未命名服务器" },
                                host = host.ifBlank { "127.0.0.1" },
                                port = portText.toIntOrNull() ?: 22,
                                username = username.ifBlank { "root" },
                                authType = authType,
                                password = password,
                                privateKey = privateKey,
                                startupScript = startupScript,
                                isMock = authType == AuthType.DEMO_MOCK,
                                lastConnected = initialServer?.lastConnected ?: 0L
                            )
                            onSave(server)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("保存", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthTypeChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** 统一的输入框配色，供本模块内多个对话框（服务器编辑、端口映射）复用 */
@Composable
internal fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
