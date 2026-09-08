package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 独立的关于页面 (对标 ChatGPT Remote 截图的分段独立圆角卡片)
 */
@Composable
fun AppAboutScreen(
    onBack: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OpenAiSectionCard(
                headerTitle = "应用信息"
            ) {
                // 第一个卡片：顶部大圆角，底部小圆角
                OpenAiSettingRow(
                    title = com.bettershell.app.AppConstants.APP_NAME,
                    subtitle = com.bettershell.app.AppConstants.APP_VERSION_LABEL,
                    position = CardPosition.TOP
                )

                // 中间卡片：全小圆角
                OpenAiSettingRow(
                    title = "核心引擎",
                    subtitle = com.bettershell.app.AppConstants.CORE_ENGINE_INFO,
                    position = CardPosition.MIDDLE
                )

                // 最后一个卡片：顶部小圆角，底部大圆角
                OpenAiSettingRow(
                    title = "开源协议",
                    subtitle = com.bettershell.app.AppConstants.OPEN_SOURCE_LICENSE,
                    position = CardPosition.BOTTOM
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
