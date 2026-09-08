package com.bettershell.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 规范化 Agent 选择底栏弹窗 (SelectAgentBottomSheet)
 * - 顶部小横条也包含在顶描边之下（统一无缝整体）
 * - 拆分独立色块卡片 (CardPosition: TOP / MIDDLE / BOTTOM / SINGLE)
 * - 右侧标准单选圆形选中标记 (Radio Circle)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAgentBottomSheet(
    discoveredAgents: List<DiscoveredAgent>,
    selectedAgent: DiscoveredAgent?,
    onSelectAgent: (DiscoveredAgent) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme
    val itemBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val itemBorder = if (isDark) Color(0xFF2C2D30) else Color(0xFFE5E7EB)
    val topBorderColor = if (isDark) Color(0xFF383838) else Color(0xFFD1D5DB)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null // 自定义内置 dragHandle，确保描边包含在小横条之上！
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f)
                .bottomSheetTopBorder(strokeWidth = 1.dp, color = topBorderColor, cornerRadius = 28.dp)
                .padding(horizontal = 20.dp)
        ) {
            // 内置顶层小横条 (Drag Handle)，使其完全包裹在顶部微光圆角描边之内
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
                }
            }

            // 居中标题
            Text(
                text = "选择 Agent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                if (discoveredAgents.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = itemBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "该服务器尚未探测到已安装的 Agent CLI\n可在服务器设置中重新扫描检测",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val count = discoveredAgents.size
                    discoveredAgents.forEachIndexed { index, agent ->
                        val isSelected = selectedAgent?.id == agent.id
                        val meta = SupportedAgentsCatalog.findMeta(agent.command)

                        val position = when {
                            count == 1 -> CardPosition.SINGLE
                            index == 0 -> CardPosition.TOP
                            index == count - 1 -> CardPosition.BOTTOM
                            else -> CardPosition.MIDDLE
                        }

                        val title = meta.displayName + if (agent.version.isNotBlank()) " v${agent.version}" else ""
                        val subtitle = agent.path.ifBlank { agent.command }

                        OpenAiRadioOptionCard(
                            title = title,
                            subtitle = subtitle,
                            selected = isSelected,
                            position = position,
                            isDark = isDark,
                            onClick = {
                                onSelectAgent(agent)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
