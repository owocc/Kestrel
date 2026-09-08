package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.agent.AgentDiscoveryRepository
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 独立的 Agent 环境设置页面 (完全对标 ChatGPT Remote 截图的分段独立圆角卡片)
 */
@Composable
fun ServerAgentSettingsScreen(
    server: ServerConfig,
    agentDiscoveryRepo: AgentDiscoveryRepository,
    onNavigateToSingleAgent: (DiscoveredAgent) -> Unit,
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
            title = "Agent 设置",
            onBack = onBack
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
                headerTitle = "已就绪的 Agent (${discoveredAgents.size})"
            ) {
                if (discoveredAgents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "未在此服务器探测到已知 Agent (支持 omp, Claude Code, Aider 等)",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                } else {
                    discoveredAgents.forEachIndexed { index, agent ->
                        val meta = SupportedAgentsCatalog.findMeta(agent.command)
                        val icon = when (agent.command) {
                            "omp" -> LucideIcons.Asteroid
                            "claude" -> LucideIcons.Brain
                            "aider" -> LucideIcons.Bot
                            else -> LucideIcons.Terminal
                        }

                        val position = when {
                            discoveredAgents.size == 1 -> CardPosition.SINGLE
                            index == 0 -> CardPosition.TOP
                            index == discoveredAgents.size - 1 -> CardPosition.BOTTOM
                            else -> CardPosition.MIDDLE
                        }

                        OpenAiSettingRow(
                            title = meta.displayName,
                            subtitle = if (agent.isAvailable) "已安装 (${agent.version.ifBlank { "版本就绪" }})" else "未就绪",
                            icon = icon,
                            showChevron = true,
                            position = position,
                            onClick = { onNavigateToSingleAgent(agent) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
