package com.bettershell.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog

/**
 * 现代一体化 Chat/Work 模式输入栏 (ChatGPT Style)
 * - 输入框整体距离底部充分抬高
 * - 输入框内部保留纯粹的 '+' 操作按钮与圆形发送箭头
 * - 当前生效的 Agent 与模型移至输入框外部底部，仅作为纯净状态展示
 */
@Composable
fun ChatIntegratedInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    activeAgent: DiscoveredAgent?,
    isAgentBusy: Boolean,
    isDark: Boolean,
    onOpenSelectAgent: () -> Unit,
    onOpenSelectThinkingLevel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasText = text.isNotBlank()
    var showPlusMenu by remember { mutableStateOf(false) }

    val containerBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val containerBorder = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 12.dp) // 距离底部充分抬高！
    ) {
        // 1. 输入胶囊外壳
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = containerBg,
            border = BorderStroke(1.dp, containerBorder),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // 上半部分：多行文本输入区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 32.dp, max = 130.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = if (activeAgent != null) "询问 ${activeAgent.type.displayName}..." else "输入消息，探讨代码与命令...",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(if (isDark) Color.White else Color(0xFF10A37F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 下半部分：左侧 '+' 按钮，右侧发送箭头
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // '+' 按钮与对齐左下角的 Popover 菜单
                    Box {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5E7EB))
                                .clickable { showPlusMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "操作与切换",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        OpenAiDropdownMenu(
                            expanded = showPlusMenu,
                            onDismissRequest = { showPlusMenu = false },
                            alignment = Alignment.BottomStart,
                            offset = DpOffset(x = 0.dp, y = (-42).dp),
                            transformOrigin = TransformOrigin(pivotFractionX = 0f, pivotFractionY = 1f),
                            width = 220.dp,
                            isDark = isDark,
                            items = listOf(
                                OpenAiMenuItemData(
                                    title = "选择 Agent",
                                    icon = LucideIcons.Bot,
                                    onClick = onOpenSelectAgent
                                ),
                                OpenAiMenuItemData(
                                    title = "思考程度",
                                    icon = LucideIcons.Brain,
                                    onClick = onOpenSelectThinkingLevel
                                ),
                                OpenAiMenuItemData(
                                    title = "插入文件路径",
                                    icon = LucideIcons.Paperclip,
                                    onClick = { onTextChanged(text + " @") }
                                ),
                                OpenAiMenuItemData(
                                    title = "图片与快照",
                                    icon = LucideIcons.Image,
                                    onClick = { onTextChanged(text + " ") }
                                )
                            )
                        )
                    }

                    // 右侧操作按钮 (绿色向上箭头 / 红色停止)
                    val sendBtnBg by animateColorAsState(
                        targetValue = when {
                            isAgentBusy -> Color(0xFFEF4444)
                            hasText -> Color(0xFF10A37F)
                            else -> if (isDark) Color(0xFF333333) else Color(0xFFD1D5DB)
                        },
                        label = "sendBtnBg"
                    )

                    val sendBtnTint by animateColorAsState(
                        targetValue = when {
                            isAgentBusy -> Color.White
                            hasText -> Color.White
                            else -> if (isDark) Color(0xFF737373) else Color(0xFF9CA3AF)
                        },
                        label = "sendBtnTint"
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(sendBtnBg)
                            .clickable(enabled = hasText || isAgentBusy) {
                                onSend()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAgentBusy) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = "Stop",
                                tint = sendBtnTint,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = sendBtnTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. 外部底部：当前 Agent 和模型的纯净状态展示条 (只用于展示状态)
        if (activeAgent != null) {
            val meta = SupportedAgentsCatalog.findMeta(activeAgent.command)
            val modelText = if (activeAgent.selectedModel.isNotBlank() && activeAgent.selectedModel != "default") {
                activeAgent.selectedModel
            } else {
                "默认模型"
            }
            val thinkingText = activeAgent.thinkingLevel.displayName

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 7.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 呼吸/活跃圆点指示灯
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isAgentBusy) Color(0xFF10B981) else Color(0xFF9CA3AF).copy(alpha = 0.6f))
                )

                Spacer(modifier = Modifier.size(6.dp))

                Text(
                    text = "${meta.displayName} · $modelText · $thinkingText",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
