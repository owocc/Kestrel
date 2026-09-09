package com.bettershell.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.agent.AgentDiscoveryRepository
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentMeta
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.AccentOrange

enum class ServerSettingsTab(val title: String) {
    BASIC("基础连接"),
    AI_AGENTS("AI & Agent 设置"),
    STARTUP("启动脚本")
}

/**
 * 统一复用的服务器深度管理与配置底栏抽屉
 * 在首页 ServerList 和进入终端 Session 后 100% 复用同一个组件！
 * 包含：
 * 1. 基础连接设置 (可随时修改服务器名称、Host/IP/域名、端口、用户名、密码/私钥)
 * 2. AI & Agent 设置 (查看已检测/未安装 Agent，点击每个 Agent 支持进入单独二级配置页面修改该 CLI 专属模型与思考深度)
 * 3. 启动脚本 (自定义自动执行 Bash 脚本)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UnifiedServerSettingsBottomSheet(
    server: ServerConfig,
    agentDiscoveryRepo: AgentDiscoveryRepository,
    onSaveServer: (ServerConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(ServerSettingsTab.BASIC) }

    // 基础连接表单状态
    var name by remember { mutableStateOf(server.name) }
    var host by remember { mutableStateOf(server.host) }
    var portText by remember { mutableStateOf(server.port.toString()) }
    var username by remember { mutableStateOf(server.username) }
    var authType by remember { mutableStateOf(server.authType) }
    var password by remember { mutableStateOf(server.password) }
    var privateKey by remember { mutableStateOf(server.privateKey) }
    var passphrase by remember { mutableStateOf(server.passphrase) }
    var startupScript by remember { mutableStateOf(server.startupScript) }
    var showPassword by remember { mutableStateOf(false) }

    // Agent 数据状态
    var discoveredAgents by remember(server.id) {
        mutableStateOf(agentDiscoveryRepo.getCachedAgents(server.id))
    }
    // 选中的某个 CLI Agent，点击进入单独详细配置页面
    var inspectingAgent by remember { mutableStateOf<DiscoveredAgent?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 如果处于某个具体 CLI 的单独配置二级页面
            if (inspectingAgent != null) {
                SingleAgentConfigPage(
                    agent = inspectingAgent!!,
                    onBack = { inspectingAgent = null },
                    onUpdateAgent = { updatedAgent ->
                        val updated = discoveredAgents.map { if (it.id == updatedAgent.id) updatedAgent else it }
                        discoveredAgents = updated
                        agentDiscoveryRepo.saveAgents(server.id, updated)
                        inspectingAgent = updatedAgent
                    }
                )
                return@Column
            }

            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "服务器设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${server.name} · ${server.host}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 保存基础配置按钮
                    Button(
                        onClick = {
                            val p = portText.toIntOrNull() ?: 22
                            val updated = server.copy(
                                name = name.ifBlank { "Server" },
                                host = host.trim(),
                                port = p,
                                username = username.trim().ifBlank { "root" },
                                authType = authType,
                                password = password,
                                privateKey = privateKey,
                                passphrase = passphrase,
                                startupScript = startupScript,
                                defaultAgentId = server.defaultAgentId,
                                presetDirectories = server.presetDirectories,
                                persistentTerminalSession = server.persistentTerminalSession
                            )
                            onSaveServer(updated)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存", fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 多个菜单 Tab 切换胶囊 (基础连接 | AI & Agent 设置 | 启动脚本)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ServerSettingsTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tab }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tab 1: 基础连接设置
            if (selectedTab == ServerSettingsTab.BASIC) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("服务器显示名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("IP 地址 / 域名 (Host)") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = { portText = it },
                            label = { Text("端口") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.3f)
                        )
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("登录用户名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 认证类型切换 (密码 vs 密钥)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (authType == AuthType.PASSWORD) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (authType == AuthType.PASSWORD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { authType = AuthType.PASSWORD }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text("密码登录", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (authType == AuthType.PASSWORD) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (authType == AuthType.PRIVATE_KEY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (authType == AuthType.PRIVATE_KEY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { authType = AuthType.PRIVATE_KEY }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text("私钥认证", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (authType == AuthType.PRIVATE_KEY) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (authType == AuthType.PASSWORD) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("SSH 密码") },
                            singleLine = true,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        OutlinedTextField(
                            value = passphrase,
                            onValueChange = { passphrase = it },
                            label = { Text("私钥密码 (Passphrase, 可选)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Tab 2: AI & Agent 设置 (展示已检测/未安装列表，点击任何一个 CLI 进入单独页面显示单模型配置)
            if (selectedTab == ServerSettingsTab.AI_AGENTS) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "点击下方任意 Agent CLI 可进入单独页面配置其模型与思考参数：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "已就绪的 Agent CLI (${discoveredAgents.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (discoveredAgents.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("暂未检测到已就绪的 Agent，可在首页卡片菜单中点击【扫描 Agent】", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            discoveredAgents.forEach { agent ->
                                val meta = SupportedAgentsCatalog.findMeta(agent.command)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { inspectingAgent = agent },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(meta.avatarBgColorHex)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(meta.shortName, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(meta.avatarTextColorHex)))
                                            }
                                            Column {
                                                Text(meta.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text("当前模型: ${agent.selectedModel} · 思考: ${agent.thinkingLevel.displayName}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("单独配置", style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontWeight = FontWeight.SemiBold)
                                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp).clip(CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 3: 启动脚本
            if (selectedTab == ServerSettingsTab.STARTUP) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "终端启动脚本 (Startup Script)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "进入终端时会自动逐行执行这些 Shell 指令（如：source ~/.zshrc，切换目录等）：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = startupScript,
                        onValueChange = { startupScript = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                }
            }
        }
    }
}

/**
 * 专属单个 CLI Agent 的单独配置页面
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SingleAgentConfigPage(
    agent: DiscoveredAgent,
    onBack: () -> Unit,
    onUpdateAgent: (DiscoveredAgent) -> Unit
) {
    val meta = SupportedAgentsCatalog.findMeta(agent.command)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(meta.avatarBgColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(meta.shortName, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(meta.avatarTextColorHex)))
            }

            Column {
                Text(meta.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(agent.path, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // 1. 该 CLI 单独模型配置
        Text(
            text = "默认推理模型 (Model Routing)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agent.models.forEach { model ->
                val isSelected = agent.selectedModel == model || (agent.selectedModel.isBlank() && model == "default")
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        onUpdateAgent(agent.copy(selectedModel = model))
                    }
                ) {
                    Text(
                        text = model,
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. 思考深度 (Thinking Level)
        Text(
            text = "思考深度参数 (Thinking Level)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThinkingLevel.entries.forEach { level ->
                val isSelected = agent.thinkingLevel == level
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) AccentOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isSelected) AccentOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        onUpdateAgent(agent.copy(thinkingLevel = level))
                    }
                ) {
                    Text(
                        text = level.displayName,
                        style = TextStyle(fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("完成并返回")
        }
    }
}
