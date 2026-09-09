package com.bettershell.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 独立的服务器基础连接配置页面 (单独压入栈，带保存圆钮，支持预见式返回)
 */
@Composable
fun ServerBasicSettingsScreen(
    server: ServerConfig,
    onSaveServer: (ServerConfig) -> Unit,
    onBack: () -> Unit
) {
    val isDark = com.bettershell.app.ui.theme.isAppInDarkTheme

    var name by remember { mutableStateOf(server.name) }
    var description by remember { mutableStateOf(server.description) }
    var selectedIconKey by remember { mutableStateOf(server.icon.ifBlank { "terminal" }) }
    var showIconPicker by remember { mutableStateOf(false) }
    var host by remember { mutableStateOf(server.host) }
    var portText by remember { mutableStateOf(server.port.toString()) }
    var username by remember { mutableStateOf(server.username) }
    var authType by remember { mutableStateOf(server.authType) }
    var password by remember { mutableStateOf(server.password) }
    var privateKey by remember { mutableStateOf(server.privateKey) }
    var passphrase by remember { mutableStateOf(server.passphrase) }
    var showPassword by remember { mutableStateOf(false) }
    var presetDirsText by remember { mutableStateOf(server.presetDirectories.joinToString("\n")) }
    var persistentTerminal by remember { mutableStateOf(server.persistentTerminalSession) }
    fun doSave() {
        val p = portText.toIntOrNull() ?: 22
        val updated = server.copy(
            name = name.ifBlank { "Server" },
            description = description.trim(),
            icon = selectedIconKey,
            host = host.trim(),
            port = p,
            username = username.trim().ifBlank { "root" },
            authType = authType,
            password = password,
            privateKey = privateKey,
            passphrase = passphrase,
            presetDirectories = presetDirsText.lines().map { it.trim() }.filter { it.isNotBlank() },
            persistentTerminalSession = persistentTerminal
        )
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "基础连接设置",
            onBack = onBack,
            isDark = isDark,
            showSave = true,
            onSave = { doSave() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OpenAiSectionCard(
                headerTitle = "服务器主机与账号",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("服务器名称") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("用途描述 (展示在卡片，隐私友好)") },
                        placeholder = { Text("例如: 模型推理工作站") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("IP 地址 / 域名 (Host)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(0.7f)
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = { portText = it },
                            label = { Text("端口") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.3f)
                        )
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("登录用户名") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 自选图标条目 (放置在底下一列，单独显示，点击展开)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showIconPicker = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF161616) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2C2D30) else Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = com.bettershell.app.ui.components.ServerIconCatalog.getIcon(selectedIconKey),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "服务器图标",
                                    fontSize = 15.sp,
                                    color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
                                )
                            }
                            Text(
                                text = "更换图标 >",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            OpenAiSectionCard(
                headerTitle = "身份凭据与验证",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val activePillBg = if (isDark) Color(0xFF424242) else Color(0xFFFFFFFF)
                        val activeBorder = if (isDark) Color(0xFF555555) else Color(0xFFE0E0E0)
                        val inactiveBorder = if (isDark) Color(0xFF2C2D30) else Color(0xFFE5E7EB)
                        val inactiveBg = if (isDark) Color(0xFF161616) else Color(0xFFFFFFFF)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (authType == AuthType.PASSWORD) activePillBg else inactiveBg,
                            border = BorderStroke(1.dp, if (authType == AuthType.PASSWORD) activeBorder else inactiveBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { authType = AuthType.PASSWORD }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "密码认证",
                                    fontWeight = if (authType == AuthType.PASSWORD) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (authType == AuthType.PASSWORD) (if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (authType == AuthType.PRIVATE_KEY) activePillBg else inactiveBg,
                            border = BorderStroke(1.dp, if (authType == AuthType.PRIVATE_KEY) activeBorder else inactiveBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { authType = AuthType.PRIVATE_KEY }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "私钥认证",
                                    fontWeight = if (authType == AuthType.PRIVATE_KEY) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (authType == AuthType.PRIVATE_KEY) (if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (authType == AuthType.PASSWORD) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("SSH 密码") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = privateKey,
                            onValueChange = { privateKey = it },
                            label = { Text("私钥内容 (PEM / OpenSSH)") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        OutlinedTextField(
                            value = passphrase,
                            onValueChange = { passphrase = it },
                            label = { Text("私钥密码 (Passphrase, 可选)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 预置项目目录配置 (方便 Work 模式一键切换)
            OpenAiSectionCard(
                headerTitle = "工作空间与项目目录",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "预置项目目录列表 (每行一个，用于 Work 模式快速跳转与文件探测)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = presetDirsText,
                        onValueChange = { presetDirsText = it },
                        placeholder = { Text("/home/user/myproject\n/var/www/site\n~/dev/agent-repo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(14.dp),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "终端常驻守护 (Tmux / Herdr 模式)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "断开 SSH 后远程终端与任务不中断，重新连接后自动恢复",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = persistentTerminal,
                            onCheckedChange = { persistentTerminal = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showIconPicker) {
        com.bettershell.app.ui.components.ServerIconPickerBottomSheet(
            currentIconKey = selectedIconKey,
            onSelectIcon = { selectedIconKey = it },
            onDismiss = { showIconPicker = false }
        )
    }
}
