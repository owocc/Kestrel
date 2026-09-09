package com.bettershell.app.ui.theme

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
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
    primary = Color(0xFF0284C7), // 优雅深天蓝青 (Sky 600)，彻底解决原先使用纯白导致手柄和选中高光隐形的问题
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3F4F6),
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
            val activity = view.context as? ComponentActivity
            if (activity != null) {
                val statusBarStyle = if (isDark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                activity.enableEdgeToEdge(
                    statusBarStyle = statusBarStyle,
                    navigationBarStyle = statusBarStyle
                )
            }

            val window = (view.context as? Activity)?.window
            if (window != null) {
                val solidColor = if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                window.statusBarColor = solidColor
                window.navigationBarColor = solidColor
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    // 全局划词选中文本高亮与控制手柄色彩：
    // 浅色模式：深天蓝青水滴手柄 (#0284C7)，柔和天蓝青半透明高光 (#38BDF8，alpha 0.28f)，在纯白底色上黑字极其清晰
    // 深色模式：高亮科技青手柄 (AccentCyan #06B6D4)，半透明青蓝选区高光 (alpha 0.35f)，在纯黑底色上白字极其清晰
    val customSelectionColors = if (isDark) {
        TextSelectionColors(
            handleColor = AccentCyan,
            backgroundColor = AccentCyan.copy(alpha = 0.35f)
        )
    } else {
        TextSelectionColors(
            handleColor = Color(0xFF0284C7),
            backgroundColor = Color(0xFF38BDF8).copy(alpha = 0.28f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography
    ) {
        CompositionLocalProvider(
            LocalAppIsDark provides isDark,
            LocalTextSelectionColors provides customSelectionColors,
            content = content
        )
    }
}
