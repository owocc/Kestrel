package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.terminal.AgentEventLogItem
import com.bettershell.app.ui.components.EventLogRowItem
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.isAppInDarkTheme
import compose.icons.TablerIcons
import compose.icons.tablericons.FileCode
import compose.icons.tablericons.List
enum class LogDisplayMode {
    STRUCTURED,
    RAW_TEXT
}

/**
 * 独立的日志详情页面 (Full Screen LogScreen)
 * - 顶部居中大标题“日志”，带有圆形返回键
 * - 右侧采用一个精致圆形按钮直接切换模式：
 *   - 当前为“结构化”时：显示代码/文档图标，点击切换为“纯文本”
 *   - 当前为“纯文本”时：显示列表/结构化图标，点击切换为“结构化”
 * - 界面极其干净整洁，不再有占位的顶部药丸标签栏！
 */
@Composable
fun LogScreen(
    rawLogs: String,
    eventLogs: List<AgentEventLogItem>,
    onBack: () -> Unit
) {
    val isDark = isAppInDarkTheme
    var selectedMode by remember { mutableStateOf(LogDisplayMode.STRUCTURED) }
    val containerBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)

    // 右侧按钮图标与提示：按需在 纯文本 和 结构化 之间一键切换
    val actionIcon = if (selectedMode == LogDisplayMode.STRUCTURED) {
        TablerIcons.FileCode // 结构化模式下，显示代码文件图标，点击切为纯文本
    } else {
        TablerIcons.List // 纯文本模式下，显示列表图标，点击切为结构化
    }
    val actionDesc = if (selectedMode == LogDisplayMode.STRUCTURED) "切换为纯文本日志" else "切换为结构化事件"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 1. 顶部 Header：右侧按钮一键切换图标与模式
        StandardPageHeader(
            title = if (selectedMode == LogDisplayMode.STRUCTURED) "日志 (结构化)" else "日志 (原始文本)",
            onBack = onBack,
            showSave = true,
            actionIcon = actionIcon,
            actionContentDescription = actionDesc,
            onSave = {
                selectedMode = if (selectedMode == LogDisplayMode.STRUCTURED) {
                    LogDisplayMode.RAW_TEXT
                } else {
                    LogDisplayMode.STRUCTURED
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. 内容呈现区域
        when (selectedMode) {
            LogDisplayMode.STRUCTURED -> {
                if (eventLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无结构化事件记录\nAgent 执行思考、调用工具与生成步骤时会在此处实时展现\n可点击右上角图标切换为纯文本原始日志",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(eventLogs, key = { it.id }) { item ->
                            EventLogRowItem(item = item)
                        }
                    }
                }
            }

            LogDisplayMode.RAW_TEXT -> {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    color = containerBg,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = rawLogs.ifBlank { "暂无原始日志输出..." },
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
