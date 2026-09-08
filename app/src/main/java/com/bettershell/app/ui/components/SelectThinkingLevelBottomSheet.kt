package com.bettershell.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.ui.theme.AccentOrange
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 规范化思考程度选择底栏弹窗 (SelectThinkingLevelBottomSheet)
 * - 直接展开 2/3 屏幕高度 (fillMaxHeight(0.67f) + skipPartiallyExpanded = true)
 * - 无须二次收起/展开，无额外关闭按钮
 * - 纯净居中文案标题，无装饰图标
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f)
                .padding(horizontal = 20.dp)
        ) {
            // 规范化纯净居中标题，无图标，无关闭按钮
            Text(
                text = "思考程度",
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
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
