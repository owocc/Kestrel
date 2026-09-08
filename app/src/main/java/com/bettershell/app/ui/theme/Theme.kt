package com.bettershell.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bettershell.app.data.AppThemeMode

/**
 * 全局统一设计系统标记：当前是否处于深色模式
 */
val LocalAppIsDark = staticCompositionLocalOf { true }

val isAppInDarkTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalAppIsDark.current

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF424242), // 与 ModeTogglePill 深色模式选中滑块完全一致的中灰 (0xFF424242)
    onPrimary = Color(0xFFF3F4F6),
    primaryContainer = Color(0xFF424242), // 聊天用户消息气泡、表单选中背景统一
    onPrimaryContainer = Color(0xFFF3F4F6),
    secondary = DarkSecondary,
    background = TerminalBlack,
    surface = Color(0xFF1E1E1E), // 严格对应统一 OpenAI 卡片深色背景
    surfaceVariant = Color(0xFF262626),
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFFFFF), // 与 ModeTogglePill 浅色模式选中滑块完全一致的纯白
    onPrimary = Color(0xFF111827),
    primaryContainer = Color(0xFFFFFFFF), // 聊天用户消息气泡、表单选中背景统一
    onPrimaryContainer = Color(0xFF111827),
    secondary = LightSecondary,
    background = TerminalLight,
    surface = Color(0xFFF3F4F6), // 严格对应统一 OpenAI 卡片浅色背景
    surfaceVariant = Color(0xFFE5E7EB),
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

@Composable
fun BetterShellTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bg = if (isDark) TerminalBlack else TerminalLight
            window.statusBarColor = bg.toArgb()
            window.navigationBarColor = bg.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    // 全局划词选中文本高亮与控制手柄色彩：
    // 深色模式下：白色半透明高亮选区 (#FFFFFF，alpha 0.35f)，手柄纯白
    // 浅色模式下：黑色半透明高亮选区 (#000000，alpha 0.20f)，手柄深黑
    val customSelectionColors = if (isDark) {
        TextSelectionColors(
            handleColor = Color(0xFFE5E7EB),
            backgroundColor = Color(0xFFFFFFFF).copy(alpha = 0.35f)
        )
    } else {
        TextSelectionColors(
            handleColor = Color(0xFF111827),
            backgroundColor = Color(0xFF000000).copy(alpha = 0.20f)
        )
    }

    CompositionLocalProvider(
        LocalAppIsDark provides isDark,
        LocalTextSelectionColors provides customSelectionColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
