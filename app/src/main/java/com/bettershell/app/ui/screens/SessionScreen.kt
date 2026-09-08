package com.bettershell.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.bettershell.app.ui.components.LucideEllipsis
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.AppThemeMode
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.ServerRepository
import com.bettershell.app.data.TerminalFont
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.terminal.ConnectionState
import com.bettershell.app.terminal.ServerSessionItem
import com.bettershell.app.terminal.SessionManager
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.AccentOrange
import com.bettershell.app.ui.theme.AccentRed
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.bettershell.app.agent.AgentDiscoveryRepository
import com.bettershell.app.agent.AgentProbeScript
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.UniversalAgentRunner
import com.bettershell.app.ui.components.AgentPickerBottomSheet
import com.bettershell.app.ui.components.ChatIntegratedInputBar
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiDropdownMenu
import com.bettershell.app.ui.components.OpenAiMenuItemData
import com.bettershell.app.ui.components.UnifiedServerSettingsBottomSheet
import com.bettershell.app.terminal.OmpAgentClient
import com.bettershell.app.ui.components.AgentLogsBottomSheet
import com.bettershell.app.ui.components.AgentChatView
import com.bettershell.app.ui.components.ModeTogglePill
import com.bettershell.app.ui.components.SessionMode
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SessionScreen(
    server: ServerConfig,
    repository: ServerRepository,
    sessionManager: SessionManager,
    prefsRepository: TerminalPreferencesRepository,
    onOpenRawLogs: (logs: String) -> Unit = {},
    onOpenServerSettings: () -> Unit = {},
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentServer by remember { mutableStateOf(server) }

    val sessionsMap by sessionManager.sessionsMap.collectAsState()
    val activeSessionMap by sessionManager.activeSessionMap.collectAsState()

    val serverSessions = remember(sessionsMap, currentServer.id) {
        sessionsMap[currentServer.id] ?: emptyList()
    }

    val activeSessionItem = remember(serverSessions, activeSessionMap, currentServer.id) {
        sessionManager.getActiveSession(currentServer.id)
            ?: sessionManager.getOrCreateInitialSession(currentServer)
    }
    val shellTerminalSession = activeSessionItem.terminalSession
    val shellConnectionState by shellTerminalSession.connectionState.collectAsState()
    val shellRawOutput by shellTerminalSession.annotatedOutput.collectAsState()
    val terminalPrefs by prefsRepository.preferences.collectAsState()

    val isDark = when (terminalPrefs.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val displayOutput = remember(shellRawOutput, isDark) {
        shellTerminalSession.getAnnotatedOutput(isDark)
    }

    // Chat 专属独立会话 (与 Shell 彻底隔离)
    val chatSessionItem = remember(currentServer.id) {
        sessionManager.getOrCreateChatSession(currentServer)
    }
    val chatTerminalSession = chatSessionItem.terminalSession
    val chatRawOutput by chatTerminalSession.annotatedOutput.collectAsState()
    val chatConnectionState by chatTerminalSession.connectionState.collectAsState()

    var currentMode by remember { mutableStateOf(SessionMode.SHELL) }
    var inputText by remember { mutableStateOf("") }
    var isExpandedInput by remember { mutableStateOf(false) }
    var showServerSettingsSheet by remember { mutableStateOf(false) }
    var showSessionSwitcherSheet by remember { mutableStateOf(false) }
    var showAgentLogsSheet by remember { mutableStateOf(false) }
    var sessionToRename by remember { mutableStateOf<ServerSessionItem?>(null) }
    var showFontSizeIndicator by remember { mutableStateOf(false) }
    var indicatorDismissJob by remember { mutableStateOf<Job?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val agentDiscoveryRepo = remember { AgentDiscoveryRepository(context) }

    var discoveredAgents by remember(currentServer.id) {
        mutableStateOf(agentDiscoveryRepo.getCachedAgents(currentServer.id))
    }
    val selectedAgentId = remember(currentServer.id) {
        agentDiscoveryRepo.getSelectedAgentId(currentServer.id)
    }
    val initialAgent = remember(discoveredAgents, selectedAgentId) {
        discoveredAgents.find { it.id == selectedAgentId } ?: discoveredAgents.firstOrNull()
    }

    val agentRunner = remember(currentServer.id) {
        UniversalAgentRunner(
            initialAgent = initialAgent,
            sendRawCommand = { cmd -> chatTerminalSession.sendCommand(cmd) }
        )
    }
    val activeAgent by agentRunner.currentAgent.collectAsState()
    val chatMessages by agentRunner.messages.collectAsState()
    val isAgentBusy by agentRunner.isBusy.collectAsState()
    val currentAgentStatus by agentRunner.currentStatus.collectAsState()
    val agentRawLogs by agentRunner.rawLogs.collectAsState()
    val agentEventLogs by agentRunner.eventLogs.collectAsState()
    var showAgentPickerSheet by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }

    // 仅监听 Chat 管道解析 JSONL 执行流 (扫描已全量移交首页服务器管理)
    LaunchedEffect(chatTerminalSession) {
        chatTerminalSession.rawChunkFlow.collect { chunk ->
            agentRunner.onRemoteChunk(chunk)
        }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    var showToolsSheet by remember { mutableStateOf(false) }
    val navBottomDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val isImeVisible = WindowInsets.isImeVisible
    val bottomNavPadding = if (isImeVisible) 0.dp else navBottomDp
    // Hierarchical back handling
    val hasLocalOverlay = sessionToRename != null ||
            showSessionSwitcherSheet ||
            showServerSettingsSheet ||
            showToolsSheet ||
            showAgentPickerSheet ||
            isExpandedInput ||
            isImeVisible

    // 仅在存在局部弹窗或软键盘时启用局部 BackHandler 优先拦截关闭弹窗；无局部状态时放行给全局 PredictiveBack
    BackHandler(enabled = hasLocalOverlay) {
        when {
            sessionToRename != null -> sessionToRename = null
            showSessionSwitcherSheet -> showSessionSwitcherSheet = false
            showServerSettingsSheet -> showServerSettingsSheet = false
            showToolsSheet -> showToolsSheet = false
            showAgentPickerSheet -> showAgentPickerSheet = false
            isExpandedInput -> isExpandedInput = false
            isImeVisible -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }

    // Root Container: Layered architecture (Shell layer + Floating Input Layer)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
        // Layer 1: Shell & Terminal Layer (Underneath)
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation & Status Bar with Session Switching & Theme Toggle
            SessionTopBar(
                server = currentServer,
                activeSession = activeSessionItem,
                connectionState = if (currentMode == SessionMode.CHAT) chatConnectionState else shellConnectionState,
                currentMode = currentMode,
                onModeSelected = { currentMode = it },
                softWrap = terminalPrefs.softWrap,
                isDark = isDark,
                onToggleSoftWrap = { prefsRepository.toggleSoftWrap() },
                onToggleTheme = {
                    val nextMode = if (isDark) AppThemeMode.LIGHT else AppThemeMode.DARK
                    prefsRepository.updateThemeMode(nextMode)
                },
                onBack = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onBack()
                },
                onTitleClick = { showSessionSwitcherSheet = true },
                onViewLogs = { showAgentLogsSheet = true },
                onReconnect = {
                    if (currentMode == SessionMode.CHAT) chatTerminalSession.connect()
                    else shellTerminalSession.connect()
                },
                onClear = {
                    if (currentMode == SessionMode.CHAT) {
                        chatTerminalSession.clearScreen()
                    } else {
                        shellTerminalSession.clearScreen()
                    }
                },
                onOpenSettings = { onOpenServerSettings() }
            )
            // Content Area: Switch between Agent Chat View and Interactive Terminal View
            if (currentMode == SessionMode.CHAT) {
                AgentChatView(
                    messages = chatMessages,
                    isAgentBusy = isAgentBusy,
                    currentStatus = currentAgentStatus,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                    .pointerInput(terminalPrefs.fontSizeSp) {
                        awaitEachGesture {
                            var initialDistance = 0f
                            var baseFontSize = terminalPrefs.fontSizeSp

                            do {
                                val event = awaitPointerEvent()
                                val activePointers = event.changes.filter { it.pressed }

                                if (activePointers.size >= 2) {
                                    val p1 = activePointers[0].position
                                    val p2 = activePointers[1].position
                                    val currentDistance = (p1 - p2).getDistance()

                                    if (initialDistance == 0f) {
                                        initialDistance = currentDistance
                                        baseFontSize = terminalPrefs.fontSizeSp
                                    } else if (initialDistance > 0f && currentDistance > 0f) {
                                        val scale = currentDistance / initialDistance
                                        val newSize = (baseFontSize * scale).coerceIn(8f, 26f)
                                        if (kotlin.math.abs(newSize - terminalPrefs.fontSizeSp) >= 0.2f) {
                                            prefsRepository.updateFontSize(newSize)
                                            showFontSizeIndicator = true
                                            indicatorDismissJob?.cancel()
                                            indicatorDismissJob = coroutineScope.launch {
                                                delay(1200)
                                                showFontSizeIndicator = false
                                            }
                                        }
                                    }
                                    activePointers.forEach { it.consume() }
                                } else {
                                    initialDistance = 0f
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
            ) {
                SelectionContainer {
                    val verticalScrollState = rememberScrollState()
                    val horizontalScrollState = rememberScrollState()

                    LaunchedEffect(displayOutput) {
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
                        Column {
                            Text(
                                text = displayOutput,
                                style = TextStyle(
                                    fontFamily = terminalPrefs.font.toComposeFontFamily(),
                                    fontSize = terminalPrefs.fontSizeSp.sp,
                                    lineHeight = (terminalPrefs.fontSizeSp * terminalPrefs.lineSpacingMultiplier).sp,
                                    color = if (isDark) Color(0xFFE5E7EB) else Color(0xFF1F2328)
                                ),
                                softWrap = terminalPrefs.softWrap,
                                modifier = if (terminalPrefs.softWrap) Modifier.fillMaxWidth() else Modifier
                            )

                            // Bottom padding so terminal text is not obscured by the collapsed bottom input bar
                            Spacer(modifier = Modifier.height(115.dp))
                        }
                    }
                }

                // Floating Font Size Indicator during pinch gesture
                androidx.compose.animation.AnimatedVisibility(
                    visible = showFontSizeIndicator,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FormatSize,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "字号: ${terminalPrefs.fontSizeSp.toInt()} sp",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            }
        }

        // Layer 2: Floating Input System (Overlaid at bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (currentMode == SessionMode.CHAT) {
                // Chat 专属独立输入体系 (融合胶囊风格，对齐用户提供的 ChatGPT 截图)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(bottom = bottomNavPadding)
                ) {
                    ChatIntegratedInputBar(
                        text = chatInputText,
                        onTextChanged = { chatInputText = it },
                        activeAgent = activeAgent,
                        isAgentBusy = isAgentBusy,
                        isDark = isDark,
                        onOpenAgentPicker = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showAgentPickerSheet = true
                        },
                        onSend = {
                            val promptToSend = chatInputText.trim()
                            if (promptToSend.isNotBlank()) {
                                chatInputText = ""
                                agentRunner.sendPrompt(promptToSend)
                            }
                        }
                    )
                }
            } else {
                // Shell 终端专属独立输入体系 (快捷键栏 + 药丸命令输入框 + 多行扩展支持)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpandedInput,
                    enter = androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut()
                ) {
                    ExpandedInputSheet(
                        text = inputText,
                        onTextChanged = { inputText = it },
                        fontFamily = terminalPrefs.font.toComposeFontFamily(),
                        onCollapse = { isExpandedInput = false },
                        onSend = {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotBlank()) {
                                inputText = ""
                                isExpandedInput = false
                                shellTerminalSession.sendCommand(textToSend)
                            }
                        }
                    )
                }

                if (!isExpandedInput) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .padding(bottom = bottomNavPadding)
                    ) {
                        // Quick Shortcut Bar
                        QuickShortcutBar(
                            onSendRaw = { shellTerminalSession.sendRaw(it) },
                            onInsertText = { inputText += it }
                        )

                        // Shell Input Bar
                        GoogleMessagesInputBar(
                            text = inputText,
                            onTextChanged = { inputText = it },
                            isToolsExpanded = showToolsSheet,
                            onToggleTools = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                showToolsSheet = true
                            },
                            onExpandInput = { isExpandedInput = true },
                            onInputFocused = {},
                            isChatMode = false,
                            onSend = {
                                val textToSend = inputText.trim()
                                if (textToSend.isNotBlank()) {
                                    inputText = ""
                                    shellTerminalSession.sendCommand(textToSend)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    // Material 3 Standard Tools Modal Bottom Sheet
    if (showToolsSheet) {
        ToolsBottomSheet(
            hasStartupScript = currentServer.startupScript.isNotBlank(),
            onRunStartupScript = {
                if (currentServer.startupScript.isNotBlank()) {
                    for (line in currentServer.startupScript.lines().filter { it.isNotBlank() && !it.startsWith("#") }) {
                        shellTerminalSession.sendCommand(line)
                    }
                }
                showToolsSheet = false
            },
            onInsertCommand = { cmd ->
                inputText = cmd
                showToolsSheet = false
            },
            onInsertSymbol = { sym ->
                inputText += sym
            },
            onClearTerminal = {
                shellTerminalSession.clearScreen()
                showToolsSheet = false
            },
            onDismiss = { showToolsSheet = false }
        )
    }

    // Chat 日志查看弹窗 (仿 Multica 一行一个事件流，并支持打开纯文本页面)
    if (showAgentLogsSheet) {
        AgentLogsBottomSheet(
            eventLogs = agentEventLogs,
            onOpenRawLogsPage = {
                showAgentLogsSheet = false
                onOpenRawLogs(agentRawLogs)
            },
            onDismiss = { showAgentLogsSheet = false }
        )
    }
    // Agent 与模型选择底栏 Sheet (对标 Multica 针对每个工作区/服务器切换 Agent、模型和思考程度)
    if (showAgentPickerSheet) {
        AgentPickerBottomSheet(
            discoveredAgents = discoveredAgents,
            selectedAgent = activeAgent,
            onSelectAgent = { newAgent ->
                agentRunner.setAgent(newAgent)
                agentDiscoveryRepo.saveSelectedAgentId(currentServer.id, newAgent.id)
            },
            onSelectModel = { newModel ->
                agentRunner.setModel(newModel)
                val curId = activeAgent?.id
                if (curId != null) {
                    val updated = discoveredAgents.map {
                        if (it.id == curId) it.copy(selectedModel = newModel) else it
                    }
                    discoveredAgents = updated
                    agentDiscoveryRepo.saveAgents(currentServer.id, updated)
                }
            },
            onSelectThinkingLevel = { newLevel ->
                agentRunner.setThinkingLevel(newLevel)
                val curId = activeAgent?.id
                if (curId != null) {
                    val updated = discoveredAgents.map {
                        if (it.id == curId) it.copy(thinkingLevel = newLevel) else it
                    }
                    discoveredAgents = updated
                    agentDiscoveryRepo.saveAgents(currentServer.id, updated)
                }
            },
            onDismiss = { showAgentPickerSheet = false }
        )
    }
    if (showSessionSwitcherSheet) {
        SessionSwitcherSheet(
            server = currentServer,
            sessions = serverSessions,
            activeSessionId = activeSessionItem.id,
            onSelectSession = { sessionId ->
                sessionManager.setActiveSession(currentServer.id, sessionId)
                showSessionSwitcherSheet = false
            },
            onNewSession = {
                sessionManager.createSession(currentServer)
                showSessionSwitcherSheet = false
            },
            onRenameRequest = { sessionItem ->
                sessionToRename = sessionItem
            },
            onCloseSession = { sessionId ->
                sessionManager.closeSession(currentServer.id, sessionId)
                if (serverSessions.size <= 1) {
                    showSessionSwitcherSheet = false
                }
            },
            onDismiss = { showSessionSwitcherSheet = false }
        )
    }

    // Rename Session Dialog
    if (sessionToRename != null) {
        RenameSessionDialog(
            currentTitle = sessionToRename!!.title,
            onDismiss = { sessionToRename = null },
            onSave = { newTitle ->
                sessionManager.renameSession(currentServer.id, sessionToRename!!.id, newTitle)
                sessionToRename = null
            }
        )
    }

    // 统一复用的服务器深度管理抽屉 (修改密码、Host、AI与单CLI配置等)
    if (showServerSettingsSheet) {
        UnifiedServerSettingsBottomSheet(
            server = currentServer,
            agentDiscoveryRepo = agentDiscoveryRepo,
            onSaveServer = { updated ->
                currentServer = updated
                coroutineScope.launch {
                    repository.updateServer(updated)
                }
                showServerSettingsSheet = false
            },
            onDismiss = { showServerSettingsSheet = false }
        )
    }
}

@Composable
fun SessionTopBar(
    server: ServerConfig,
    activeSession: ServerSessionItem,
    connectionState: ConnectionState,
    currentMode: SessionMode,
    onModeSelected: (SessionMode) -> Unit,
    softWrap: Boolean,
    isDark: Boolean,
    onToggleSoftWrap: () -> Unit,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onTitleClick: () -> Unit,
    onViewLogs: () -> Unit,
    onReconnect: () -> Unit,
    onClear: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp)
    ) {
        // 1. 左侧：统一使用规范的 StandardBackButton
        Box(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp)
        ) {
            com.bettershell.app.ui.components.StandardBackButton(
                onBack = onBack,
                isDark = isDark
            )
        }
        // 2. 中间：双胶囊切换滑块（Chat / Shell 模式），绝对居中！
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            ModeTogglePill(
                currentMode = currentMode,
                onModeSelected = onModeSelected,
                isDark = isDark,
                modifier = Modifier.width(148.dp)
            )
        }

        // 3. 右侧：只留一个 Lucide 风格 Ellipsis 更多按钮，展开精简下拉菜单
        Box(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            IconButton(onClick = { showMoreMenu = true }) {
                LucideEllipsis(
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 22.dp
                )
            }
            val menuItems = mutableListOf<OpenAiMenuItemData>()
            menuItems.add(
                OpenAiMenuItemData(
                    title = "会话列表 (${server.name})",
                    icon = LucideIcons.Terminal,
                    onClick = onTitleClick
                )
            )
            menuItems.add(
                OpenAiMenuItemData(
                    title = "查看 Chat 原始日志",
                    icon = LucideIcons.Code2,
                    iconTint = AccentCyan,
                    onClick = onViewLogs
                )
            )
            menuItems.add(
                OpenAiMenuItemData(
                    title = if (softWrap) "禁用自动换行" else "启用自动换行",
                    icon = LucideIcons.WrapText,
                    onClick = onToggleSoftWrap
                )
            )
            if (connectionState is ConnectionState.Disconnected || connectionState is ConnectionState.Error) {
                menuItems.add(
                    OpenAiMenuItemData(
                        title = "重新连接",
                        icon = LucideIcons.RefreshCw,
                        iconTint = AccentGreen,
                        onClick = onReconnect
                    )
                )
            }
            menuItems.add(
                OpenAiMenuItemData(
                    title = "清空终端",
                    icon = LucideIcons.Eraser,
                    onClick = onClear
                )
            )
            menuItems.add(
                OpenAiMenuItemData(
                    title = "服务器设置",
                    icon = LucideIcons.Bolt,
                    onClick = onOpenSettings
                )
            )
            OpenAiDropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
                items = menuItems,
                isDark = isDark,
                width = 230.dp
            )
        }
    }
}

/**
 * Google Messages Style Chat Input Bar (Image #1, #3, #4)
 * - Left: '+' circular button (toggles tools drawer)
 * - Center: Full-round pill input box. When text is present/multi-line, shows expand button at emoji position!
 * - Right: Circular Send button
 * - Flat, zero shadow, minimalist
 */
@Composable
fun GoogleMessagesInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    isToolsExpanded: Boolean,
    onToggleTools: () -> Unit,
    onExpandInput: () -> Unit,
    onInputFocused: () -> Unit = {},
    isChatMode: Boolean = false,
    onSend: () -> Unit
) {
    val plusRotation by animateFloatAsState(
        targetValue = if (isToolsExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "plusRotation"
    )

    val hasText = text.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left: Circular Action Button ('+' with smooth 45° rotation into '×' when expanded)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isToolsExpanded) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    1.dp,
                    if (isToolsExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    CircleShape
                )
                .clickable(onClick = onToggleTools),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "工具与指令",
                tint = if (isToolsExpanded) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(plusRotation)
            )
        }

        // Center: Full-round pill input container (Google Messages style)
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 120.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    RoundedCornerShape(26.dp)
                )
                .clickable { onInputFocused() }
                .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = if (isChatMode) "给 omp 发送需求或问题..." else "输入 Shell 命令...",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    onInputFocused()
                                }
                            },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() })
                    )
                }

                // Trailing Expand Button (Positioned at Emoji location)
                if (text.isNotEmpty()) {
                    IconButton(
                        onClick = onExpandInput,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInFull,
                            contentDescription = "展开多行编辑器",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Right: Circular Send Button with active tint & feedback
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (hasText) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    1.dp,
                    if (hasText) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    CircleShape
                )
                .clickable(enabled = hasText, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "发送命令",
                tint = if (hasText) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
/**
 * Expanded Input Sheet (Image #5 / #6)
 * - Layered over bottom ~55% of the screen (terminal remains visible in top ~45%)
 * - Top-left: '⌄' collapse button
 * - Center: Large multi-line editor with monospace font
 * - Bottom-right: Circular Send button
 * - Clean, flat, zero shadow, toolbars collapsed
 */
@Composable
fun ExpandedInputSheet(
    text: String,
    onTextChanged: (String) -> Unit,
    fontFamily: FontFamily,
    onCollapse: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Down chevron (⌄) to collapse, title info, and clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "收起输入框",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "${text.length} 字符 · ${text.lines().size} 行",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (text.isNotEmpty()) {
                    Text(
                        text = "清空",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onTextChanged("") }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-line Monospace Editor filling the expanded surface
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "在此编写多行 Agent 命令、Shell 脚本或输入交互内容...\n支持长文本编辑和快速发送到终端",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                )
            }

            // Bottom bar inside expanded sheet: Circular send button on bottom-right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable(enabled = text.isNotBlank(), onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

data class ToolItemData(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val isScript: Boolean = false,
    val command: String? = null,
    val symbol: String? = null,
    val isClear: Boolean = false
)

/**
 * Google Messages Style Tools Drawer
 * - Full screen width, no border, no bottom corners, flat surface
 * - Nested scrolling: scroll through all tools, drag down at top to close drawer
 * - Long-press drag-and-drop reordering for tools
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsBottomSheet(
    hasStartupScript: Boolean,
    onRunStartupScript: () -> Unit,
    onInsertCommand: (String) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onClearTerminal: () -> Unit,
    onDismiss: () -> Unit
) {
    var toolsList by remember {
        mutableStateOf(
            listOf(
                ToolItemData("script", "启动脚本", Icons.Rounded.Code, isScript = true),
                ToolItemData("agent", "Agent 状态", Icons.Rounded.SmartToy, command = "agent --status"),
                ToolItemData("git", "Git 状态", Icons.Rounded.Code, command = "git status"),
                ToolItemData("files", "文件列表", Icons.Rounded.Terminal, command = "ls -la"),
                ToolItemData("top", "进程监控", Icons.Rounded.Memory, command = "top"),
                ToolItemData("python", "Python", Icons.Rounded.Terminal, command = "python3 agent.py"),
                ToolItemData("diff", "Git Diff", Icons.Rounded.Code, command = "git diff"),
                ToolItemData("log", "Git Log", Icons.Rounded.Code, command = "git log --oneline -n 10"),
                ToolItemData("docker", "Docker", Icons.Rounded.Memory, command = "docker ps"),
                ToolItemData("ports", "网络端口", Icons.Rounded.Terminal, command = "ss -tlpn || netstat -tlpn"),
                ToolItemData("memory", "内存磁盘", Icons.Rounded.Memory, command = "free -h && df -h"),
                ToolItemData("pipe", "管道符 |", Icons.Rounded.Code, symbol = " | "),
                ToolItemData("home", "主目录 ~/", Icons.Rounded.Folder, symbol = "~/"),
                ToolItemData("sudo", "常用 sudo", Icons.Rounded.Terminal, symbol = "sudo "),
                ToolItemData("clear", "清空终端", Icons.Rounded.DeleteSweep, isClear = true)
            )
        )
    }

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val gridState = rememberLazyGridState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val maxSheetHeight = screenHeightDp * 2f / 3f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (draggingIndex != null) "拖拽卡片以调整顺序" else "快捷工具与指令",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (draggingIndex != null) AccentOrange else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "长按卡片可排序",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                itemsIndexed(toolsList, key = { _, item -> item.id }) { index, item ->
                    val isDragging = draggingIndex == index

                    Box(
                        modifier = Modifier
                            .zIndex(if (isDragging) 10f else 1f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationX = dragOffset.x
                                    translationY = dragOffset.y
                                    scaleX = 1.15f
                                    scaleY = 1.15f
                                }
                            }
                            .pointerInput(index) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = index
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount

                                        val cellWidthPx = size.width.toFloat()
                                        val cellHeightPx = size.height.toFloat()
                                        val deltaCol = (dragOffset.x / cellWidthPx).roundToInt()
                                        val deltaRow = (dragOffset.y / cellHeightPx).roundToInt()
                                        val targetIndex = (draggingIndex!! + deltaRow * 3 + deltaCol)
                                            .coerceIn(0, toolsList.lastIndex)

                                        if (targetIndex != draggingIndex) {
                                            val updated = toolsList.toMutableList()
                                            val moved = updated.removeAt(draggingIndex!!)
                                            updated.add(targetIndex, moved)
                                            toolsList = updated
                                            draggingIndex = targetIndex
                                            dragOffset = Offset.Zero
                                        }
                                    },
                                    onDragEnd = {
                                        draggingIndex = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggingIndex = null
                                        dragOffset = Offset.Zero
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ToolGridItem(
                            icon = item.icon,
                            label = item.label,
                            highlight = item.isScript && hasStartupScript,
                            isReordering = isDragging,
                            onClick = {
                                when {
                                    item.isScript -> onRunStartupScript()
                                    item.command != null -> onInsertCommand(item.command)
                                    item.symbol != null -> onInsertSymbol(item.symbol)
                                    item.isClear -> onClearTerminal()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ToolGridItem(
    icon: ImageVector,
    label: String,
    highlight: Boolean = false,
    isReordering: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    when {
                        highlight -> MaterialTheme.colorScheme.primaryContainer
                        isReordering -> AccentOrange.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .border(
                    1.dp,
                    when {
                        highlight -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        isReordering -> AccentOrange.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    highlight -> MaterialTheme.colorScheme.onPrimaryContainer
                    isReordering -> AccentOrange
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickKeyChip(label = "回车 ↵", isHighlight = true) { onSendRaw(byteArrayOf(13)) }
        QuickKeyChip(label = "TAB ⇥") { onSendRaw(byteArrayOf(9)) }
        QuickKeyChip(label = "ESC") { onSendRaw(byteArrayOf(27)) }
        QuickKeyChip(label = "Ctrl+C", isDanger = true) { onSendRaw(byteArrayOf(3)) }
        QuickKeyChip(label = "Ctrl+D") { onSendRaw(byteArrayOf(4)) }
        QuickKeyChip(label = "Ctrl+Z") { onSendRaw(byteArrayOf(26)) }
        QuickKeyChip(label = "Ctrl+L") { onSendRaw(byteArrayOf(12)) }
        QuickKeyChip(label = "↑") { onSendRaw(byteArrayOf(27, 91, 65)) }
        QuickKeyChip(label = "↓") { onSendRaw(byteArrayOf(27, 91, 66)) }
        QuickKeyChip(label = "←") { onSendRaw(byteArrayOf(27, 91, 68)) }
        QuickKeyChip(label = "→") { onSendRaw(byteArrayOf(27, 91, 67)) }
        QuickKeyChip(label = "|") { onInsertText(" | ") }
        QuickKeyChip(label = "&&") { onInsertText(" && ") }
        QuickKeyChip(label = "~/") { onInsertText("~/") }
        QuickKeyChip(label = "CLEAR") { onInsertText("clear\n") }
    }
}

@Composable
fun QuickKeyChip(
    label: String,
    isDanger: Boolean = false,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = when {
            isHighlight -> MaterialTheme.colorScheme.primaryContainer
            isDanger -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        },
        border = BorderStroke(
            1.dp,
            when {
                isHighlight -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                isDanger -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            }
        ),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isHighlight -> MaterialTheme.colorScheme.onPrimaryContainer
                        isDanger -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            )
        }
    }
}

/**
 * Session Switcher Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSwitcherSheet(
    server: ServerConfig,
    sessions: List<ServerSessionItem>,
    activeSessionId: String,
    onSelectSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onRenameRequest: (ServerSessionItem) -> Unit,
    onCloseSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "会话管理 (Sessions)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${server.name} · 已保持 ${sessions.size} 个后台会话",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sessions.forEach { sessionItem ->
                    val isActive = sessionItem.id == activeSessionId
                    val connState by sessionItem.terminalSession.connectionState.collectAsState()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                1.dp,
                                if (isActive) AccentGreen else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectSession(sessionItem.id) },
                        color = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Rounded.CheckCircle else Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    tint = if (isActive) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = sessionItem.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(AccentGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "当前",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentGreen
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = when (connState) {
                                            is ConnectionState.Connected -> "● 在线运行中"
                                            is ConnectionState.Connecting -> "● 连接中..."
                                            else -> "○ 未连接"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (connState is ConnectionState.Connected) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { onRenameRequest(sessionItem) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "重命名会话",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (sessions.size > 1) {
                                    IconButton(onClick = { onCloseSession(sessionItem.id) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "关闭会话",
                                            tint = AccentRed.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onNewSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("新建终端会话 (+ New Session)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RenameSessionDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("修改会话标题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column {
                Text(
                    text = "为该终端会话设置自定义标题（留空将继续自动跟随终端内的程序名称）：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("会话标题") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.trim()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Server Settings Modal Sheet
 * Theme, Font, Display & Startup Script settings
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
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline)
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${server.name} (${server.username}@${server.host}:${server.port})",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Theme Mode Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "主题外观 (Theme Mode)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppThemeMode.entries.forEach { mode ->
                    val selected = terminalPrefs.themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { prefsRepository.updateThemeMode(mode) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Terminal Display & Font Settings
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "选择字体 (包含完整 Nerd Font / Powerline 图标):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
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
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
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
                        text = "字体大小 (支持双指手势缩放)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "当前: ${terminalPrefs.fontSizeSp.toInt()} sp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { prefsRepository.updateFontSize(terminalPrefs.fontSizeSp - 1f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { prefsRepository.updateFontSize(terminalPrefs.fontSizeSp + 1f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Soft Wrap switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
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
                        color = MaterialTheme.colorScheme.onSurface
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "配置该服务器进入后运行的自定义 sh 脚本（例如激活虚拟环境、启动 Agent 会话等）：",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = startupScript,
                onValueChange = { startupScript = it },
                placeholder = {
                    Text(
                        "# 编写自定义 sh 代码\ncd ~/agent\nsource venv/bin/activate\npython3 main.py",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        onSave(server.copy(startupScript = startupScript))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("保存配置", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
