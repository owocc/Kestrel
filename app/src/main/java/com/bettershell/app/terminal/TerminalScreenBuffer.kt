package com.bettershell.app.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

data class TerminalCell(
    var char: Char = ' ',
    var fg: Color = TerminalColors.DEFAULT_TEXT_COLOR,
    var bg: Color = Color.Transparent,
    var isBold: Boolean = false
)

class TerminalRow {
    val cells = mutableListOf<TerminalCell>()

    fun length(): Int = cells.size

    fun clear() {
        cells.clear()
    }

    fun clearFrom(col: Int) {
        while (cells.size > col) {
            cells.removeAt(cells.size - 1)
        }
    }

    fun clearRange(fromCol: Int, toCol: Int) {
        val end = minOf(toCol, cells.size)
        for (c in fromCol until end) {
            cells[c].char = ' '
            cells[c].fg = TerminalColors.DEFAULT_TEXT_COLOR
            cells[c].bg = Color.Transparent
            cells[c].isBold = false
        }
    }

    fun setChar(col: Int, ch: Char, fg: Color, bg: Color, isBold: Boolean) {
        while (cells.size <= col) {
            cells.add(TerminalCell())
        }
        val cell = cells[col]
        cell.char = ch
        cell.fg = fg
        cell.bg = bg
        cell.isBold = isBold
    }

    fun writeString(startCol: Int, text: String, fg: Color, bg: Color, isBold: Boolean): Int {
        for (i in text.indices) {
            setChar(startCol + i, text[i], fg, bg, isBold)
        }
        return startCol + text.length
    }

    fun toAnnotatedString(isDark: Boolean = true): AnnotatedString {
        if (cells.isEmpty()) return AnnotatedString("")

        // Trim trailing spaces for clean layout
        var lastNonSpace = cells.size - 1
        while (lastNonSpace >= 0 && cells[lastNonSpace].char == ' ' && cells[lastNonSpace].bg == Color.Transparent) {
            lastNonSpace--
        }
        if (lastNonSpace < 0) return AnnotatedString("")

        val visibleLength = lastNonSpace + 1

        return buildAnnotatedString {
            var start = 0
            while (start < visibleLength) {
                val cell = cells[start]
                var end = start + 1
                while (end < visibleLength &&
                    cells[end].fg == cell.fg &&
                    cells[end].bg == cell.bg &&
                    cells[end].isBold == cell.isBold
                ) {
                    end++
                }

                val str = StringBuilder(end - start)
                for (k in start until end) {
                    str.append(cells[k].char)
                }

                val resolvedFg = TerminalColors.resolveFg(cell.fg, isDark)
                val resolvedBg = if (isDark) cell.bg else {
                    if (cell.bg == Color.Transparent) Color.Transparent else cell.bg.copy(alpha = 0.85f)
                }

                val style = SpanStyle(
                    color = resolvedFg,
                    background = resolvedBg,
                    fontWeight = if (cell.isBold) FontWeight.Bold else FontWeight.Normal
                )
                append(AnnotatedString(str.toString(), style))
                start = end
            }
        }
    }
}

object TerminalColors {
    // Standard 16 ANSI colors
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

    /**
     * Standard xterm 256-color palette
     * 0-15: Standard & Bright ANSI
     * 16-231: 6x6x6 RGB color cube
     * 232-255: 24 grayscale steps
     */
    fun colorFrom256(index: Int): Color {
        return when {
            index in 0..15 -> colorFromIndex(index)
            index in 16..231 -> {
                val c = index - 16
                val b = c % 6
                val g = (c / 6) % 6
                val r = c / 36

                val rVal = if (r == 0) 0 else 55 + r * 40
                val gVal = if (g == 0) 0 else 55 + g * 40
                val bVal = if (b == 0) 0 else 55 + b * 40
                Color(rVal, gVal, bVal)
            }
            index in 232..255 -> {
                val gray = 8 + (index - 232) * 10
                Color(gray, gray, gray)
            }
            else -> DEFAULT_TEXT_COLOR
        }
    }

    fun resolveFg(fg: Color, isDark: Boolean): Color {
        if (isDark) return fg
        return when (fg) {
            DEFAULT_TEXT_COLOR -> Color(0xFF1F2328)
            COLOR_WHITE -> Color(0xFF1F2328)
            COLOR_BRIGHT_WHITE -> Color(0xFF111827)
            COLOR_YELLOW -> Color(0xFFB45309)
            COLOR_BRIGHT_YELLOW -> Color(0xFFD97706)
            COLOR_CYAN -> Color(0xFF0E7490)
            COLOR_BRIGHT_CYAN -> Color(0xFF0891B2)
            COLOR_GREEN -> Color(0xFF15803D)
            COLOR_BRIGHT_GREEN -> Color(0xFF16A34A)
            else -> fg
        }
    }
}

/**
 * Modern Virtual Terminal Screen Buffer
 * - Cell-based grid with in-place updates (never wipes line colors on cursor rewrite)
 * - Complete ANSI 16-color, 256-color, and 24-bit TrueColor RGB support
 * - Foreground & Background color parsing
 * - In-place line rewriting with carriage returns (\r)
 * - Screen clear (\u001B[2J, \u001B[3J) & line clear (\u001B[K, \u001B[2K)
 * - Cursor positioning (\u001B[H, \u001B[row;colH)
 * - OSC window title extraction (\u001B]0;...\u0007, \u001B]2;...\u0007)
 */
class TerminalScreenBuffer(
    private val maxScrollback: Int = 3000,
    private val onTitleChanged: ((String) -> Unit)? = null
) {
    private val rows = mutableListOf<TerminalRow>()
    private var cursorRow = 0
    private var cursorCol = 0

    private var currentFg = TerminalColors.DEFAULT_TEXT_COLOR
    private var currentBg = Color.Transparent
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

            // 1. OSC sequence (\u001B]...)
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

            // 2. CSI sequence (\u001B[...)
            if (c == '\u001B' && i + 1 < len && data[i + 1] == '[') {
                var j = i + 2
                while (j < len && (data[j] in '0'..'9' || data[j] == ';' || data[j] == ':' || data[j] == '?' || data[j] == ' ')) {
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

            // 3. Characters & Control codes
            when (c) {
                '\r' -> {
                    // Carriage return: reset cursor to start of current line
                    cursorCol = 0
                    i++
                }
                '\n' -> {
                    // Line feed: advance to next line
                    cursorRow++
                    cursorCol = 0
                    ensureRowExists(cursorRow)
                    trimScrollbackIfNeeded()
                    i++
                }
                '\b' -> {
                    if (cursorCol > 0) cursorCol--
                    i++
                }
                '\t' -> {
                    // Tab: advance to next 8-column tab stop
                    val nextTab = (cursorCol / 8 + 1) * 8
                    ensureRowExists(cursorRow)
                    rows[cursorRow].writeString(cursorCol, " ".repeat(nextTab - cursorCol), currentFg, currentBg, currentBold)
                    cursorCol = nextTab
                    i++
                }
                '\u0007' -> {
                    i++ // Bell
                }
                '\u001B' -> {
                    i++ // Stray escape
                }
                else -> {
                    ensureRowExists(cursorRow)
                    val row = rows[cursorRow]

                    val nextControl = data.indexOfAny(charArrayOf('\u001B', '\r', '\n', '\b', '\t', '\u0007'), i)
                    val end = if (nextControl != -1) nextControl else len
                    val textSegment = data.substring(i, end)

                    cursorCol = row.writeString(cursorCol, textSegment, currentFg, currentBg, currentBold)
                    i = end
                }
            }
        }
    }

    private fun handleOsc(payload: String) {
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
                handleSgr(params)
            }
            'J' -> {
                val mode = params.toIntOrNull() ?: 0
                when (mode) {
                    2, 3 -> {
                        // Clear entire screen
                        rows.clear()
                        rows.add(TerminalRow())
                        cursorRow = 0
                        cursorCol = 0
                    }
                    0 -> {
                        // Clear from cursor to end of screen
                        if (cursorRow < rows.size) {
                            rows[cursorRow].clearFrom(cursorCol)
                            while (rows.size > cursorRow + 1) {
                                rows.removeAt(rows.size - 1)
                            }
                        }
                    }
                    1 -> {
                        // Clear from start to cursor
                        for (r in 0 until cursorRow) {
                            rows[r].clear()
                        }
                        if (cursorRow < rows.size) {
                            rows[cursorRow].clearRange(0, cursorCol)
                        }
                    }
                }
            }
            'K' -> {
                val mode = params.toIntOrNull() ?: 0
                ensureRowExists(cursorRow)
                when (mode) {
                    0 -> {
                        // Clear from cursor to end of line
                        rows[cursorRow].clearFrom(cursorCol)
                    }
                    1 -> {
                        // Clear from line start to cursor
                        rows[cursorRow].clearRange(0, cursorCol)
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
                val parts = params.split(";", ":")
                val targetRow = (parts.getOrNull(0)?.toIntOrNull() ?: 1) - 1
                val targetCol = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                cursorRow = maxOf(0, targetRow)
                cursorCol = maxOf(0, targetCol)
                ensureRowExists(cursorRow)
            }
            'A' -> {
                val count = params.toIntOrNull() ?: 1
                cursorRow = maxOf(0, cursorRow - count)
            }
            'B' -> {
                val count = params.toIntOrNull() ?: 1
                cursorRow += count
                ensureRowExists(cursorRow)
            }
            'C' -> {
                val count = params.toIntOrNull() ?: 1
                cursorCol += count
            }
            'D' -> {
                val count = params.toIntOrNull() ?: 1
                cursorCol = maxOf(0, cursorCol - count)
            }
            'G' -> {
                val col = (params.toIntOrNull() ?: 1) - 1
                cursorCol = maxOf(0, col)
            }
        }
    }

    private fun handleSgr(params: String) {
        if (params.isEmpty()) {
            resetSgr()
            return
        }

        // Support both semicolon ';' and colon ':' parameter delimiters
        val codes = params.replace(":", ";").split(";").mapNotNull { it.toIntOrNull() }
        var idx = 0

        while (idx < codes.size) {
            when (val code = codes[idx]) {
                0 -> {
                    resetSgr()
                }
                1 -> currentBold = true
                2 -> currentBold = false // Dim
                22 -> currentBold = false // Normal weight

                // Standard 8 Foreground colors
                in 30..37 -> {
                    currentFg = TerminalColors.colorFromIndex(code - 30)
                }
                39 -> currentFg = TerminalColors.DEFAULT_TEXT_COLOR

                // Extended Foreground Color (256-color or TrueColor RGB)
                38 -> {
                    if (idx + 2 < codes.size && codes[idx + 1] == 5) {
                        // 38;5;index (256-color palette)
                        val colorIndex = codes[idx + 2]
                        currentFg = TerminalColors.colorFrom256(colorIndex)
                        idx += 2
                    } else if (idx + 4 < codes.size && codes[idx + 1] == 2) {
                        // 38;2;r;g;b (24-bit TrueColor)
                        val r = codes[idx + 2].coerceIn(0, 255)
                        val g = codes[idx + 3].coerceIn(0, 255)
                        val b = codes[idx + 4].coerceIn(0, 255)
                        currentFg = Color(r, g, b)
                        idx += 4
                    }
                }

                // Standard 8 Background colors
                in 40..47 -> {
                    currentBg = TerminalColors.colorFromIndex(code - 40)
                }
                49 -> currentBg = Color.Transparent

                // Extended Background Color (256-color or TrueColor RGB)
                48 -> {
                    if (idx + 2 < codes.size && codes[idx + 1] == 5) {
                        // 48;5;index (256-color palette)
                        val colorIndex = codes[idx + 2]
                        currentBg = TerminalColors.colorFrom256(colorIndex)
                        idx += 2
                    } else if (idx + 4 < codes.size && codes[idx + 1] == 2) {
                        // 48;2;r;g;b (24-bit TrueColor)
                        val r = codes[idx + 2].coerceIn(0, 255)
                        val g = codes[idx + 3].coerceIn(0, 255)
                        val b = codes[idx + 4].coerceIn(0, 255)
                        currentBg = Color(r, g, b)
                        idx += 4
                    }
                }

                // Bright 8 Foreground colors
                in 90..97 -> {
                    currentFg = TerminalColors.colorFromIndex(code - 90 + 8)
                }

                // Bright 8 Background colors
                in 100..107 -> {
                    currentBg = TerminalColors.colorFromIndex(code - 100 + 8)
                }
            }
            idx++
        }
    }

    private fun resetSgr() {
        currentFg = TerminalColors.DEFAULT_TEXT_COLOR
        currentBg = Color.Transparent
        currentBold = false
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
    fun toAnnotatedString(isDark: Boolean = true): AnnotatedString {
        return buildAnnotatedString {
            for (rowIndex in rows.indices) {
                val row = rows[rowIndex]
                append(row.toAnnotatedString(isDark))
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
        resetSgr()
    }
}
