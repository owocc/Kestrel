package com.bettershell.app.ui.components

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.ui.theme.isAppInDarkTheme

data class OpenAiMenuItemData(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * 全局统一的 OpenAI 风格高立体感浮层菜单 (Unified OpenAI Dropdown Menu)
 * - 容器去 padding：菜单容器自身零内边距 (padding = 0.dp)
 * - 占满宽度：每一个 item 铺满整行宽度 (fillMaxWidth)，点击水波纹整行铺开
 * - 去除 item 自身圆角：平铺式设计，无独立圆角胶囊块
 * - 增加上下内边距：每一个 item 拥有舒适的上下留白 (vertical = 14.dp)
 * - 阴影扩大：扩散与 blur 程度扩大到 20dp (elevation = 20.dp)，alpha 保持通透
 */
@Composable
fun OpenAiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<OpenAiMenuItemData>,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    width: Dp = 210.dp
) {
    val menuBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(20.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = offset,
            modifier = modifier
                .width(width)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.38f else 0.10f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.28f else 0.08f)
                )
                .background(menuBg)
                .border(1.dp, borderColor.copy(alpha = if (isDark) 0.5f else 0.7f), RoundedCornerShape(20.dp))
                .padding(0.dp) // 容器绝对无内边距
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
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
            .padding(horizontal = 16.dp, vertical = 14.dp), // 占满宽度，上下边距增加为 14dp，去除 item 自身圆角
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
                modifier = Modifier.size(19.dp)
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
