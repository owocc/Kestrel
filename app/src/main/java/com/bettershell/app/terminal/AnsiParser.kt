package com.bettershell.app.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object AnsiParser {

    private val ANSI_REGEX = Regex("\u001B\\[[0-9;?]*[a-zA-Z]")

    // Terminal 16-color palette (optimized for dark OLED terminal background)
    private val COLOR_BLACK = Color(0xFF1E1E24)
    private val COLOR_RED = Color(0xFFFF6B6B)
    private val COLOR_GREEN = Color(0xFF51CF66)
    private val COLOR_YELLOW = Color(0xFFFCC419)
    private val COLOR_BLUE = Color(0xFF339AF0)
    private val COLOR_MAGENTA = Color(0xFFCC5DE8)
    private val COLOR_CYAN = Color(0xFF20C997)
    private val COLOR_WHITE = Color(0xFFE9ECEF)

    private val COLOR_BRIGHT_BLACK = Color(0xFF636E72)
    private val COLOR_BRIGHT_RED = Color(0xFFFF8787)
    private val COLOR_BRIGHT_GREEN = Color(0xFF69DB7C)
    private val COLOR_BRIGHT_YELLOW = Color(0xFFFFD43B)
    private val COLOR_BRIGHT_BLUE = Color(0xFF4DABF7)
    private val COLOR_BRIGHT_MAGENTA = Color(0xFFDA77F2)
    private val COLOR_BRIGHT_CYAN = Color(0xFF38D9A9)
    private val COLOR_BRIGHT_WHITE = Color(0xFFFFFFFF)
    private val DEFAULT_TEXT_COLOR = Color(0xFFD8DEE9)

    fun parse(rawText: String): AnnotatedString {
        val cleanText = sanitize(rawText)
        return buildAnnotatedString {
            var currentIndex = 0
            var currentColor = DEFAULT_TEXT_COLOR
            var isBold = false

            val matches = ANSI_REGEX.findAll(cleanText).toList()

            for (match in matches) {
                // Append text before escape sequence
                if (match.range.first > currentIndex) {
                    val segment = cleanText.substring(currentIndex, match.range.first)
                    val style = SpanStyle(
                        color = currentColor,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                    )
                    append(AnnotatedString(segment, style))
                }

                // Process ANSI escape sequence
                val code = match.value
                if (code.endsWith("m")) {
                    val params = code.removePrefix("\u001B[")
                        .removeSuffix("m")
                        .split(";")
                        .mapNotNull { it.toIntOrNull() }

                    if (params.isEmpty() || params.contains(0)) {
                        // Reset
                        currentColor = DEFAULT_TEXT_COLOR
                        isBold = false
                    }

                    for (param in params) {
                        when (param) {
                            0 -> {
                                currentColor = DEFAULT_TEXT_COLOR
                                isBold = false
                            }
                            1 -> isBold = true
                            22 -> isBold = false // Normal intensity
                            30 -> currentColor = COLOR_BLACK
                            31 -> currentColor = COLOR_RED
                            32 -> currentColor = COLOR_GREEN
                            33 -> currentColor = COLOR_YELLOW
                            34 -> currentColor = COLOR_BLUE
                            35 -> currentColor = COLOR_MAGENTA
                            36 -> currentColor = COLOR_CYAN
                            37 -> currentColor = COLOR_WHITE
                            39 -> currentColor = DEFAULT_TEXT_COLOR
                            90 -> currentColor = COLOR_BRIGHT_BLACK
                            91 -> currentColor = COLOR_BRIGHT_RED
                            92 -> currentColor = COLOR_BRIGHT_GREEN
                            93 -> currentColor = COLOR_BRIGHT_YELLOW
                            94 -> currentColor = COLOR_BRIGHT_BLUE
                            95 -> currentColor = COLOR_BRIGHT_MAGENTA
                            96 -> currentColor = COLOR_BRIGHT_CYAN
                            97 -> currentColor = COLOR_BRIGHT_WHITE
                        }
                    }
                }

                currentIndex = match.range.last + 1
            }

            // Append remaining text
            if (currentIndex < cleanText.length) {
                val segment = cleanText.substring(currentIndex)
                val style = SpanStyle(
                    color = currentColor,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                )
                append(AnnotatedString(segment, style))
            }
        }
    }

    private val OSC_REGEX = Regex("\u001B\\][^\u0007\u001B]*(\u0007|\u001B\\\\)?")
    private val DCS_REGEX = Regex("\u001BP[^\u001B]*\u001B\\\\")
    private val CONTROL_SEQ_REGEX = Regex("\u001B\\[\\?[0-9;]*[a-zA-Z]")
    private val CURSOR_SEQ_REGEX = Regex("\u001B\\[[0-9;]*[HJKGsu]")
    private val CHARSET_SEQ_REGEX = Regex("\u001B[()][A-Za-z0-9]")
    private val ESC_2CHAR_REGEX = Regex("\u001B[=>78MEc]")

    private fun sanitize(input: String): String {
        return input.replace("\r\n", "\n")
            .replace("\r", "")
            .replace(OSC_REGEX, "")
            .replace(DCS_REGEX, "")
            .replace(CONTROL_SEQ_REGEX, "")
            .replace(CHARSET_SEQ_REGEX, "")
            .replace(ESC_2CHAR_REGEX, "")
            .replace(CURSOR_SEQ_REGEX, "")
    }
}
