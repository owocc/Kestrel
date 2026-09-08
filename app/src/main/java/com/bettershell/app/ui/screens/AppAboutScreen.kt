package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 独立的关于页面 (单独进入栈，享受全局预见式返回):
 * - 版本信息
 * - 协议与架构
 */
@Composable
fun AppAboutScreen(
    onBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "关于",
            onBack = onBack,
            isDark = isDark
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
                headerTitle = "应用信息",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "BetterShell",
                    subtitle = "版本: 1.0 (Android 16 Ready)",
                    showDivider = true,
                    isDark = isDark
                )

                OpenAiSettingRow(
                    title = "核心引擎",
                    subtitle = "omp (Oh My Pi) & Multica 多 Agent 架构",
                    showDivider = true,
                    isDark = isDark
                )

                OpenAiSettingRow(
                    title = "开源协议",
                    subtitle = "Apache License 2.0",
                    showDivider = false,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
