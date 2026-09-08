package com.bettershell.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

enum class SessionMode(val title: String) {
    WORK("Work"),
    TERMINAL("Terminal")
}

/**
 * 现代居中双胶囊模式切换滑块 (Chat / Shell 切换)
 * 适配深色 / 浅色模式：
 * - 深色模式：深灰容器 + 高亮中灰滑块
 * - 浅色模式：浅灰容器 + 纯白高质感滑块，带细微边框
 */
@Composable
fun ModeTogglePill(
    currentMode: SessionMode,
    onModeSelected: (SessionMode) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val modes = SessionMode.entries
    val selectedIndex = modes.indexOf(currentMode)

    val containerBg = if (isDark) Color(0xFF262626) else Color(0xFFE5E7EB)
    val sliderBg = if (isDark) Color(0xFF424242) else Color(0xFFFFFFFF)
    val containerBorderColor = if (isDark) Color(0xFF333333) else Color(0xFFD1D5DB)

    BoxWithConstraints(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(containerBg)
            .border(1.dp, containerBorderColor.copy(alpha = 0.5f), RoundedCornerShape(19.dp))
            .padding(3.dp)
    ) {
        val totalWidth = maxWidth
        val segmentWidth = totalWidth / modes.size

        // 滑块偏移量动画
        val sliderOffsetFraction by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "sliderOffset"
        )

        // 滑动的高亮胶囊指示器
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (sliderOffsetFraction * segmentWidth.toPx()).roundToInt(),
                        y = 0
                    )
                }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(sliderBg)
                .then(
                    if (!isDark) Modifier.border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    else Modifier
                )
        )

        // 按钮文字排版
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            modes.forEach { mode ->
                val isSelected = mode == currentMode
                val unselectedTextColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF6B7280)
                val selectedTextColor = if (isDark) Color.White else Color(0xFF111827)

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) selectedTextColor else unselectedTextColor,
                    label = "textColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onModeSelected(mode)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}
