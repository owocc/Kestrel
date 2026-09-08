package com.bettershell.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.AppConstants
import com.bettershell.app.R
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 独立的关于页面 (对标 ChatGPT Remote 截图的分段独立圆角卡片)
 * - 顶部展示带有 24dp 大圆角与精致描边的 App 官方高清图标 (A21)
 * - 下方紧随应用信息卡片
 */
@Composable
fun AppAboutScreen(
    onBack: () -> Unit
) {
    val isDark = isAppInDarkTheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "关于",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // 1. App 品牌圆角大图标展示 (96dp x 96dp，24dp 大圆角，带微光描边)
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = AppConstants.APP_NAME,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppConstants.APP_NAME,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = AppConstants.APP_VERSION_LABEL,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 2. 应用信息分段卡片 (ChatGPT 风格)
            OpenAiSectionCard(
                headerTitle = "详细信息"
            ) {
                // 第一个卡片：顶部大圆角，底部小圆角
                OpenAiSettingRow(
                    title = "应用名称",
                    subtitle = AppConstants.APP_NAME,
                    position = CardPosition.TOP
                )

                // 中间卡片：全小圆角
                OpenAiSettingRow(
                    title = "版本代号",
                    subtitle = "v${AppConstants.APP_VERSION} (Android 16 Ready)",
                    position = CardPosition.MIDDLE
                )

                OpenAiSettingRow(
                    title = "核心引擎",
                    subtitle = AppConstants.CORE_ENGINE_INFO,
                    position = CardPosition.MIDDLE
                )

                // 最后一个卡片：顶部小圆角，底部大圆角
                OpenAiSettingRow(
                    title = "开源协议",
                    subtitle = AppConstants.OPEN_SOURCE_LICENSE,
                    position = CardPosition.BOTTOM
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
