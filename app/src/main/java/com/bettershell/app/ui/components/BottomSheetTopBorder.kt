package com.bettershell.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 底部弹窗顶部圆角描边 Modifier
 * 专门沿着 RoundedCornerShape(topStart, topEnd) 的上边缘绘制清晰精致的 1dp 微光描边，
 * 让纯黑/纯白背景下的 BottomSheet 顶部圆角弧线一目了然！
 */
fun Modifier.bottomSheetTopBorder(
    strokeWidth: Dp = 1.dp,
    color: Color = Color(0xFF333333),
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
        // 右侧圆角终点向下延伸一点 (size.width - halfStroke, radiusPx)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokePx)
    )
}
