package com.bettershell.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.isAppInDarkTheme
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

/**
 * 基于 mikepenz/multiplatform-markdown-renderer-m3 (JetBrains Markdown AST) 的专业 Markdown 渲染器
 * 完整支持：
 * 1. 标题 (H1-H6)
 * 2. 粗体、斜体、删除线、下划线
 * 3. 代码块（带暗色/浅色优雅圆角背景与 Monospace 代码字体）
 * 4. 行内代码（带轻量背景与精致着色）
 * 5. 无序与有序列表、多层嵌套列表、任务清单
 * 6. 引用块 (Blockquote)
 * 7. 超链接 (带自动点击跳转与下划线)
 * 8. GFM 完整表格渲染
 */
@Composable
fun SimpleMarkdownMessage(
    content: String,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme

    // 自适应主题的专业配色体系
    val colors = markdownColor(
        text = MaterialTheme.colorScheme.onSurface,
        codeBackground = if (isDark) Color(0xFF18181B) else Color(0xFFF4F4F5),
        inlineCodeBackground = if (isDark) Color(0xFF27272A) else Color(0xFFE4E4E7),
        dividerColor = if (isDark) Color(0xFF27272A) else Color(0xFFE4E4E7),
        tableBackground = if (isDark) Color(0xFF18181B) else Color(0xFFF4F4F5)
    )
    // 精致清晰的 Typography 配置
    val typography = markdownTypography(
        h1 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
        h2 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
        h3 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        h4 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        h5 = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        h6 = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
        text = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, fontSize = 14.sp),
        paragraph = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, fontSize = 14.sp),
        code = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = if (isDark) Color(0xFFE4E4E7) else Color(0xFF18181B)
        ),
        inlineCode = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = if (isDark) Color(0xFFF43F5E) else Color(0xFFE11D48)
        ),
        link = MaterialTheme.typography.bodyMedium.copy(
            color = AccentCyan,
            fontSize = 14.sp
        ),
        quote = MaterialTheme.typography.bodyMedium.copy(
            fontStyle = FontStyle.Italic,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        list = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp, fontSize = 14.sp),
        bullet = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp, fontSize = 14.sp),
        ordered = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp, fontSize = 14.sp)
    )

    Markdown(
        content = content,
        colors = colors,
        typography = typography,
        modifier = modifier.fillMaxWidth()
    )
}
