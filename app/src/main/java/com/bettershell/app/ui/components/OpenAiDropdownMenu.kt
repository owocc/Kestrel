package com.bettershell.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bettershell.app.ui.theme.isAppInDarkTheme

data class OpenAiMenuItemData(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * 真正 0 边距、纯沉浸式定制 OpenAI 浮层菜单 (Zero-Padding Immersive Dropdown Menu)
 * - 彻底脱离官方原生 DropdownMenu 内部死死写死的 8dp MenuVerticalMargin
 * - 基于标准 Popup + Surface 实现，实现真正的顶部与底部 0 间距
 * - 第一个 Item 紧贴顶边，最后一个 Item 紧贴底边，水波纹完美充满 20dp 大圆角
 * - 阴影 Blur 扩大至 20dp，通透柔和扩散
 */
@Composable
fun OpenAiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<OpenAiMenuItemData>,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    width: Dp = 210.dp
) {
    if (!expanded) return

    val menuBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 0, y = 14),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .width(width)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.40f else 0.12f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(menuBg)
                .border(1.dp, borderColor.copy(alpha = if (isDark) 0.5f else 0.7f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = menuBg,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp) // 真正没有任何上下内边距
            ) {
                items.forEachIndexed { index, item ->
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
    isDark: Boolean = isAppInDarkTheme,
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
        else -> if (isDark) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 19.dp), // 饱满舒适的 19dp 上下高纵深
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = actualIconTint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
