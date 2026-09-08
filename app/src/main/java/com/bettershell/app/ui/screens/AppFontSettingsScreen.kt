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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.data.TerminalFont
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 终端代码字体独立设置页面 (完全压入路由栈，享受全局预见式返回与平行滑动):
 * - 纯文本排列，无任何额外图标 (对标用户指令: 字体选项不需要图标，直接显示文本)
 * - 右侧显示选中状态勾勾
 */
@Composable
fun AppFontSettingsScreen(
    prefsRepository: TerminalPreferencesRepository,
    onBack: () -> Unit
) {
    val prefs by prefsRepository.preferences.collectAsState()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "终端代码字体",
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
                headerTitle = "可用等宽字体",
                isDark = isDark
            ) {
                TerminalFont.entries.forEachIndexed { index, font ->
                    OpenAiSettingRow(
                        title = font.displayName,
                        icon = null, // 无图标，直接显示文本
                        trailingText = if (prefs.font == font) "✓" else null,
                        showDivider = index < TerminalFont.entries.lastIndex,
                        isDark = isDark,
                        onClick = { prefsRepository.updateFont(font) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
