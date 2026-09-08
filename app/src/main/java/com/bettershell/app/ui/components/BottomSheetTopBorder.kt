package com.bettershell.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 底部弹窗顶部圆角渐变微光描边 Modifier
 * 专门沿着 RoundedCornerShape(topStart, topEnd) 的上边缘绘制清晰精致的 1dp 渐变描边：
 * - 中间高光清透，越到两侧/两端尽头越透明渐隐 (Transparent -> BaseColor -> Transparent)
 * - 营造极其轻盈、通透的高级质感！
 */
fun Modifier.bottomSheetTopBorder(
    strokeWidth: Dp = 1.dp,
    color: Color = Color(0xFF383838),
    cornerRadius: Dp = 28.dp
): Modifier = this.drawWithContent {
    drawContent()
    val strokePx = strokeWidth.toPx()
    val radiusPx = cornerRadius.toPx()
    val halfStroke = strokePx / 2f

    val path = Path().apply {
        // 从左侧圆角起点开始 (0, radiusPx)
        moveTo(halfStroke, radiusPx)
        // 左上圆角弧线
        arcTo(
            rect = Rect(
                offset = Offset(halfStroke, halfStroke),
                size = Size(radiusPx * 2 - strokePx, radiusPx * 2 - strokePx)
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        // 顶部水平线
        lineTo(size.width - radiusPx, halfStroke)
        // 右上圆角弧线
        arcTo(
            rect = Rect(
                offset = Offset(size.width - radiusPx * 2 + halfStroke, halfStroke),
                size = Size(radiusPx * 2 - strokePx, radiusPx * 2 - strokePx)
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        // 右侧圆角终点向下延伸
        lineTo(size.width - halfStroke, radiusPx)
    }

    // 两端完全透明渐隐，中央高亮清晰的水平线性渐变笔刷
    val gradientBrush = Brush.horizontalGradient(
        0.0f to Color.Transparent,
        0.08f to color.copy(alpha = 0.25f),
        0.20f to color.copy(alpha = 0.75f),
        0.50f to color,
        0.80f to color.copy(alpha = 0.75f),
        0.92f to color.copy(alpha = 0.25f),
        1.0f to Color.Transparent,
        startX = 0f,
        endX = size.width
    )

    drawPath(
        path = path,
        brush = gradientBrush,
        style = Stroke(width = strokePx)
    )
}
