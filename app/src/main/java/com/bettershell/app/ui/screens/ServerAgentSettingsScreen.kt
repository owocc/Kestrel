package com.bettershell.app.ui.screens

import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.agent.AgentDiscoveryRepository
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SshAgentScanner
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.isAppInDarkTheme
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Scan
import kotlinx.coroutines.launch

/**
 * 独立的 Agent 环境设置页面 (完全对标 ChatGPT Remote 截图的分段独立圆角卡片)
 * - 顶栏右上角设置圆形刷新按钮 (带有转圈加载状态)
 * - 页面内提供专属的“扫描 Agent 运行时”卡片按钮
 * - 点击后通过后台 SSH 快速探针即时扫描远程服务器，动态更新已就绪的 Agent 列表并持久化！
 */
@Composable
fun ServerAgentSettingsScreen(
    server: ServerConfig,
    agentDiscoveryRepo: AgentDiscoveryRepository,
    onNavigateToSingleAgent: (DiscoveredAgent) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isAppInDarkTheme

    var isScanning by remember { mutableStateOf(false) }
    var discoveredAgents by remember(server.id) {
        mutableStateOf(agentDiscoveryRepo.getCachedAgents(server.id))
    }

    // 触发 SSH 快速探测
    fun startScan() {
        if (isScanning) return
        isScanning = true
        coroutineScope.launch {
            try {
                val output = SshAgentScanner.scanServer(server)
                val parsed = agentDiscoveryRepo.parseProbeResult(output)
                if (parsed != null) {
                    agentDiscoveryRepo.saveAgents(server.id, parsed)
                    discoveredAgents = parsed
                    Toast.makeText(
                        context,
                        if (parsed.isNotEmpty()) "扫描完成，发现 ${parsed.size} 个就绪 Agent" else "扫描完成，未发现已知 Agent",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "未能获取探针扫描结果", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "SSH 扫描失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isScanning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "Agent 运行时环境",
            onBack = onBack,
            showSave = true, // 右侧圆形操作按钮
            actionIcon = TablerIcons.Refresh,
            actionContentDescription = "刷新扫描",
            isLoading = isScanning,
            onSave = { startScan() }
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
            // 1. 扫描与探测操作区
            OpenAiSectionCard(
                headerTitle = "环境扫描与同步"
            ) {
                OpenAiSettingRow(
                    title = if (isScanning) "正在通过 SSH 探测服务器..." else "扫描服务器 Agent 环境",
                    subtitle = "执行无侵入式快速探针，检测 PATH 与用户环境中安装的 Coding Agent",
                    icon = TablerIcons.Scan,
                    trailingText = if (isScanning) "扫描中..." else "立即检测",
                    position = CardPosition.SINGLE,
                    isDark = isDark,
                    onClick = { startScan() }
                )
            }

            // 2. 探测结果列表
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
                            text = if (isScanning) "正在连接并检测环境，请稍候..." else "未在此服务器探测到已知 Agent\n点击上方“立即检测”或右上角刷新按钮重新扫描",
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
