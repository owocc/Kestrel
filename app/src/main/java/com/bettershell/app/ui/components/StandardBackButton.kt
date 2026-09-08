package com.bettershell.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 全局统一标准返回按钮 (Standard Unified Back Button)
 * - 40dp 规范圆形衬底
 * - 浅色模式采用 Color(0xFFF3F4F6)
 * - 深色模式完美适配深黑灰底衬 Color(0xFF1E1E1E)
 * - 图标自适应 MaterialTheme.colorScheme.onSurface
 */
@Composable
fun StandardBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme,
    size: Dp = 40.dp
) {
    val buttonBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val iconTint = if (isDark) Color(0xFFE5E7EB) else Color(0xFF1F2937)

    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onBack),
        shape = CircleShape,
        color = buttonBg,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
