package com.bettershell.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 规范化统一页头 (对齐用户截图 2 的 OpenAI 极简 Header 规范):
 * - 左侧：白色/浅灰圆形底衬的返回箭头按钮 (Rounded Circle Back Button)
 * - 居中：绝对水平居中的页面标题 (Centered Title, 16~17sp, SemiBold)
 * - 右侧：白色/浅灰圆形底衬的灰色小勾确认/保存按钮 (可配置显隐和点击事件)
 */
@Composable
fun StandardPageHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    showSave: Boolean = false,
    onSave: (() -> Unit)? = null
) {
    val circleButtonBg = if (isDark) Color(0xFF262626) else Color(0xFFF3F4F6)
    val iconTint = if (isDark) Color(0xFFE5E7EB) else Color(0xFF1F2937)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. 左侧圆形返回按钮
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .clip(CircleShape)
                .background(circleButtonBg)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        // 2. 居中标题
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        // 3. 右侧圆形保存按钮 (对齐截图 2 的圆形 Check 勾勾)
        if (showSave && onSave != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                .clip(CircleShape)
                .background(circleButtonBg)
                .clickable(onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Save",
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
