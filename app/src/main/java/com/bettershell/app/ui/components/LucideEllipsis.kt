package com.bettershell.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lucide 风格的 Ellipsis (三点水平更多图标)
 */
@Composable
fun LucideEllipsis(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension * 0.085f
        val centerY = this.size.height / 2f
        val centerX = this.size.width / 2f
        val spacing = this.size.width * 0.28f

        // 左点
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(centerX - spacing, centerY)
        )
        // 中点
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(centerX, centerY)
        )
        // 右点
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(centerX + spacing, centerY)
        )
    }
}
