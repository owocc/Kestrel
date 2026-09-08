package com.bettershell.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.bettershell.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TerminalFont(val displayName: String) {
    JETBRAINS_MONO_NERD("JetBrains Mono (Nerd Font)"),
    FIRA_CODE_NERD("Fira Code (Nerd Font)"),
    SYSTEM_MONOSPACE("系统等宽 (System Monospace)");

    @Composable
    fun toComposeFontFamily(): FontFamily {
        return when (this) {
            JETBRAINS_MONO_NERD -> FontFamily(Font(R.font.jetbrains_mono_nerd))
            FIRA_CODE_NERD -> FontFamily(Font(R.font.fira_code_nerd))
            SYSTEM_MONOSPACE -> FontFamily.Monospace
        }
    }
}
enum class AppThemeMode(val displayName: String) {
    SYSTEM("跟随系统"),
    DARK("深色模式"),
    LIGHT("浅色模式")
}


data class TerminalPreferences(
    val font: TerminalFont = TerminalFont.JETBRAINS_MONO_NERD,
    val fontSizeSp: Float = 12f,
    val lineSpacingMultiplier: Float = 1.3f,
    val softWrap: Boolean = false, // 默认不换行，支持横向滚动保持 CLI 表格完整排版
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val serverLayoutMode: String = "LIST", // 首页服务器列表排列模式: LIST | GRID
    val cachedKeyboardHeightPx: Int = 0
)
class TerminalPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("terminal_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<TerminalPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): TerminalPreferences {
        val fontName = prefs.getString("font", TerminalFont.JETBRAINS_MONO_NERD.name)
            ?: TerminalFont.JETBRAINS_MONO_NERD.name
        val font = try {
            TerminalFont.valueOf(fontName)
        } catch (e: Exception) {
            TerminalFont.JETBRAINS_MONO_NERD
        }
        val fontSize = prefs.getFloat("font_size", 12f)
        val lineSpacing = prefs.getFloat("line_spacing", 1.3f)
        val softWrap = prefs.getBoolean("soft_wrap", false)

        val themeName = prefs.getString("theme_mode", AppThemeMode.DARK.name)
            ?: AppThemeMode.DARK.name
        val themeMode = try {
            AppThemeMode.valueOf(themeName)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
        val layoutMode = prefs.getString("server_layout_mode", "LIST") ?: "LIST"
        return TerminalPreferences(
            font = font,
            fontSizeSp = fontSize,
            lineSpacingMultiplier = lineSpacing,
            softWrap = softWrap,
            themeMode = themeMode,
            serverLayoutMode = layoutMode,
            cachedKeyboardHeightPx = prefs.getInt("cached_keyboard_height_px", 0)
        )
    }

    fun updateFont(font: TerminalFont) {
        prefs.edit().putString("font", font.name).apply()
        _preferences.value = _preferences.value.copy(font = font)
    }

    fun updateFontSize(sizeSp: Float) {
        val clamped = (Math.round(sizeSp * 10f) / 10f).coerceIn(8f, 26f)
        prefs.edit().putFloat("font_size", clamped).apply()
        _preferences.value = _preferences.value.copy(fontSizeSp = clamped)
    }

    fun updateThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun toggleSoftWrap() {
        val newValue = !_preferences.value.softWrap
        prefs.edit().putBoolean("soft_wrap", newValue).apply()
        _preferences.value = _preferences.value.copy(softWrap = newValue)
    }
    fun updateCachedKeyboardHeight(heightPx: Int) {
        if (heightPx > 200 && heightPx != _preferences.value.cachedKeyboardHeightPx) {
            prefs.edit().putInt("cached_keyboard_height_px", heightPx).apply()
            _preferences.value = _preferences.value.copy(cachedKeyboardHeightPx = heightPx)
        }
    }
    fun updateServerLayoutMode(mode: String) {
        prefs.edit().putString("server_layout_mode", mode).apply()
        _preferences.value = _preferences.value.copy(serverLayoutMode = mode)
    }
    fun updatePreferences(newPrefs: TerminalPreferences) {
        prefs.edit()
            .putString("font", newPrefs.font.name)
            .putFloat("font_size", newPrefs.fontSizeSp)
            .putFloat("line_spacing", newPrefs.lineSpacingMultiplier)
            .putBoolean("soft_wrap", newPrefs.softWrap)
            .apply()
        _preferences.value = newPrefs
    }
}
