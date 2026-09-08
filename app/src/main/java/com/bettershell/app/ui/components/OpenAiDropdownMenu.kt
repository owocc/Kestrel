package com.bettershell.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OpenAiMenuItemData(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * 完整对齐 OpenAI / ChatGPT 规范的大圆角高立体感浮层菜单 (Large Shadow & Clean Lucide Icons):
 * - 超大立体柔和阴影 (16dp shadowElevation + 细微半透明边界)
 * - 24dp 圆润圆角 (RoundedCornerShape(24.dp))
 * - 彻底去除图标背景底衬，纯净展示原生 Lucide 线条图标
 * - 菜单文字排版紧凑优雅
 */
@Composable
fun OpenAiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<OpenAiMenuItemData>,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    width: Dp = 200.dp
) {
    val menuBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(24.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = offset,
            modifier = modifier
                .width(width)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.65f else 0.18f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.12f)
                )
                .background(menuBg)
                .border(1.dp, borderColor.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                .padding(vertical = 8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items.forEach { item ->
                    OpenAiDropdownMenuItemRow(
                        title = item.title,
                        icon = item.icon,
                        iconTint = item.iconTint,
                        isDestructive = item.isDestructive,
                        isDark = isDark,
                        onClick = {
                            onDismissRequest()
                            item.onClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OpenAiDropdownMenuItemRow(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    isDestructive: Boolean = false,
    isDark: Boolean = false,
    onClick: () -> Unit
) {
    val defaultIconColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF262626)
    val actualIconTint = when {
        isDestructive -> MaterialTheme.colorScheme.error
        iconTint != null -> iconTint
        else -> defaultIconColor
    }
    val textColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 无底衬、纯粹原生 Lucide 图标 (对齐需求: 内部的图标全部去除背景)
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = actualIconTint,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            ),
            color = textColor
        )
    }
}
