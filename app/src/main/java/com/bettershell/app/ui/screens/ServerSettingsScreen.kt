package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.agent.AgentDiscoveryRepository
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 严格遵循截图 1 的 OpenAI 风格多列分组服务器设置首页 (全部通过 pushScreen 进入独立全屏子页面)
 * - 基础连接管理 (点击进入 ServerBasicSettingsScreen)
 * - Agent 智能体设置 (点击进入 ServerAgentSettingsScreen)
 * - 自动化启动脚本 (点击进入 ServerStartupScriptScreen)
 */
@Composable
fun ServerSettingsScreen(
    server: ServerConfig,
    agentDiscoveryRepo: AgentDiscoveryRepository,
    onNavigateToBasic: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToStartup: () -> Unit,
    onBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val discoveredAgents by remember(server.id) {
        mutableStateOf(agentDiscoveryRepo.getCachedAgents(server.id))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
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
            // 分组 1：连接与网络 (图标使用 ServerCog)
            OpenAiSectionCard(
                headerTitle = "连接管理",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "基础连接设置",
                    subtitle = "${server.username}@${server.host}:${server.port}",
                    icon = LucideIcons.ServerCog, // 使用 ServerCog
                    showChevron = true,
                    isDark = isDark,
                    showDivider = false,
                    onClick = onNavigateToBasic
                )
            }

            // 分组 2：AI Agent 生态 (图标使用 Asteroid)
            OpenAiSectionCard(
                headerTitle = "AI 智能体",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "Agent 设置",
                    subtitle = "已检测就绪 ${discoveredAgents.size} 个 Agent 工具",
                    icon = LucideIcons.Asteroid, // 使用 Asteroid
                    showChevron = true,
                    isDark = isDark,
                    showDivider = false,
                    onClick = onNavigateToAgents
                )
            }

            // 分组 3：自动化脚本 (图标使用 Code)
            OpenAiSectionCard(
                headerTitle = "高级选项",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "初始化启动脚本",
                    subtitle = if (server.startupScript.isNotBlank()) "已配置自动化指令" else "未配置",
                    icon = LucideIcons.Terminal,
                    showChevron = true,
                    isDark = isDark,
                    showDivider = false,
                    onClick = onNavigateToStartup
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
