package com.bettershell.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.AccentOrange

enum class ServerSettingSubSection {
    INDEX,
    BASIC_CONNECTION,
    AI_AGENTS,
    STARTUP_SCRIPT
}

/**
 * 严格遵照截图 1 & 截图 2 的 OpenAI 风格多列分组服务器设置
 * - 根视图为多列分组（基础连接、AI与Agent环境、初始化脚本）
 * - 点击任一分类，进入该分类的独立保存/编辑子页面 (带独立标准页头，右侧圆钮保存)
 * - 点击具体 Agent CLI，进入该模型的专属推理配置子页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServerSettingsScreen(
    server: ServerConfig,
    agentDiscoveryRepo: AgentDiscoveryRepository,
    onSaveServer: (ServerConfig) -> Unit,
    onBack: () -> Unit
) {
    var activeSubSection by remember { mutableStateOf(ServerSettingSubSection.INDEX) }
    val isDark = isSystemInDarkTheme()

    // 基础连接状态
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

    // Agent 数据
    var discoveredAgents by remember(server.id) {
        mutableStateOf(agentDiscoveryRepo.getCachedAgents(server.id))
    }
    var inspectingAgent by remember { mutableStateOf<DiscoveredAgent?>(null) }

    fun doSave() {
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
            startupScript = startupScript
        )
        onSaveServer(updated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 1. 如果在单个 CLI Agent 的配置页面
        if (inspectingAgent != null) {
            SingleAgentConfigSubPage(
                agent = inspectingAgent!!,
                isDark = isDark,
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

        // 2. 子分类页面：基础连接配置 (单独保存)
        if (activeSubSection == ServerSettingSubSection.BASIC_CONNECTION) {
            StandardPageHeader(
                title = "基础连接设置",
                onBack = { activeSubSection = ServerSettingSubSection.INDEX },
                isDark = isDark,
                showSave = true,
                onSave = {
                    doSave()
                    activeSubSection = ServerSettingSubSection.INDEX
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OpenAiSectionCard(
                    headerTitle = "服务器信息",
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
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (authType == AuthType.PASSWORD) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (authType == AuthType.PASSWORD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { authType = AuthType.PASSWORD }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text("密码认证", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (authType == AuthType.PASSWORD) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (authType == AuthType.PRIVATE_KEY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (authType == AuthType.PRIVATE_KEY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
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
            }
            return@Column
        }

        // 3. 子分类页面：AI & Agent 专属管理 (展示已发现 Agent，点击进入单个 CLI 配置)
        if (activeSubSection == ServerSettingSubSection.AI_AGENTS) {
            StandardPageHeader(
                title = "AI & Agent 设置",
                onBack = { activeSubSection = ServerSettingSubSection.INDEX },
                isDark = isDark
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OpenAiSectionCard(
                    headerTitle = "已就绪 Agent (${discoveredAgents.size})",
                    isDark = isDark
                ) {
                    if (discoveredAgents.isEmpty()) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "当前服务器尚未发现已安装的 Agent\n可在首页点击右侧菜单进行探测",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        discoveredAgents.forEachIndexed { index, agent ->
                            val meta = SupportedAgentsCatalog.findMeta(agent.command)
                            OpenAiSettingRow(
                                title = meta.displayName,
                                subtitle = "模型: ${agent.selectedModel} · 思考: ${agent.thinkingLevel.displayName}",
                                icon = Icons.Rounded.SmartToy,
                                iconTint = Color(meta.avatarBgColorHex),
                                showChevron = true,
                                isDark = isDark,
                                showDivider = index < discoveredAgents.lastIndex,
                                onClick = { inspectingAgent = agent }
                            )
                        }
                    }
                }
            }
            return@Column
        }

        // 4. 子分类页面：初始化 Shell 脚本 (单独保存)
        if (activeSubSection == ServerSettingSubSection.STARTUP_SCRIPT) {
            StandardPageHeader(
                title = "启动脚本设置",
                onBack = { activeSubSection = ServerSettingSubSection.INDEX },
                isDark = isDark,
                showSave = true,
                onSave = {
                    doSave()
                    activeSubSection = ServerSettingSubSection.INDEX
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OpenAiSectionCard(
                    headerTitle = "自动执行 Shell 脚本",
                    isDark = isDark
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "每次进入终端时，会自动逐行执行这些 Shell 指令：",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = startupScript,
                            onValueChange = { startupScript = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            shape = RoundedCornerShape(14.dp),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp)
                        )
                    }
                }
            }
            return@Column
        }

        // 5. 主菜单索引视图 (严格遵循截图 1 的 OpenAI 风格多列分组)
        StandardPageHeader(
            title = "服务器设置",
            onBack = onBack,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 分组 1：连接与网络
            OpenAiSectionCard(
                headerTitle = "连接管理",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "基础连接设置",
                    subtitle = "${username}@${host}:${portText}",
                    icon = Icons.Rounded.Dns,
                    showChevron = true,
                    isDark = isDark,
                    showDivider = false,
                    onClick = { activeSubSection = ServerSettingSubSection.BASIC_CONNECTION }
                )
            }

            // 分组 2：AI Agent 生态
            OpenAiSectionCard(
                headerTitle = "AI 智能体",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "AI & Agent 设置",
                    subtitle = "已检测就绪 ${discoveredAgents.size} 个 Agent 工具",
                    icon = Icons.Rounded.SmartToy,
                    iconTint = AccentCyan,
                    showChevron = true,
                    isDark = isDark,
                    showDivider = false,
                    onClick = { activeSubSection = ServerSettingSubSection.AI_AGENTS }
                )
            }

            // 分组 3：自动化脚本
            OpenAiSectionCard(
                headerTitle = "高级选项",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "初始化启动脚本",
                    subtitle = if (startupScript.isNotBlank()) "已配置自动化指令" else "未配置",
                    icon = Icons.Rounded.Code,
                    showChevron = true,
                    isDark = isDark,
                    showDivider = false,
                    onClick = { activeSubSection = ServerSettingSubSection.STARTUP_SCRIPT }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * 独立的单个 CLI Agent 配置页面 (Full Screen Sub-Page)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SingleAgentConfigSubPage(
    agent: DiscoveredAgent,
    isDark: Boolean,
    onBack: () -> Unit,
    onUpdateAgent: (DiscoveredAgent) -> Unit
) {
    val meta = SupportedAgentsCatalog.findMeta(agent.command)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StandardPageHeader(
            title = "${meta.displayName} 配置",
            onBack = onBack,
            isDark = isDark
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OpenAiSectionCard(isDark = isDark) {
                OpenAiSettingRow(
                    title = "可执行文件路径",
                    subtitle = agent.path,
                    icon = Icons.Rounded.Code,
                    showDivider = false,
                    isDark = isDark
                )
            }

            OpenAiSectionCard(
                headerTitle = "默认推理模型 (Model Routing)",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        agent.models.forEach { model ->
                            val isSelected = agent.selectedModel == model || (agent.selectedModel.isBlank() && model == "default")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier.clickable {
                                    onUpdateAgent(agent.copy(selectedModel = model))
                                }
                            ) {
                                Text(
                                    text = model,
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            OpenAiSectionCard(
                headerTitle = "思考深度参数 (Thinking Level)",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThinkingLevel.entries.forEach { level ->
                            val isSelected = agent.thinkingLevel == level
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AccentOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier.clickable {
                                    onUpdateAgent(agent.copy(thinkingLevel = level))
                                }
                            ) {
                                Text(
                                    text = level.displayName,
                                    style = TextStyle(fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
