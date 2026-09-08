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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 规范化思考程度选择底栏弹窗 (SelectThinkingLevelBottomSheet)
 * - 严格对标 ChatGPT 权限设置截图样式：
 *   - 拆分独立色块卡片 (CardPosition: TOP / MIDDLE / BOTTOM / SINGLE)
 *   - 右侧标准单选圆形选中标记 (Radio Circle)
 *   - 弹窗底部没有任何多余文本，清爽纯粹
 * - 2/3 屏幕高度直接展开，居中标题，无关闭按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectThinkingLevelBottomSheet(
    currentLevel: ThinkingLevel,
    onSelectLevel: (ThinkingLevel) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
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
        val topBorderColor = if (isDark) Color(0xFF383838) else Color(0xFFD1D5DB)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f)
                .bottomSheetTopBorder(strokeWidth = 1.dp, color = topBorderColor, cornerRadius = 28.dp)
                .padding(horizontal = 20.dp)
        ) {
            // 规范化纯净居中标题
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

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                val levels = ThinkingLevel.entries
                val count = levels.size

                levels.forEachIndexed { index, level ->
                    val isSelected = currentLevel == level
                    val levelDescription = when (level) {
                        ThinkingLevel.AUTO -> "由 Agent 根据任务复杂性动态决定是否思考"
                        ThinkingLevel.OFF -> "关闭思考过程，直接生成最终答复，响应最快"
                        ThinkingLevel.LOW -> "轻度逻辑推演，消耗较少 Token"
                        ThinkingLevel.MEDIUM -> "标准深度思考，推荐绝大多数开发任务"
                        ThinkingLevel.HIGH -> "深入推导与代码分析，适合复杂重构"
                        ThinkingLevel.MAX -> "满配极强深度思考，进行多轮自我纠错与验证"
                    }

                    val position = when {
                        count == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == count - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }

                    OpenAiRadioOptionCard(
                        title = level.displayName,
                        subtitle = levelDescription,
                        selected = isSelected,
                        position = position,
                        isDark = isDark,
                        onClick = {
                            onSelectLevel(level)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
