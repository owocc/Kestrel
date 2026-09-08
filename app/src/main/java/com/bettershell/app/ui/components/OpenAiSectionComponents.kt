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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 全局通用 OpenAI / 设置风格分组卡片 (Universal Section Card)
 * - 全自动根据 App 当前主题自适应：深色纯正 #1E1E1E，浅色纯正 #F3F4F6
 * - 22dp 大圆角容器
 * - 供【关于页面】、【设置页面】、【服务器设置】、【Agent配置】等全量页面复用！
 */
@Composable
fun OpenAiSectionCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme,
    headerTitle: String? = null,
    content: @Composable () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)

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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = cardBg,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * 全局通用设置与详情行条目 (Universal Setting Row Item)
 * - 自动自适应深色/浅色高保真文字与图标颜色
 */
@Composable
fun OpenAiSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    showDivider: Boolean = true,
    isDark: Boolean = isAppInDarkTheme,
    trailingText: String? = null,
    showDropdownArrow: Boolean = false,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val dividerColor = if (isDark) Color(0xFF2C2D30) else Color(0xFFE5E7EB)
    val titleColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val arrowTint = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        fontWeight = FontWeight.Normal,
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

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = if (icon != null) 50.dp else 16.dp),
                thickness = 0.8.dp,
                color = dividerColor
            )
        }
    }
}
