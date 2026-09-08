package com.bettershell.app.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

data class TerminalSpan(
    val text: String,
    val color: Color,
    val isBold: Boolean = false
)

class TerminalRow(
    val spans: MutableList<TerminalSpan> = mutableListOf()
) {
    fun rawText(): String = spans.joinToString("") { it.text }

    fun clear() {
        spans.clear()
    }

    fun append(text: String, color: Color, isBold: Boolean) {
        if (text.isEmpty()) return
        val last = spans.lastOrNull()
        if (last != null && last.color == color && last.isBold == isBold) {
            spans[spans.size - 1] = last.copy(text = last.text + text)
        } else {
            spans.add(TerminalSpan(text, color, isBold))
        }
    }

    fun overwriteFrom(startCol: Int, newText: String, color: Color, isBold: Boolean) {
        val currentStr = rawText()
        if (startCol >= currentStr.length) {
            // Pad with spaces if needed
            val spacesNeeded = startCol - currentStr.length
            if (spacesNeeded > 0) {
                append(" ".repeat(spacesNeeded), color, false)
            }
            append(newText, color, isBold)
            return
        }

        // Split existing line into before startCol and after overwritten part
        val before = currentStr.substring(0, startCol)
        val afterStart = startCol + newText.length
        val after = if (afterStart < currentStr.length) currentStr.substring(afterStart) else ""

        spans.clear()
        if (before.isNotEmpty()) {
            spans.add(TerminalSpan(before, TerminalColors.DEFAULT_TEXT_COLOR, false))
        }
        spans.add(TerminalSpan(newText, color, isBold))
        if (after.isNotEmpty()) {
            spans.add(TerminalSpan(after, TerminalColors.DEFAULT_TEXT_COLOR, false))
        }
    }
}

object TerminalColors {
    val COLOR_BLACK = Color(0xFF1E1E24)
    val COLOR_RED = Color(0xFFFF6B6B)
    val COLOR_GREEN = Color(0xFF51CF66)
    val COLOR_YELLOW = Color(0xFFFCC419)
    val COLOR_BLUE = Color(0xFF339AF0)
    val COLOR_MAGENTA = Color(0xFFCC5DE8)
    val COLOR_CYAN = Color(0xFF20C997)
    val COLOR_WHITE = Color(0xFFE9ECEF)

    val COLOR_BRIGHT_BLACK = Color(0xFF636E72)
    val COLOR_BRIGHT_RED = Color(0xFFFF8787)
    val COLOR_BRIGHT_GREEN = Color(0xFF69DB7C)
    val COLOR_BRIGHT_YELLOW = Color(0xFFFFD43B)
    val COLOR_BRIGHT_BLUE = Color(0xFF4DABF7)
    val COLOR_BRIGHT_MAGENTA = Color(0xFFDA77F2)
    val COLOR_BRIGHT_CYAN = Color(0xFF38D9A9)
    val COLOR_BRIGHT_WHITE = Color(0xFFFFFFFF)
    val DEFAULT_TEXT_COLOR = Color(0xFFD8DEE9)

    fun colorFromIndex(index: Int): Color {
        return when (index) {
            0 -> COLOR_BLACK
            1 -> COLOR_RED
            2 -> COLOR_GREEN
            3 -> COLOR_YELLOW
            4 -> COLOR_BLUE
            5 -> COLOR_MAGENTA
            6 -> COLOR_CYAN
            7 -> COLOR_WHITE
            8 -> COLOR_BRIGHT_BLACK
            9 -> COLOR_BRIGHT_RED
            10 -> COLOR_BRIGHT_GREEN
            11 -> COLOR_BRIGHT_YELLOW
            12 -> COLOR_BRIGHT_BLUE
            13 -> COLOR_BRIGHT_MAGENTA
            14 -> COLOR_BRIGHT_CYAN
            15 -> COLOR_BRIGHT_WHITE
            else -> DEFAULT_TEXT_COLOR
        }
    }
}

/**
 * High-performance virtual terminal screen buffer supporting in-place updates,
 * screen clears, carriage returns, cursor movements, and window title extraction.
 */
class TerminalScreenBuffer(
    private val maxScrollback: Int = 3000,
    private val onTitleChanged: ((String) -> Unit)? = null
) {
    private val rows = mutableListOf<TerminalRow>()
    private var cursorRow = 0
    private var cursorCol = 0

    private var currentColor = TerminalColors.DEFAULT_TEXT_COLOR
    private var currentBold = false

    init {
        rows.add(TerminalRow())
    }

    @Synchronized
    fun processBytes(data: String) {
        var i = 0
        val len = data.length

        while (i < len) {
            val c = data[i]

            // 1. Check for OSC sequence (\u001B]...)
            if (c == '\u001B' && i + 1 < len && data[i + 1] == ']') {
                val endBel = data.indexOf('\u0007', i + 2)
                val endEsc = data.indexOf("\u001B\\", i + 2)

                val endIndex = when {
                    endBel != -1 && endEsc != -1 -> minOf(endBel, endEsc)
                    endBel != -1 -> endBel
                    endEsc != -1 -> endEsc
                    else -> -1
                }

                if (endIndex != -1) {
                    val oscPayload = data.substring(i + 2, endIndex)
                    handleOsc(oscPayload)
                    i = if (endIndex == endEsc) endIndex + 2 else endIndex + 1
                    continue
                }
            }

            // 2. Check for CSI sequence (\u001B[...)
            if (c == '\u001B' && i + 1 < len && data[i + 1] == '[') {
                var j = i + 2
                while (j < len && (data[j] in '0'..'9' || data[j] == ';' || data[j] == '?' || data[j] == ' ')) {
                    j++
                }
                if (j < len && (data[j] in 'a'..'z' || data[j] in 'A'..'Z')) {
                    val finalChar = data[j]
                    val params = data.substring(i + 2, j)
                    handleCsi(finalChar, params)
                    i = j + 1
                    continue
                }
            }

            // 3. Simple escape or single character
            when (c) {
                '\r' -> {
                    // Carriage return: return cursor to start of current row (crucial for CLI in-place updates!)
                    cursorCol = 0
                    i++
                }
                '\n' -> {
                    // Line feed: advance to next row
                    cursorRow++
                    cursorCol = 0
                    ensureRowExists(cursorRow)
                    trimScrollbackIfNeeded()
                    i++
                }
                '\b' -> {
                    // Backspace
                    if (cursorCol > 0) cursorCol--
                    i++
                }
                '\u0007' -> {
                    // Bell - ignore
                    i++
                }
                '\u001B' -> {
                    // Stray escape character - skip
                    i++
                }
                else -> {
                    // Regular printable character
                    ensureRowExists(cursorRow)
                    val row = rows[cursorRow]

                    // Find text segment until next control character
                    val nextControl = data.indexOfAny(charArrayOf('\u001B', '\r', '\n', '\b', '\u0007'), i)
                    val end = if (nextControl != -1) nextControl else len
                    val textSegment = data.substring(i, end)

                    if (cursorCol == row.rawText().length) {
                        row.append(textSegment, currentColor, currentBold)
                    } else {
                        row.overwriteFrom(cursorCol, textSegment, currentColor, currentBold)
                    }

                    cursorCol += textSegment.length
                    i = end
                }
            }
        }
    }

    private fun handleOsc(payload: String) {
        // OSC 0;title or OSC 2;title sets window title
        if (payload.startsWith("0;") || payload.startsWith("2;")) {
            val title = payload.substring(2)
            if (title.isNotBlank()) {
                onTitleChanged?.invoke(title.trim())
            }
        }
    }

    private fun handleCsi(finalChar: Char, params: String) {
        when (finalChar) {
            'm' -> {
                // SGR Color & Styling
                handleSgr(params)
            }
            'J' -> {
                // Erase in display
                val mode = params.toIntOrNull() ?: 0
                when (mode) {
                    2, 3 -> {
                        // Clear entire screen!
                        rows.clear()
                        rows.add(TerminalRow())
                        cursorRow = 0
                        cursorCol = 0
                    }
                    0 -> {
                        // Clear from cursor to end of screen
                        if (cursorRow < rows.size) {
                            rows[cursorRow].overwriteFrom(cursorCol, "", currentColor, false)
                            while (rows.size > cursorRow + 1) {
                                rows.removeAt(rows.size - 1)
                            }
                        }
                    }
                    1 -> {
                        // Clear from start of screen to cursor
                        for (r in 0 until cursorRow) {
                            rows[r].clear()
                        }
                    }
                }
            }
            'K' -> {
                // Erase in line
                val mode = params.toIntOrNull() ?: 0
                ensureRowExists(cursorRow)
                when (mode) {
                    0 -> {
                        // Clear from cursor to end of line
                        val current = rows[cursorRow].rawText()
                        if (cursorCol < current.length) {
                            val kept = current.substring(0, cursorCol)
                            rows[cursorRow].clear()
                            if (kept.isNotEmpty()) {
                                rows[cursorRow].append(kept, currentColor, false)
                            }
                        }
                    }
                    1 -> {
                        // Clear from start to cursor
                        rows[cursorRow].overwriteFrom(0, " ".repeat(cursorCol), currentColor, false)
                    }
                    2 -> {
                        // Clear entire line
                        rows[cursorRow].clear()
                        cursorCol = 0
                    }
                }
            }
            'H', 'f' -> {
                // Cursor position: [row;colH (1-indexed)
                val parts = params.split(";")
                val targetRow = (parts.getOrNull(0)?.toIntOrNull() ?: 1) - 1
                val targetCol = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                cursorRow = maxOf(0, targetRow)
                cursorCol = maxOf(0, targetCol)
                ensureRowExists(cursorRow)
            }
            'A' -> {
                // Cursor Up
                val count = params.toIntOrNull() ?: 1
                cursorRow = maxOf(0, cursorRow - count)
            }
            'B' -> {
                // Cursor Down
                val count = params.toIntOrNull() ?: 1
                cursorRow += count
                ensureRowExists(cursorRow)
            }
            'C' -> {
                // Cursor Forward
                val count = params.toIntOrNull() ?: 1
                cursorCol += count
            }
            'D' -> {
                // Cursor Backward
                val count = params.toIntOrNull() ?: 1
                cursorCol = maxOf(0, cursorCol - count)
            }
            'G' -> {
                // Cursor to column
                val col = (params.toIntOrNull() ?: 1) - 1
                cursorCol = maxOf(0, col)
            }
        }
    }

    private fun handleSgr(params: String) {
        if (params.isEmpty()) {
            currentColor = TerminalColors.DEFAULT_TEXT_COLOR
            currentBold = false
            return
        }

        val codes = params.split(";").mapNotNull { it.toIntOrNull() }
        var idx = 0
        while (idx < codes.size) {
            when (val code = codes[idx]) {
                0 -> {
                    currentColor = TerminalColors.DEFAULT_TEXT_COLOR
                    currentBold = false
                }
                1 -> currentBold = true
                22 -> currentBold = false
                30, 31, 32, 33, 34, 35, 36, 37 -> {
                    currentColor = TerminalColors.colorFromIndex(code - 30)
                }
                39 -> currentColor = TerminalColors.DEFAULT_TEXT_COLOR
                90, 91, 92, 93, 94, 95, 96, 97 -> {
                    currentColor = TerminalColors.colorFromIndex(code - 90 + 8)
                }
                38 -> {
                    // Extended color: 38;5;index or 38;2;r;g;b
                    if (idx + 2 < codes.size && codes[idx + 1] == 5) {
                        val colorIndex = codes[idx + 2]
                        if (colorIndex in 0..15) {
                            currentColor = TerminalColors.colorFromIndex(colorIndex)
                        }
                        idx += 2
                    } else if (idx + 4 < codes.size && codes[idx + 1] == 2) {
                        val r = codes[idx + 2]
                        val g = codes[idx + 3]
                        val b = codes[idx + 4]
                        currentColor = Color(r, g, b)
                        idx += 4
                    }
                }
            }
            idx++
        }
    }

    private fun ensureRowExists(rowIndex: Int) {
        while (rows.size <= rowIndex) {
            rows.add(TerminalRow())
        }
    }

    private fun trimScrollbackIfNeeded() {
        if (rows.size > maxScrollback) {
            val removeCount = rows.size - maxScrollback
            repeat(removeCount) {
                if (rows.isNotEmpty()) {
                    rows.removeAt(0)
                    if (cursorRow > 0) cursorRow--
                }
            }
        }
    }

    @Synchronized
    fun toAnnotatedString(): AnnotatedString {
        return buildAnnotatedString {
            for (rowIndex in rows.indices) {
                val row = rows[rowIndex]
                for (span in row.spans) {
                    val style = SpanStyle(
                        color = span.color,
                        fontWeight = if (span.isBold) FontWeight.Bold else FontWeight.Normal
                    )
                    append(AnnotatedString(span.text, style))
                }
                if (rowIndex < rows.size - 1) {
                    append("\n")
                }
            }
        }
    }

    @Synchronized
    fun clear() {
        rows.clear()
        rows.add(TerminalRow())
        cursorRow = 0
        cursorCol = 0
        currentColor = TerminalColors.DEFAULT_TEXT_COLOR
        currentBold = false
    }
}
