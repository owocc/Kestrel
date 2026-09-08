package com.bettershell.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.ui.theme.AccentOrange
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 独立思考程度选择弹窗 (SelectThinkingLevelBottomSheet)
 * 专门用于在 Work 模式下调节大模型/Agent 的思考程度 (Thinking Level)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectThinkingLevelBottomSheet(
    currentLevel: ThinkingLevel,
    onSelectLevel: (ThinkingLevel) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme
    val itemBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val itemBorder = if (isDark) Color(0xFF2C2D30) else Color(0xFFE5E7EB)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Brain,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "思考程度 (Thinking Level)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThinkingLevel.entries.forEach { level ->
                    val isSelected = currentLevel == level
                    val levelDescription = when (level) {
                        ThinkingLevel.AUTO -> "由 Agent 根据任务复杂性动态决定是否思考"
                        ThinkingLevel.OFF -> "关闭思考过程，直接生成最终答复，响应最快"
                        ThinkingLevel.LOW -> "轻度逻辑推演，消耗较少 Token"
                        ThinkingLevel.MEDIUM -> "标准深度思考，推荐绝大多数开发任务"
                        ThinkingLevel.HIGH -> "深入推导与代码分析，适合复杂重构"
                        ThinkingLevel.MAX -> "满配极强深度思考，进行多轮自我纠错与验证"
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AccentOrange.copy(alpha = 0.14f) else itemBg,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) AccentOrange else itemBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLevel(level)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = level.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = levelDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = AccentOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
