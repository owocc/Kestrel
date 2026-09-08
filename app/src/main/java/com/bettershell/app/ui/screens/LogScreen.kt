package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.terminal.AgentEventLogItem
import com.bettershell.app.ui.components.EventLogRowItem
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.isAppInDarkTheme

enum class LogDisplayMode(val label: String) {
    STRUCTURED("结构化事件"),
    RAW_TEXT("纯文本原始日志")
}

/**
 * 独立的日志详情页面 (Full Screen LogScreen)
 * - 顶部居中大标题“日志”，带有圆形返回键
 * - 顶部双模式切换胶囊 (结构化事件 / 纯文本原始日志)
 * - 结构化事件模式：渲染优雅的带时间戳与状态小胶囊的事件流卡片
 * - 纯文本模式：支持纵向与横向平滑滚动及全量长按自由选择复制 (SelectionContainer)
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
    val pillActiveBg = if (isDark) Color(0xFF2E2E2E) else Color(0xFFFFFFFF)
    val textColorActive = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textColorInactive = if (isDark) Color(0xFF8E8E93) else Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 1. 顶部规范化 Header
        StandardPageHeader(
            title = "日志",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. 顶部切换 Tab 胶囊 (对标 ModeTogglePill / ChatGPT 切换药丸)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = containerBg,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogDisplayMode.entries.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) pillActiveBg else Color.Transparent)
                                .clickable { selectedMode = mode }
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                style = TextStyle(
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) textColorActive else textColorInactive
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 内容呈现区域
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
                            text = "暂无结构化事件记录\nAgent 执行思考、调用工具与生成步骤时会在此处实时展现",
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
