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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 卡片在列表中的相对位置模式：
 * - SINGLE: 单张卡片（全大圆角 22dp）
 * - TOP: 第一个卡片（顶端大圆角 22dp，底部小圆角 6dp）
 * - MIDDLE: 中间卡片（全小圆角 6dp）
 * - BOTTOM: 最后一个卡片（顶端小圆角 6dp，底部大圆角 22dp）
 */
enum class CardPosition {
    SINGLE,
    TOP,
    MIDDLE,
    BOTTOM
}

/**
 * 根据卡片所在位置生成对齐最新 ChatGPT Remote 截图的圆角形状：
 * - 顶部卡片：top = 22dp, bottom = 6dp
 * - 中间卡片：top = 6dp, bottom = 6dp
 * - 底部卡片：top = 6dp, bottom = 22dp
 * - 独立卡片：all = 22dp
 */
fun getCardShape(position: CardPosition, largeRadius: Dp = 22.dp, smallRadius: Dp = 6.dp): RoundedCornerShape {
    return when (position) {
        CardPosition.SINGLE -> RoundedCornerShape(largeRadius)
        CardPosition.TOP -> RoundedCornerShape(
            topStart = largeRadius,
            topEnd = largeRadius,
            bottomStart = smallRadius,
            bottomEnd = smallRadius
        )
        CardPosition.MIDDLE -> RoundedCornerShape(smallRadius)
        CardPosition.BOTTOM -> RoundedCornerShape(
            topStart = smallRadius,
            topEnd = smallRadius,
            bottomStart = largeRadius,
            bottomEnd = largeRadius
        )
    }
}

/**
 * 完整对齐 ChatGPT 截图的分组外壳容器 (Section Container)
 * 带有分组 headerTitle
 */
@Composable
fun OpenAiSectionCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme,
    headerTitle: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (headerTitle != null) {
            Text(
                text = headerTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * 独立色块条目卡片 (Individual Item Card - 对标 ChatGPT Remote 样式)
 * - 彻底去除细线分割线！
 * - 卡片与卡片之间使用垂直间隙 (默认间隔 3.dp)！
 * - 首张卡片顶部大圆角、底部小圆角；中间卡片全小圆角；末尾卡片底部大圆角！
 */
@Composable
fun OpenAiSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    position: CardPosition = CardPosition.SINGLE,
    showDivider: Boolean = false, // 遵循最新指令：彻底废弃内嵌分割线，保留参数兼容旧调用
    isDark: Boolean = isAppInDarkTheme,
    trailingText: String? = null,
    showDropdownArrow: Boolean = false,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val titleColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val arrowTint = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)

    val shape = getCardShape(position)

    // 卡片间距：非 SINGLE 模式下，卡片之间保留 3dp 纯净空隙
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
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧图标 (如有)
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                }

                // 中间主文字与副标题
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = titleColor
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = subtitleColor
                        )
                    }
                }

                // 右侧附加信息
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = subtitleColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                if (showDropdownArrow) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = arrowTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showChevron) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = arrowTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
