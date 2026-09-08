package com.bettershell.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.WrapText
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.ServerRepository
import com.bettershell.app.data.TerminalFont
import com.bettershell.app.data.TerminalPreferences
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.terminal.AnsiParser
import com.bettershell.app.terminal.ConnectionState
import com.bettershell.app.terminal.TerminalSession
import com.bettershell.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    server: ServerConfig,
    repository: ServerRepository,
    prefsRepository: TerminalPreferencesRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val terminalPrefs by prefsRepository.preferences.collectAsState()
    var currentServer by remember { mutableStateOf(server) }

    val session = remember(currentServer.id) {
        TerminalSession(currentServer, coroutineScope)
    }

    DisposableEffect(session) {
        onDispose {
            session.disconnect()
        }
    }

    val connectionState by session.connectionState.collectAsState()
    val rawOutput by session.rawOutput.collectAsState()
    val history by session.history.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var showServerSettingsSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on output change
    LaunchedEffect(rawOutput) {
        if (rawOutput.isNotBlank()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation & Status Bar
            SessionTopBar(
                server = currentServer,
                connectionState = connectionState,
                softWrap = terminalPrefs.softWrap,
                onToggleSoftWrap = { prefsRepository.toggleSoftWrap() },
                onBack = onBack,
                onReconnect = { session.connect() },
                onClear = { session.clearScreen() },
                onOpenSettings = { showServerSettingsSheet = true }
            )

            // Terminal View Area (dark background with ANSI parsed monospace text)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SelectionContainer {
                    val annotatedOutput = remember(rawOutput) {
                        AnsiParser.parse(rawOutput)
                    }

                    val verticalScrollState = rememberScrollState()
                    val horizontalScrollState = rememberScrollState()

                    LaunchedEffect(rawOutput) {
                        verticalScrollState.animateScrollTo(verticalScrollState.maxValue)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScrollState)
                            .then(
                                if (!terminalPrefs.softWrap) Modifier.horizontalScroll(horizontalScrollState) else Modifier
                            )
                    ) {
                        Text(
                            text = annotatedOutput,
                            style = TextStyle(
                                fontFamily = terminalPrefs.font.toComposeFontFamily(),
                                fontSize = terminalPrefs.fontSizeSp.sp,
                                lineHeight = (terminalPrefs.fontSizeSp * terminalPrefs.lineSpacingMultiplier).sp,
                                color = Color(0xFFE5E7EB)
                            ),
                            softWrap = terminalPrefs.softWrap,
                            modifier = if (terminalPrefs.softWrap) Modifier.fillMaxWidth() else Modifier
                        )
                    }
                }
            }

            // Quick Shortcut Bar (ESC, TAB, CTRL-C, CTRL-D, Arrows)
            QuickShortcutBar(
                onSendRaw = { session.sendRaw(it) },
                onInsertText = { inputText += it }
            )

            // Expandable Bottom Input Panel (Exact match to Image #1)
            ExpandableInputPanel(
                text = inputText,
                onTextChanged = { inputText = it },
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded },
                onSend = {
                    if (inputText.isNotBlank()) {
                        session.sendCommand(inputText)
                        inputText = ""
                    }
                },
                onQuickPrompt = { prompt ->
                    inputText = prompt
                },
                onRunStartupScript = {
                    if (currentServer.startupScript.isNotBlank()) {
                        for (line in currentServer.startupScript.lines().filter { it.isNotBlank() && !it.startsWith("#") }) {
                            session.sendCommand(line)
                        }
                    }
                },
                hasStartupScript = currentServer.startupScript.isNotBlank()
            )
        }
    }

    // Server Settings Sheet (Configure startup script & options dynamically)
    if (showServerSettingsSheet) {
        ServerSettingsSheet(
            server = currentServer,
            prefsRepository = prefsRepository,
            onDismiss = { showServerSettingsSheet = false },
            onSave = { updated ->
                currentServer = updated
                coroutineScope.launch {
                    repository.updateServer(updated)
                }
                showServerSettingsSheet = false
            }
        )
    }
}

@Composable
fun SessionTopBar(
    server: ServerConfig,
    connectionState: ConnectionState,
    softWrap: Boolean,
    onToggleSoftWrap: () -> Unit,
    onBack: () -> Unit,
    onReconnect: () -> Unit,
    onClear: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(TerminalBlack)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Back button & Server Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkOnSurface
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkOnSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Connection Dot Status
                    val (dotColor, statusText) = when (connectionState) {
                        is ConnectionState.Connected -> AccentGreen to "已连接"
                        is ConnectionState.Connecting -> AccentOrange to "连接中"
                        is ConnectionState.Disconnected -> DarkOnSurfaceVariant to "未连接"
                        is ConnectionState.Error -> AccentRed to "错误"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(dotColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = dotColor
                        )
                    }
                }

                Text(
                    text = "${server.username}@${server.host}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = DarkOnSurfaceVariant
                )
            }
        }

        // Right Action buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = onToggleSoftWrap) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.WrapText,
                    contentDescription = if (softWrap) "禁用自动换行" else "启用自动换行",
                    tint = if (softWrap) AccentGreen else DarkOnSurfaceVariant
                )
            }
            if (connectionState is ConnectionState.Disconnected || connectionState is ConnectionState.Error) {
                IconButton(onClick = onReconnect) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reconnect",
                        tint = AccentGreen
                    )
                }
            }

            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = "Clear Terminal",
                    tint = DarkOnSurfaceVariant
                )
            }

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Server Settings",
                    tint = DarkOnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickShortcutBar(
    onSendRaw: (ByteArray) -> Unit,
    onInsertText: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickKeyChip(label = "ESC") { onSendRaw(byteArrayOf(27)) }
        QuickKeyChip(label = "TAB ⇥") { onSendRaw(byteArrayOf(9)) }
        QuickKeyChip(label = "Ctrl+C", isDanger = true) { onSendRaw(byteArrayOf(3)) }
        QuickKeyChip(label = "Ctrl+D") { onSendRaw(byteArrayOf(4)) }
        QuickKeyChip(label = "↑") { onSendRaw(byteArrayOf(27, 91, 65)) }
        QuickKeyChip(label = "↓") { onSendRaw(byteArrayOf(27, 91, 66)) }
        QuickKeyChip(label = "CLEAR") { onInsertText("clear\n") }
        QuickKeyChip(label = "|") { onInsertText(" | ") }
        QuickKeyChip(label = "~/") { onInsertText("~/") }
    }
}

@Composable
fun QuickKeyChip(
    label: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, if (isDanger) AccentRed.copy(alpha = 0.5f) else DarkOutline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDanger) AccentRed else DarkOnSurface
            )
        )
    }
}

/**
 * Expandable Input Panel
 * Strictly replicates Image #1:
 * - Collapsed: Floating rounded white card/pill at the bottom.
 * - Expanded: High white surface card with rounded top corners, large multi-line textarea, character counter, agent quick prompts, and action buttons.
 */
@Composable
fun ExpandableInputPanel(
    text: String,
    onTextChanged: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSend: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    onRunStartupScript: () -> Unit,
    hasStartupScript: Boolean
) {
    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) 480.dp else 68.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "panelHeight"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 22.dp,
        label = "panelCorner"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isExpanded) 0.dp else 16.dp,
                end = if (isExpanded) 0.dp else 16.dp,
                bottom = if (isExpanded) 0.dp else 12.dp,
                top = 4.dp
            )
            .height(animatedHeight)
            .shadow(
                elevation = if (isExpanded) 16.dp else 6.dp,
                shape = if (isExpanded) {
                    RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                } else {
                    RoundedCornerShape(cornerRadius)
                }
            ),
        shape = if (isExpanded) {
            RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        } else {
            RoundedCornerShape(cornerRadius)
        },
        color = InputPanelWhite,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isExpanded) {
                // Expanded View Header & Content
                ExpandedPanelContent(
                    text = text,
                    onTextChanged = onTextChanged,
                    onToggleExpand = onToggleExpand,
                    onSend = onSend,
                    onQuickPrompt = onQuickPrompt,
                    onRunStartupScript = onRunStartupScript,
                    hasStartupScript = hasStartupScript
                )
            } else {
                // Collapsed View (Single sleek horizontal row as in Image #1 left)
                CollapsedPanelContent(
                    text = text,
                    onTextChanged = onTextChanged,
                    onToggleExpand = onToggleExpand,
                    onSend = onSend
                )
            }
        }
    }
}

@Composable
fun CollapsedPanelContent(
    text: String,
    onTextChanged: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expand icon button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(InputPanelSurface)
                .clickable(onClick = onToggleExpand),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.OpenInFull,
                contentDescription = "Expand input",
                tint = InputTextDark,
                modifier = Modifier.size(18.dp)
            )
        }

        // Input Text Field
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(InputPanelSurface)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "输入指令发送到 Agent 终端...",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        color = InputTextSecondary
                    )
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = InputTextDark,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(InputTextDark),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
        }

        // Send Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank()) Color(0xFF111827) else Color(0xFFE5E7EB))
                .clickable(enabled = text.isNotBlank(), onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank()) Color.White else Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ExpandedPanelContent(
    text: String,
    onTextChanged: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onSend: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    onRunStartupScript: () -> Unit,
    hasStartupScript: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Drag Handle & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(InputPanelSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Terminal,
                        contentDescription = null,
                        tint = InputTextDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Agent 命令输入面板",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InputTextDark
                )
            }

            // Collapse Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(InputPanelSurface)
                    .clickable(onClick = onToggleExpand),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloseFullscreen,
                    contentDescription = "Collapse input",
                    tint = InputTextDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Prompt Templates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasStartupScript) {
                PromptChip(
                    label = "⚡ 运行启动脚本",
                    isHighlight = true,
                    onClick = onRunStartupScript
                )
            }
            PromptChip(label = "agent --status", onClick = { onQuickPrompt("agent --status") })
            PromptChip(label = "ls -la", onClick = { onQuickPrompt("ls -la") })
            PromptChip(label = "git status", onClick = { onQuickPrompt("git status") })
            PromptChip(label = "python3 agent.py", onClick = { onQuickPrompt("python3 agent.py") })
            PromptChip(label = "htop", onClick = { onQuickPrompt("htop") })
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Multi-line Editor
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(InputPanelSurface)
                .border(1.dp, InputDivider, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "在此编写多行 Agent 命令、Shell 脚本或输入交互内容...\n支持长文本编辑和快速发送到终端",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = InputTextSecondary
                    )
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = InputTextDark
                ),
                cursorBrush = SolidColor(InputTextDark)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Action Bar inside expanded panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text count & Clear
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${text.length} 字符 · ${text.lines().size} 行",
                    style = MaterialTheme.typography.bodySmall,
                    color = InputTextSecondary
                )

                if (text.isNotEmpty()) {
                    Text(
                        text = "清空",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentRed,
                        modifier = Modifier
                            .clickable { onTextChanged("") }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Prominent Send Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (text.isNotBlank()) Color(0xFF111827) else Color(0xFFE5E7EB))
                        .clickable(enabled = text.isNotBlank(), onClick = onSend)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "发送",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (text.isNotBlank()) Color.White else Color(0xFF9CA3AF)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = if (text.isNotBlank()) Color.White else Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromptChip(
    label: String,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHighlight) Color(0xFF111827) else InputPanelSurface)
            .border(
                1.dp,
                if (isHighlight) Color(0xFF111827) else InputDivider,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isHighlight) Color.White else InputTextDark
            )
        )
    }
}

/**
 * Server Settings Modal Sheet
 * Allows editing startup script and inspecting server details during live session
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSettingsSheet(
    server: ServerConfig,
    prefsRepository: TerminalPreferencesRepository,
    onDismiss: () -> Unit,
    onSave: (ServerConfig) -> Unit
) {
    val terminalPrefs by prefsRepository.preferences.collectAsState()
    var startupScript by remember { mutableStateOf(server.startupScript) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(DarkOutline)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "服务器设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnSurface
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, "Close", tint = DarkOnSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${server.name} (${server.username}@${server.host}:${server.port})",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = DarkOnSurfaceVariant
            )

            // Section 1: Terminal Display & Font Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FontDownload,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "终端字体与排版设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "选择字体 (包含完整 Nerd Font / Powerline 图标):",
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TerminalFont.entries.forEach { fontOption ->
                    val selected = terminalPrefs.font == fontOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) DarkPrimary else DarkSurfaceVariant)
                            .border(1.dp, if (selected) DarkPrimary else DarkOutline, RoundedCornerShape(10.dp))
                            .clickable { prefsRepository.updateFont(fontOption) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (fontOption) {
                                TerminalFont.JETBRAINS_MONO_NERD -> "JetBrains"
                                TerminalFont.FIRA_CODE_NERD -> "Fira Code"
                                TerminalFont.SYSTEM_MONOSPACE -> "系统等宽"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color(0xFF111827) else DarkOnSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Glyph Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalCardBg)
                    .border(1.dp, DarkOutline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "图标预览: ❯   src/  ● idle  ✔ pass  󰘬 main  λ ≠",
                    style = TextStyle(
                        fontFamily = terminalPrefs.font.toComposeFontFamily(),
                        fontSize = 12.sp,
                        color = AccentGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Font Size adjustment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "字体大小",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DarkOnSurface
                    )
                    Text(
                        text = "当前: ${terminalPrefs.fontSizeSp.toInt()} sp",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkOutline, CircleShape)
                            .clickable { prefsRepository.updateFontSize(terminalPrefs.fontSizeSp - 1f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = DarkOnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkOutline, CircleShape)
                            .clickable { prefsRepository.updateFontSize(terminalPrefs.fontSizeSp + 1f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = DarkOnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Soft Wrap switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, DarkOutline, RoundedCornerShape(12.dp))
                    .clickable { prefsRepository.toggleSoftWrap() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "自动换行 (Soft Wrap)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DarkOnSurface
                    )
                    Text(
                        text = if (terminalPrefs.softWrap) "已开启：长行强制折行" else "已关闭（推荐）：允许横向左右滑动，完整保留 CLI 表格与树形排版",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (terminalPrefs.softWrap) AccentOrange else AccentGreen
                    )
                }

                Switch(
                    checked = terminalPrefs.softWrap,
                    onCheckedChange = { prefsRepository.toggleSoftWrap() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = DarkOutline)
            Spacer(modifier = Modifier.height(14.dp))

            // Startup Script Editor
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "进入服务器自动运行代码 (Startup Script)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnSurface
                )
            }

            Text(
                text = "配置该服务器进入后运行的自定义 sh 脚本（例如激活虚拟环境、启动 Agent 会话等）：",
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = startupScript,
                onValueChange = { startupScript = it },
                placeholder = {
                    Text(
                        "# 编写自定义 sh 代码\ncd ~/agent\nsource venv/bin/activate\npython3 main.py",
                        fontFamily = FontFamily.Monospace,
                        color = DarkOnSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TerminalCardBg,
                    unfocusedContainerColor = TerminalCardBg,
                    focusedBorderColor = DarkPrimary,
                    unfocusedBorderColor = DarkOutline,
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("取消", color = DarkOnSurfaceVariant)
                }

                Spacer(modifier = Modifier.width(12.dp))

                androidx.compose.material3.Button(
                    onClick = {
                        onSave(server.copy(startupScript = startupScript))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color(0xFF111827)
                    )
                ) {
                    Text("保存配置", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
