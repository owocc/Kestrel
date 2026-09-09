package com.bettershell.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Stop
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
import com.bettershell.app.ui.theme.AccentCyan

/**
 * 现代一体化 Chat/Work 模式输入栏 (ChatGPT / Multica Style)
 * - 支持 @ 文件引用动态候选面板
 * - 支持图片上传、粘贴预览与删除
 * - 支持工作路径 (CWD) 快速切换与指示
 */
@Composable
fun ChatIntegratedInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    activeAgent: DiscoveredAgent?,
    isAgentBusy: Boolean,
    isDark: Boolean,
    currentCwd: String = "~",
    availableFiles: List<String> = emptyList(),
    attachedImages: List<String> = emptyList(),
    onPickImage: () -> Unit = {},
    onRemoveImage: (Int) -> Unit = {},
    onOpenSelectAgent: () -> Unit,
    onOpenSelectModel: () -> Unit,
    onOpenSelectThinkingLevel: () -> Unit,
    onOpenSelectDirectory: () -> Unit = {},
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasText = text.isNotBlank() || attachedImages.isNotEmpty()
    var showPlusMenu by remember { mutableStateOf(false) }

    val containerBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val containerBorder = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    // @ 文件提及检测
    val atIndex = text.lastIndexOf('@')
    val isTypingMention = atIndex != -1 && !text.substring(atIndex + 1).contains(' ') && !text.substring(atIndex + 1).contains('\n')
    val mentionQuery = if (isTypingMention) text.substring(atIndex + 1) else null

    val matchingFiles = remember(availableFiles, mentionQuery) {
        if (mentionQuery == null) {
            emptyList()
        } else if (mentionQuery.isEmpty()) {
            availableFiles.take(6)
        } else {
            availableFiles.filter { it.contains(mentionQuery, ignoreCase = true) }.take(6)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        // 1. @ 文件提及候选浮层
        if (isTypingMention && matchingFiles.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF222222) else Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "选择项目文件 (@)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Text(
                            text = currentCwd,
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    matchingFiles.forEach { file ->
                        val isDir = file.endsWith("/")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val beforeAt = text.substring(0, atIndex)
                                    onTextChanged("$beforeAt@$file ")
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (isDir) LucideIcons.Folder else Icons.Rounded.Description,
                                contentDescription = null,
                                tint = if (isDir) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = file,
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 2. 输入胶囊外壳
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
                // 图片预览横向条 (如果有附加图片)
                if (attachedImages.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        attachedImages.forEachIndexed { idx, imgUri ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        LucideIcons.Image,
                                        contentDescription = "Attached Image",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // 删除按钮
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xCC000000))
                                        .clickable { onRemoveImage(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 上半部分：多行文本输入区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 32.dp, max = 130.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty() && attachedImages.isEmpty()) {
                        Text(
                            text = if (activeAgent != null) "询问 ${activeAgent.type.displayName}... 输入 @ 引用文件" else "输入消息，探讨代码与命令...",
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
                                    title = "选择工作目录",
                                    icon = LucideIcons.Folder,
                                    onClick = onOpenSelectDirectory
                                ),
                                OpenAiMenuItemData(
                                    title = "选择 Agent",
                                    icon = LucideIcons.Bot,
                                    onClick = onOpenSelectAgent
                                ),
                                OpenAiMenuItemData(
                                    title = "切换模型",
                                    icon = LucideIcons.Asteroid,
                                    onClick = onOpenSelectModel
                                ),
                                OpenAiMenuItemData(
                                    title = "思考程度",
                                    icon = LucideIcons.Brain,
                                    onClick = onOpenSelectThinkingLevel
                                ),
                                OpenAiMenuItemData(
                                    title = "上传图片",
                                    icon = LucideIcons.Image,
                                    onClick = onPickImage
                                ),
                                OpenAiMenuItemData(
                                    title = "引用项目文件",
                                    icon = LucideIcons.Paperclip,
                                    onClick = { onTextChanged("$text @") }
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

        // 3. 外部底部：当前工作目录与 Agent 模型的纯净状态展示条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：工作目录展示 (点击可直接触发切换)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onOpenSelectDirectory() }
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    LucideIcons.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = currentCwd.ifBlank { "~" },
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 右侧：Agent 与模型
            if (activeAgent != null) {
                val meta = SupportedAgentsCatalog.findMeta(activeAgent.command)
                val modelText = if (activeAgent.selectedModel.isNotBlank() && activeAgent.selectedModel != "default") {
                    activeAgent.selectedModel
                } else {
                    "默认模型"
                }

                Text(
                    text = "${meta.displayName} · $modelText",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    ),
                    maxLines = 1,
                    modifier = Modifier.clickable { onOpenSelectModel() }
                )
            }
        }
    }
}
