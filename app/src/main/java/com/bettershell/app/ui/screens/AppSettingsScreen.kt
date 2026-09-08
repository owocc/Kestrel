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
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.data.AppThemeMode
import com.bettershell.app.data.TerminalFont
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 软件全局设置页面 (严格按照用户截图 1 & 截图 2 的 OpenAI 极简色块规范):
 * - 规范化 StandardPageHeader (左侧圆钮返回、中间居中标题)
 * - 规范化 OpenAiSectionCard (大圆角卡片，内含细分割线条目行)
 * - 全局唯一的主题模式切换入口
 */
@Composable
fun AppSettingsScreen(
    prefsRepository: TerminalPreferencesRepository,
    onBack: () -> Unit
) {
    val prefs by prefsRepository.preferences.collectAsState()
    val isDark = when (prefs.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 规范化页头 (截图 2 规范)
        StandardPageHeader(
            title = "设置",
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
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 1. 主题与外观色块卡片 (对齐截图 1 规范)
            OpenAiSectionCard(
                headerTitle = "外观",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "深色模式",
                    subtitle = "经典极简深黑背景",
                    icon = Icons.Rounded.DarkMode,
                    trailingText = if (prefs.themeMode == AppThemeMode.DARK) "✓" else null,
                    isDark = isDark,
                    onClick = { prefsRepository.updateThemeMode(AppThemeMode.DARK) }
                )

                OpenAiSettingRow(
                    title = "浅色模式",
                    subtitle = "明亮清爽色块",
                    icon = Icons.Rounded.LightMode,
                    trailingText = if (prefs.themeMode == AppThemeMode.LIGHT) "✓" else null,
                    isDark = isDark,
                    onClick = { prefsRepository.updateThemeMode(AppThemeMode.LIGHT) }
                )

                OpenAiSettingRow(
                    title = "跟随系统",
                    subtitle = "自适应 Android 系统深浅色",
                    icon = Icons.Rounded.BrightnessAuto,
                    trailingText = if (prefs.themeMode == AppThemeMode.SYSTEM) "✓" else null,
                    showDivider = false,
                    isDark = isDark,
                    onClick = { prefsRepository.updateThemeMode(AppThemeMode.SYSTEM) }
                )
            }

            // 2. 终端默认字体色块卡片
            OpenAiSectionCard(
                headerTitle = "终端代码字体",
                isDark = isDark
            ) {
                TerminalFont.entries.forEachIndexed { index, font ->
                    OpenAiSettingRow(
                        title = font.displayName,
                        icon = Icons.Rounded.FontDownload,
                        trailingText = if (prefs.font == font) "✓" else null,
                        showDivider = index < TerminalFont.entries.lastIndex,
                        isDark = isDark,
                        onClick = { prefsRepository.updateFont(font) }
                    )
                }
            }

            // 3. 关于与协议
            OpenAiSectionCard(
                headerTitle = "关于",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "BetterShell",
                    subtitle = "v1.0 (Android 16 Ready) · 集成 omp 与 Multica",
                    icon = Icons.Rounded.Info,
                    showDivider = false,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
