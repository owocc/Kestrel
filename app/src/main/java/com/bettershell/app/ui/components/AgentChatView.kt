package com.bettershell.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FindInPage
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.terminal.ChatMessage
import com.bettershell.app.terminal.ChatSender
import com.bettershell.app.terminal.ExecutionStep
import com.bettershell.app.terminal.StepType
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.AccentOrange

/**
 * 完整复刻 Multica 风格的高级 Agent 会话视窗
 * 包含：
 * 1. 呼吸态实时状态条 (Status Pill)
 * 2. 步骤追踪时间线折叠卡片 (Chat Timeline & Step Rows)
 * 3. 结果气泡与流式排版
 */
@Composable
fun AgentChatView(
    messages: List<ChatMessage>,
    isAgentBusy: Boolean,
    currentStatus: String?,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val halfScreenHeight = (configuration.screenHeightDp * 0.5f).dp
    val filteredMessages = remember(messages) {
        messages.filterNot { msg ->
            msg.sender == ChatSender.SYSTEM && (
                msg.content.trim().isEmpty() ||
                msg.content.trim() == ">" ||
                msg.content.startsWith(">") ||
                msg.content.startsWith("∙") ||
                msg.content.startsWith("•") ||
                msg.content.contains("[coco@") ||
                msg.content.contains("@omarchy") ||
                msg.content.contains("omp --mode") ||
                msg.content.contains("claude -p") ||
                msg.content.contains("codex exec") ||
                msg.content.contains("[Context of prior discussion") ||
                msg.content.contains("[Current user request]") ||
                (msg.content.contains("@") && (msg.content.contains("~ $") || msg.content.contains("~ ✗") || msg.content.contains("]$"))) ||
                msg.content.contains("CodeBuddy SDK") ||
                msg.content.contains("AskCodebuddy") ||
                msg.content.trim() == "'" ||
                msg.content.trim() == "''"
            )
        }
    }

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.lastIndex)
        }
    }

    SelectionContainer(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredMessages, key = { it.id }) { message ->
                MulticaMessageItem(message = message)
            }

            // 正在运行时的底部呼吸态 Status Pill
            if (isAgentBusy && currentStatus != null) {
                item {
                    MulticaStatusPill(status = currentStatus)
                }
            }

            // 底部始终保留 1/2 屏幕高度边距，防止内容被输入框与软键盘遮挡
            item {
                Spacer(modifier = Modifier.height(halfScreenHeight))
            }
        }
    }
}

@Composable
fun MulticaStatusPill(status: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .alpha(alpha)
                .background(AccentCyan)
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SystemMessageSection(message: ChatMessage) {
    var isExpanded by remember { mutableStateOf(false) }
    val isDark = com.bettershell.app.ui.theme.isAppInDarkTheme
    val rawText = message.content.trim()
    val isSingleShort = !rawText.contains('\n') && rawText.length <= 35

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (isSingleShort) {
            // 单行极简系统提示：居左轻量展现
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .padding(vertical = 3.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                )
                Text(
                    text = rawText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Normal
                )
            }
        } else {
            // 多行或长系统消息：和思考步骤一样居左收纳折叠！
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (isExpanded) "系统信息详情" else "系统信息 (${rawText.lines().firstOrNull()?.take(28) ?: "查看"}...)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF222222) else Color(0xFFF3F4F6),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = rawText,
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp),
                        color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun MulticaMessageItem(message: ChatMessage) {
    val isDark = com.bettershell.app.ui.theme.isAppInDarkTheme
    when (message.sender) {
        ChatSender.SYSTEM -> {
            SystemMessageSection(message = message)
        }
        ChatSender.USER -> {
            val rawContent = message.content
            val hasContextBlock = rawContent.contains("[Context of prior discussion in this session]:")
            val displayPrompt = if (hasContextBlock) {
                rawContent.substringAfter("[Current user request]:\n").trim().ifBlank {
                    rawContent.substringAfter("[Current user request]:").trim()
                }
            } else {
                rawContent
            }

            val contextSnippet = if (hasContextBlock) {
                rawContent.substringBefore("[Current user request]").trim()
            } else null

            var showContextDetails by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp
                    ),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF505050) else Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier.widthIn(max = 310.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        if (message.imageUris.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = if (displayPrompt.isNotBlank()) 6.dp else 0.dp)
                            ) {
                                message.imageUris.forEach { imgUri ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                LucideIcons.Image,
                                                contentDescription = null,
                                                tint = AccentCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "图片附件",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 核心指令直接显示
                        if (displayPrompt.isNotBlank()) {
                            Text(
                                text = displayPrompt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // 附带的历史上下文折叠起来，默认不直接展示
                        if (contextSnippet != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showContextDetails = !showContextDetails }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        if (showContextDetails) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (showContextDetails) "收起附带的对话上下文" else "已折叠附带的对话上下文",
                                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            if (showContextDetails) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = contextSnippet,
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        ChatSender.AGENT -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 如果有执行步骤（ChatTimeline 折叠栏）
                if (message.steps.isNotEmpty()) {
                    MulticaTimelineSection(
                        steps = message.steps,
                        isStreaming = message.isStreaming
                    )
                }

                // AI 输出直接铺开展示，去除任何对话头像和气泡外壳，干净通透
                if (message.content.isNotBlank()) {
                    SimpleMarkdownMessage(
                        content = message.content,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 对应 Multica 的 `ChatTimeline` 组件：
 * 默认在任务执行中自动展开显示 N 步细节；
 * 任务完成后折叠为一行精巧的 "N steps" 触发条。
 */
@Composable
fun MulticaTimelineSection(
    steps: List<ExecutionStep>,
    isStreaming: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) } // 默认折叠，保持界面干净清爽

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp) // 去除左侧边距，左侧平铺对齐
    ) {
        // Trigger Row: 类似 Multica 的 `chevron + 3 steps`
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )

            if (isStreaming) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AccentCyan)
                )
            }

            Text(
                text = "${steps.size} 步操作过程",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        // Fold Content: 步骤详情卡片列表
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.forEachIndexed { index, step ->
                        StepRowItem(step = step)
                        if (index < steps.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepRowItem(step: ExecutionStep) {
    val icon = when (step.toolName?.lowercase()) {
        "bash", "exec" -> Icons.Rounded.Terminal
        "read", "glob" -> Icons.Rounded.FindInPage
        "grep" -> Icons.Rounded.Search
        "write", "edit" -> Icons.Rounded.Edit
        else -> Icons.Rounded.Code
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (step.isRunning) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = AccentCyan
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(14.dp)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (step.isRunning) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (step.detail != null) {
                Text(
                    text = step.detail,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
        }
    }
}
