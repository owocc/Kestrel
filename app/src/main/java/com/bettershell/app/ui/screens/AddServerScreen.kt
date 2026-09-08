package com.bettershell.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.isAppInDarkTheme
import java.util.UUID

/**
 * 独立的添加服务器页面 (AddServerScreen)
 * - 按照二级页面规范进入全局 navigationStack，拥有完全一致的预见式返回和通用 PageHeader
 * - 右上角提供统一圆形勾勾保存按钮
 * - 所有输入框一律使用全圆角 (CircleShape / 999.dp)
 * - 采用最新的 ChatGPT Remote 独立卡片与间隔规范
 */
@Composable
fun AddServerScreen(
    onSave: (ServerConfig) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("terminal") }
    var showIconPicker by remember { mutableStateOf(false) }
    var host by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("root") }
    var authType by remember { mutableStateOf(AuthType.PASSWORD) }
    var password by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var startupScript by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val isDark = isAppInDarkTheme
    val isFormValid = host.isNotBlank() && username.isNotBlank()

    fun doSave() {
        if (!isFormValid) return
        val port = portText.toIntOrNull() ?: 22
        val server = ServerConfig(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { host.trim() },
            host = host.trim(),
            port = port,
            username = username.trim(),
            authType = authType,
            password = password,
            privateKey = privateKey.trim(),
            description = description.trim(),
            icon = selectedIconKey,
            startupScript = startupScript.trim()
        )
        onSave(server)
    }

    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val inputBg = if (isDark) Color(0xFF161616) else Color(0xFFFFFFFF)
    val inputBorder = if (isDark) Color(0xFF2C2D30) else Color(0xFFE5E7EB)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = inputBg,
        unfocusedContainerColor = inputBg,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = inputBorder,
        focusedTextColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827),
        unfocusedTextColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 统一页面头：居中标题“添加服务器”，左侧标准返回按钮，右侧圆形保存勾勾
        StandardPageHeader(
            title = "添加服务器",
            onBack = onBack,
            showSave = isFormValid,
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
            // 1. 连接基础信息卡片
            OpenAiSectionCard(
                headerTitle = "连接信息"
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                    color = cardBg
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("服务器备注名称 (选填)") },
                            placeholder = { Text("例如: AI Agent 节点 / 生产集群") },
                            singleLine = true,
                            shape = CircleShape, // 全圆角
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // 自选图标选择条目
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { showIconPicker = true },
                            shape = CircleShape,
                            color = inputBg,
                            border = BorderStroke(1.dp, inputBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("用途描述 (展示在列表卡片，选填)") },
                            placeholder = { Text("例如: 模型微调工作站，隐私保护") },
                            singleLine = true,
                            shape = CircleShape,
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = host,
                                onValueChange = { host = it },
                                label = { Text("主机地址 / IP *") },
                                placeholder = { Text("192.168.1.100") },
                                singleLine = true,
                                shape = CircleShape, // 全圆角
                                colors = textFieldColors,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = portText,
                                onValueChange = { portText = it },
                                label = { Text("端口") },
                                singleLine = true,
                                shape = CircleShape, // 全圆角
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(0.35f)
                            )
                        }

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("SSH 用户名 *") },
                            placeholder = { Text("root") },
                            singleLine = true,
                            shape = CircleShape, // 全圆角
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 2. 身份凭据卡片
            OpenAiSectionCard(
                headerTitle = "身份凭据与验证"
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                    color = cardBg
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 认证方式切换胶囊 (全圆角)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val authOptions = listOf(
                                AuthType.PASSWORD to "密码认证",
                                AuthType.PRIVATE_KEY to "私钥认证"
                            )
                            authOptions.forEach { (type, label) ->
                                val isSelected = authType == type
                                Surface(
                                    shape = CircleShape, // 全圆角
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else inputBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else inputBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { authType = type }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        if (authType == AuthType.PASSWORD) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("SSH 登录密码") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                },
                                shape = CircleShape, // 全圆角
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = privateKey,
                                onValueChange = { privateKey = it },
                                label = { Text("OpenSSH 私钥内容 (BEGIN OPENSSH PRIVATE KEY)") },
                                placeholder = { Text("-----BEGIN OPENSSH PRIVATE KEY-----\n...") },
                                minLines = 4,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), // 多行文本域采用大圆角
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 3. 高级启动脚本卡片
            OpenAiSectionCard(
                headerTitle = "高级选项 (选填)"
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                    color = cardBg
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = startupScript,
                            onValueChange = { startupScript = it },
                            label = { Text("自动启动脚本命令") },
                            placeholder = { Text("例如: tmux attach || tmux new -s dev") },
                            singleLine = true,
                            shape = CircleShape, // 全圆角
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
