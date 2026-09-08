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
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 特别致谢页面 (AcknowledgementsScreen)
 * 致敬 Multica 提供的代码参考与 OpenAI / ChatGPT 提供的卓越 App UI 参考
 */
@Composable
fun AcknowledgementsScreen(
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
            title = "特别致谢",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. 代码参考与架构启发
            OpenAiSectionCard(
                headerTitle = "架构与代码参考"
            ) {
                OpenAiSettingRow(
                    title = "Multica",
                    subtitle = "特别感谢 Multica 团队及其优秀的开源探索，为本项目在多 Agent 交互架构、事件流协议设计与移动端会话工作流体验方面提供了宝贵的代码参考与架构启发。",
                    position = CardPosition.SINGLE,
                    isDark = isDark
                )
            }

            // 2. 界面设计与交互参考
            OpenAiSectionCard(
                headerTitle = "界面与交互灵感"
            ) {
                OpenAiSettingRow(
                    title = "OpenAI / ChatGPT",
                    subtitle = "感谢 OpenAI / ChatGPT 官方移动端在交互动效、单选卡片、一体化输入胶囊与现代界面美学上带来的卓越 UI 参考与灵感范本。",
                    position = CardPosition.SINGLE,
                    isDark = isDark
                )
            }

            // 3. 开源先驱寄语
            OpenAiSectionCard(
                headerTitle = "致谢寄语"
            ) {
                OpenAiSettingRow(
                    title = "致敬开源与设计先锋",
                    subtitle = "每一段优雅的代码设计与每一次细腻的交互打磨，都在推动移动端极客生产力工具的演进。感谢所有为开发者生态默默贡献的探索者！",
                    position = CardPosition.SINGLE,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
