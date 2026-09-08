package com.bettershell.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.ui.theme.AccentCyan

/**
 * 现代轻量 Markdown 消息渲染器
 * 支持：代码块（```code``` 带暗色背景和 Monospace）、行内代码（`code`）、粗体（**bold**）、标题（### ）、引用等
 */
@Composable
fun SimpleMarkdownMessage(
    content: String,
    modifier: Modifier = Modifier
) {
    val blocks = parseMarkdownBlocks(content)

    Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF18181B) // 深色代码背景
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (block.language.isNotBlank()) {
                                Text(
                                    text = block.language,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFA1A1AA),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = block.code,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFE4E4E7)
                                )
                            )
                        }
                    }
                }
                is MarkdownBlock.HeaderBlock -> {
                    Text(
                        text = block.text,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                is MarkdownBlock.TextBlock -> {
                    Text(
                        text = buildAnnotatedInlineMarkdown(block.text),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

sealed interface MarkdownBlock {
    data class TextBlock(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class HeaderBlock(val level: Int, val text: String) : MarkdownBlock
}

private fun parseMarkdownBlocks(content: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = content.lines()
    var i = 0
    val textBuffer = StringBuilder()

    fun flushText() {
        if (textBuffer.isNotBlank()) {
            blocks.add(MarkdownBlock.TextBlock(textBuffer.toString().trim()))
            textBuffer.clear()
        }
    }

    while (i < lines.size) {
        val line = lines[i]
        if (line.trim().startsWith("```")) {
            flushText()
            val lang = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            i++
        } else if (line.trim().startsWith("#")) {
            flushText()
            val trimmed = line.trim()
            val level = trimmed.takeWhile { it == '#' }.length
            val headerText = trimmed.drop(level).trim()
            blocks.add(MarkdownBlock.HeaderBlock(level, headerText))
            i++
        } else {
            textBuffer.append(line).append("\n")
            i++
        }
    }
    flushText()
    return blocks
}

private fun buildAnnotatedInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            // 处理行内代码 `code`
            val codeStart = text.indexOf('`', cursor)
            // 处理粗体 **bold**
            val boldStart = text.indexOf("**", cursor)

            val nextSpecial = listOfNotNull(
                if (codeStart != -1) codeStart else null,
                if (boldStart != -1) boldStart else null
            ).minOrNull()

            if (nextSpecial == null) {
                append(text.substring(cursor))
                break
            }

            append(text.substring(cursor, nextSpecial))

            if (nextSpecial == codeStart) {
                val codeEnd = text.indexOf('`', codeStart + 1)
                if (codeEnd != -1) {
                    val codeContent = text.substring(codeStart + 1, codeEnd)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF27272A),
                            color = Color(0xFFF43F5E),
                            fontSize = 13.sp
                        )
                    )
                    append(" $codeContent ")
                    pop()
                    cursor = codeEnd + 1
                } else {
                    append("`")
                    cursor = codeStart + 1
                }
            } else if (nextSpecial == boldStart) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    val boldContent = text.substring(boldStart + 2, boldEnd)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldContent)
                    pop()
                    cursor = boldEnd + 2
                } else {
                    append("**")
                    cursor = boldStart + 2
                }
            }
        }
    }
}
