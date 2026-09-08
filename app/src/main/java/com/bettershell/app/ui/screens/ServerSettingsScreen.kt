package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.agent.AgentDiscoveryRepository
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 服务器设置首页 (完全复用全局通用的 OpenAiSectionCard 和 CardPosition)
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
            onBack = onBack
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
            // 分组 1：连接与网络
            OpenAiSectionCard(
                headerTitle = "连接管理"
            ) {
                OpenAiSettingRow(
                    title = "基础连接配置",
                    subtitle = "${server.username}@${server.host}:${server.port}",
                    icon = LucideIcons.ServerCog,
                    showChevron = true,
                    position = CardPosition.SINGLE,
                    onClick = onNavigateToBasic
                )
            }

            // 分组 2：AI Agent 生态
            OpenAiSectionCard(
                headerTitle = "AI 智能体"
            ) {
                OpenAiSettingRow(
                    title = "Agent 运行时环境",
                    subtitle = "已探测 ${discoveredAgents.size} 个就绪 Agent (omp, claude, aider)",
                    icon = LucideIcons.Asteroid,
                    showChevron = true,
                    position = CardPosition.SINGLE,
                    onClick = onNavigateToAgents
                )
            }

            // 分组 3：自动化脚本
            OpenAiSectionCard(
                headerTitle = "高级选项"
            ) {
                OpenAiSettingRow(
                    title = "终端启动脚本",
                    subtitle = if (server.startupScript.isNotBlank()) "已配置自动化指令" else "未配置自动化启动指令",
                    icon = LucideIcons.Terminal,
                    showChevron = true,
                    position = CardPosition.SINGLE,
                    onClick = onNavigateToStartup
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
