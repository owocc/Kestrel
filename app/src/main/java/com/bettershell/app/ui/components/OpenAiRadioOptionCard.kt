package com.bettershell.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * OpenAI / ChatGPT 官方标准单选条目组件 (OpenAiRadioOptionCard)
 * - 严格复用现有 ChatGPT Remote 拆分卡片 (CardPosition: TOP / MIDDLE / BOTTOM / SINGLE)
 * - 动态圆角 (22dp / 6dp) + 3dp 卡片纯净间距
 * - 右侧内嵌原汁原味的大号单选外圈与中心实心圆点
 * - 支持主标题 (15.5sp Medium) 与副标题说明文字 (13.5sp Normal)
 */
@Composable
fun OpenAiRadioOptionCard(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    position: CardPosition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme
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
        modifier = modifier
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
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧文案区域：主标题与副标题描述
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = titleColor
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 18.sp,
                            color = subtitleColor
                        )
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                // 右侧官方单选圆圈 (ChatGPT Radio Circle)
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
