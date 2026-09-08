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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.data.AppThemeMode
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 软件全局设置页面 (严格遵循 ChatGPT Remote 的独立卡片与动态圆角)
 */
@Composable
fun AppSettingsScreen(
    prefsRepository: TerminalPreferencesRepository,
    onNavigateToFontSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit
) {
    val prefs by prefsRepository.preferences.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "设置",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 1. 主题与外观 (三张卡片：TOP -> MIDDLE -> BOTTOM，无分割线，带纯净间隔)
            OpenAiSectionCard(
                headerTitle = "外观"
            ) {
                OpenAiSettingRow(
                    title = "深色模式",
                    subtitle = "经典极简深黑背景",
                    icon = Icons.Rounded.DarkMode,
                    trailingText = if (prefs.themeMode == AppThemeMode.DARK) "✓" else null,
                    position = CardPosition.TOP,
                    onClick = { prefsRepository.updateThemeMode(AppThemeMode.DARK) }
                )

                OpenAiSettingRow(
                    title = "浅色模式",
                    subtitle = "明亮清爽色块",
                    icon = Icons.Rounded.LightMode,
                    trailingText = if (prefs.themeMode == AppThemeMode.LIGHT) "✓" else null,
                    position = CardPosition.MIDDLE,
                    onClick = { prefsRepository.updateThemeMode(AppThemeMode.LIGHT) }
                )

                OpenAiSettingRow(
                    title = "跟随系统",
                    subtitle = "自适应 Android 系统主题",
                    icon = Icons.Rounded.Smartphone,
                    trailingText = if (prefs.themeMode == AppThemeMode.SYSTEM) "✓" else null,
                    position = CardPosition.BOTTOM,
                    onClick = { prefsRepository.updateThemeMode(AppThemeMode.SYSTEM) }
                )
            }

            // 2. 终端偏好 (单张卡片：全大圆角)
            OpenAiSectionCard(
                headerTitle = "终端偏好"
            ) {
                OpenAiSettingRow(
                    title = "代码字体",
                    subtitle = "自定义等宽字体渲染",
                    icon = LucideIcons.Terminal,
                    showChevron = true,
                    position = CardPosition.SINGLE,
                    onClick = onNavigateToFontSettings
                )
            }

            // 3. 关于页面 (单张卡片：全大圆角)
            OpenAiSectionCard(
                headerTitle = "关于"
            ) {
                OpenAiSettingRow(
                    title = com.bettershell.app.AppConstants.APP_NAME,
                    subtitle = com.bettershell.app.AppConstants.APP_VERSION_LABEL,
                    icon = LucideIcons.Bolt,
                    showChevron = true,
                    position = CardPosition.SINGLE,
                    onClick = onNavigateToAbout
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
