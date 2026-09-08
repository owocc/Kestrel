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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.data.AppThemeMode
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 软件全局设置页面 (严格按照多列 OpenAI 分组):
 * - 外观主题切换 (唯一入口)
 * - 终端字体设置 (点击 push 进入 AppFontSettingsScreen 路由栈)
 * - 关于 (点击 push 进入 AppAboutScreen 路由栈)
 */
@Composable
fun AppSettingsScreen(
    prefsRepository: TerminalPreferencesRepository,
    onNavigateToFontSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit
) {
    val prefs by prefsRepository.preferences.collectAsState()
    val isDark = when (prefs.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
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
            // 1. 主题与外观 (去除染色，统一天然单色)
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

            // 2. 终端偏好 (点击进入单独路由页面)
            OpenAiSectionCard(
                headerTitle = "终端偏好",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "终端代码字体",
                    subtitle = prefs.font.displayName,
                    icon = Icons.Rounded.FontDownload,
                    showChevron = true,
                    showDivider = false,
                    isDark = isDark,
                    onClick = onNavigateToFontSettings
                )
            }

            // 3. 关于页面 (点击进入单独路由页面)
            OpenAiSectionCard(
                headerTitle = "关于",
                isDark = isDark
            ) {
                OpenAiSettingRow(
                    title = "关于 BetterShell",
                    subtitle = "v1.0 (Android 16 Ready)",
                    icon = Icons.Rounded.Info,
                    showChevron = true,
                    showDivider = false,
                    isDark = isDark,
                    onClick = onNavigateToAbout
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
