package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.OpenSourceLibrariesData
import com.bettershell.app.data.OpenSourceLibraryDetail
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.components.getCardShape
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 完整对齐 OpenAI / ChatGPT 参考截图的开源许可证列表页面 (OpenSourceLicensesScreen)
 * - 标题栏：带有标准返回圆形按钮与居中“打开开源许可证”大标题
 * - 列表条目：
 *   - 左上方加粗主标题（组件名称，不带冗余包名前缀）
 *   - 右侧灰字版本号 (如 500.0.1 / 1.3.1)
 *   - 作者信息 (如 Google / Mike Penz)
 *   - 下方彩色圆角协议小胶囊 (如 Apache License 2.0 / MIT License)
 *   - 点击平滑推进至对应库的独立协议详情页！
 */
@Composable
fun OpenSourceLicensesScreen(
    onSelectLibrary: (OpenSourceLibraryDetail) -> Unit,
    onBack: () -> Unit
) {
    val isDark = isAppInDarkTheme
    val libraries = OpenSourceLibrariesData.LIBRARIES

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "打开开源许可证",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(libraries, key = { _, lib -> lib.id }) { index, lib ->
                val position = when {
                    libraries.size == 1 -> CardPosition.SINGLE
                    index == 0 -> CardPosition.TOP
                    index == libraries.size - 1 -> CardPosition.BOTTOM
                    else -> CardPosition.MIDDLE
                }

                OpenSourceLicenseItemCard(
                    library = lib,
                    position = position,
                    isDark = isDark,
                    onClick = { onSelectLibrary(lib) }
                )

                if (index < libraries.size - 1) {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
        }
    }
}

/**
 * 对标 OpenAI 截图的开源条目卡片
 */
@Composable
private fun OpenSourceLicenseItemCard(
    library: OpenSourceLibraryDetail,
    position: CardPosition,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val titleColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val shape = getCardShape(position)

    // 胶囊背景与文字颜色配置
    val badgeBg = if (isDark) Color(0xFF2E2438) else Color(0xFFF2E7FE)
    val badgeText = if (isDark) Color(0xFFD7B6F6) else Color(0xFF7C3AED)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = cardBg,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // 首行：组件标题 + 右侧版本号
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = library.name,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = library.version,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = subtitleColor,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 次行：作者
            Text(
                text = library.author,
                fontSize = 13.sp,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(7.dp))

            // 底行：彩色协议小胶囊 (例如 Apache License 2.0 / MIT License)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = library.licenseName,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = badgeText
                )
            }
        }
    }
}
