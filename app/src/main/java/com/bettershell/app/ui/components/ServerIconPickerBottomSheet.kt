package com.bettershell.app.ui.components

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
import androidx.compose.ui.unit.sp
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 规范化服务器图标选择底栏弹窗 (ServerIconPickerBottomSheet)
 * - 100% 复用 SelectAgentBottomSheet / SelectThinkingLevelBottomSheet 的官方统一底栏规范：
 *   - 2/3 高度 (fillMaxHeight(0.67f))
 *   - 顶部小横条包裹在渐隐微光描边之下的无缝设计 (bottomSheetTopBorder)
 *   - 纯净背景 (MaterialTheme.colorScheme.background)
 *   - 独立色块卡片 (CardPosition: TOP / MIDDLE / BOTTOM / SINGLE)
 *   - 右侧 ChatGPT 经典同心单选圆圈 (Radio Circle)
 *   - 左侧展示图标与分类说明，彻底修复之前网格弹窗截断的问题！
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerIconPickerBottomSheet(
    currentIconKey: String,
    onSelectIcon: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme
    val topBorderColor = if (isDark) Color(0xFF383838) else Color(0xFFD1D5DB)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val iconOptions = ServerIconCatalog.ALL_ICONS
    val count = iconOptions.size

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
                text = "选择服务器图标",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 采用与 Agent / 思考程度相同结构的独立卡片列表
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                iconOptions.forEachIndexed { index, option ->
                    val isSelected = option.key == currentIconKey || (currentIconKey.isBlank() && option.key == ServerIconCatalog.DEFAULT_KEY)
                    val position = when {
                        count == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == count - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }

                    ServerIconRadioCard(
                        option = option,
                        selected = isSelected,
                        position = position,
                        isDark = isDark,
                        onClick = {
                            onSelectIcon(option.key)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 带有图标预览和 ChatGPT 风格单选框的单选卡片
 */
@Composable
private fun ServerIconRadioCard(
    option: ServerIconOption,
    selected: Boolean,
    position: CardPosition,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val titleColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val radioBorderColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF6B7280)
    val radioInnerDotColor = if (isDark) Color.White else Color(0xFF111827)

    val shape = getCardShape(position)

    val bottomSpacing = when (position) {
        CardPosition.SINGLE -> 0.dp
        CardPosition.BOTTOM -> 0.dp
        else -> 3.dp
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomSpacing),
        shape = shape,
        color = cardBg,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧原生图标预览
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.name,
                    tint = if (selected) MaterialTheme.colorScheme.primary else titleColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(16.dp))

                // 中间标题与分类
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.name,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = option.category,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = subtitleColor
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                // 右侧 ChatGPT 经典同心单选圆圈
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
                        val strokeWidth = 2.dp.toPx()
                        // 外圈
                        drawCircle(
                            color = if (selected) radioInnerDotColor else radioBorderColor,
                            radius = size.minDimension / 2f - strokeWidth / 2f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                        // 选中时的实心同心圆点
                        if (selected) {
                            drawCircle(
                                color = radioInnerDotColor,
                                radius = size.minDimension / 2f - strokeWidth - 2.5.dp.toPx()
                            )
                        }
                    }
                }
            }
        }
    }
}
