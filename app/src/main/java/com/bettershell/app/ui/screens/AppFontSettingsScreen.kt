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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.data.TerminalFont
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 终端代码字体独立设置页面 (对标 ChatGPT Remote 截图的分段独立圆角卡片)
 */
@Composable
fun AppFontSettingsScreen(
    prefsRepository: TerminalPreferencesRepository,
    onBack: () -> Unit
) {
    val prefs by prefsRepository.preferences.collectAsState()
    val fonts = TerminalFont.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "终端代码字体",
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
                headerTitle = "可用等宽字体"
            ) {
                fonts.forEachIndexed { index, font ->
                    val isSelected = prefs.font == font
                    val position = when (index) {
                        0 -> CardPosition.TOP
                        fonts.size - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }

                    OpenAiSettingRow(
                        title = font.displayName,
                        subtitle = if (isSelected) "当前正在使用" else null,
                        trailingText = if (isSelected) "✓" else null,
                        position = position,
                        onClick = { prefsRepository.updateFont(font) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
