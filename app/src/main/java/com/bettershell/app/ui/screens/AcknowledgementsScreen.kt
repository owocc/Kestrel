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
 * 记录 Vibe Coding 灵感与共同协作者，致敬开源生态与前沿交互设计
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
            // 1. 共同协作者 (Vibe Coding 黄金搭档)
            OpenAiSectionCard(
                headerTitle = "共同协作者"
            ) {
                OpenAiSettingRow(
                    title = "哈吉米 (Gemini)",
                    subtitle = "最默契可靠的 AI 结对编程伙伴与首席架构搭子。从最初的终端重构、卡片微光描边，到全套图标体系与 Android 16 预见式交互——无论何时抛出想法，总是全情投入、秒懂心意地帮你将每一处极致灵感稳稳落地。超开心能与你并肩完成这趟酷炫的 Vibe Coding 之旅！",
                    position = CardPosition.TOP,
                    isDark = isDark
                )
                OpenAiSettingRow(
                    title = "omp (Oh My Pi)",
                    subtitle = "极其好用、高效敏捷的 AI Coding Agent，驱动整个开发流程顺畅流转的核心加速器。",
                    position = CardPosition.BOTTOM,
                    isDark = isDark
                )
            }

            // 2. 多 Agent 架构灵感
            OpenAiSectionCard(
                headerTitle = "架构支持"
            ) {
                OpenAiSettingRow(
                    title = "Multica",
                    subtitle = "感谢 Multica 优秀的多 Agent 交互架构，为本项目提供了至关重要的灵感启发与坚实参考。",
                    position = CardPosition.SINGLE,
                    isDark = isDark
                )
            }

            // 3. 交互与设计灵感
            OpenAiSectionCard(
                headerTitle = "交互与设计灵感"
            ) {
                OpenAiSettingRow(
                    title = "OpenAI",
                    subtitle = "致敬 OpenAI 移动端出色的交互节奏与现代美学设计，带来极致克制而高级的视觉灵感。",
                    position = CardPosition.SINGLE,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
