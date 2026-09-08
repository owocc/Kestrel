package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.agent.AgentDiscoveryRepository
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 独立的 Agent 环境设置页面 (单独压入栈，支持预见式返回):
 * - 展示已就绪的 Agent 列表 (去除多余彩色染色，采用纯粹天然单色)
 * - 点击任一 Agent CLI，继续压入栈进入 SingleAgentConfigScreen 进行专属配置
 */
@Composable
fun ServerAgentSettingsScreen(
    server: ServerConfig,
    agentDiscoveryRepo: AgentDiscoveryRepository,
    onNavigateToSingleAgent: (DiscoveredAgent) -> Unit,
    onBack: () -> Unit
) {
    val isDark = com.bettershell.app.ui.theme.isAppInDarkTheme
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
            title = "Agent 设置",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OpenAiSectionCard(
                headerTitle = "已就绪的 Agent (${discoveredAgents.size})",
                isDark = isDark
            ) {
                if (discoveredAgents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "该服务器尚未检测到任何就绪的 Agent CLI",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    discoveredAgents.forEachIndexed { index, agent ->
                        val meta = SupportedAgentsCatalog.findMeta(agent.command)
                        OpenAiSettingRow(
                            title = meta.displayName,
                            subtitle = "模型: ${agent.selectedModel} · 思考: ${agent.thinkingLevel.displayName}",
                            icon = LucideIcons.Asteroid, // 使用 Asteroid 图标
                            showChevron = true,
                            showDivider = index < discoveredAgents.lastIndex,
                            isDark = isDark,
                            onClick = { onNavigateToSingleAgent(agent) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
