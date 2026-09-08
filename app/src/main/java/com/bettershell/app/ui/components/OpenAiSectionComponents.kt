package com.bettershell.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 完整对齐截图 1 的 OpenAI 风格分组色块卡片 (Section Group Card)
 * - 采用浅灰纯色块（#F3F4F6 或深色 #1E1E1E）作为容器
 * - 大圆角 RoundedCornerShape(22.dp)
 * - 组内包含多行条目，条目间带有浅色细分割线
 */
@Composable
fun OpenAiSectionCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
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
                color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF6B7280),
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
 * 对标截图 1 的行条目组件 (Setting Row Item)
 * - 左侧图标 (可选)
 * - 中间标题与副标题
 * - 右侧结尾内容或向下/向右展开箭头
 */
@Composable
fun OpenAiSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    showDivider: Boolean = true,
    isDark: Boolean = false,
    trailingText: String? = null,
    showDropdownArrow: Boolean = false,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val dividerColor = if (isDark) Color(0xFF2B2B2B) else Color(0xFFE5E7EB)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF6B7280)
                    )
                }

                if (showDropdownArrow) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF9E9E9E) else Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                } else if (showChevron) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF9E9E9E) else Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = dividerColor,
                thickness = 0.8.dp,
                modifier = Modifier.padding(start = if (icon != null) 50.dp else 16.dp)
            )
        }
    }
}
