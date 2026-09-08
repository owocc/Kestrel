package com.bettershell.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.bettershell.app.ui.theme.InputDivider
import com.bettershell.app.ui.theme.InputPanelSurface
import com.bettershell.app.ui.theme.InputPanelWhite
import com.bettershell.app.ui.theme.InputTextDark
import com.bettershell.app.ui.theme.InputTextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    server: ServerConfig,
    repository: ServerRepository,
    sessionManager: SessionManager,
    prefsRepository: TerminalPreferencesRepository,
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

    val terminalSession = activeSessionItem.terminalSession
    val connectionState by terminalSession.connectionState.collectAsState()
    val rawAnnotatedOutput by terminalSession.annotatedOutput.collectAsState()
    val terminalPrefs by prefsRepository.preferences.collectAsState()

    val isDark = when (terminalPrefs.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val displayOutput = remember(rawAnnotatedOutput, isDark) {
        terminalSession.getAnnotatedOutput(isDark)
    }

    var inputText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var showServerSettingsSheet by remember { mutableStateOf(false) }
    var showSessionSwitcherSheet by remember { mutableStateOf(false) }
    var sessionToRename by remember { mutableStateOf<ServerSessionItem?>(null) }
    var showFontSizeIndicator by remember { mutableStateOf(false) }
    var indicatorDismissJob by remember { mutableStateOf<Job?>(null) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isKeyboardOpen by remember(imeInsets, density) {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }

    // Hierarchical back handling
    BackHandler(enabled = true) {
        when {
            sessionToRename != null -> {
                sessionToRename = null
            }
            showSessionSwitcherSheet -> {
                showSessionSwitcherSheet = false
            }
            showServerSettingsSheet -> {
                showServerSettingsSheet = false
            }
            isExpanded -> {
                isExpanded = false
            }
            isKeyboardOpen -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            else -> {
                onBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation & Status Bar with Session Switching & Theme Toggle
            SessionTopBar(
                server = currentServer,
                activeSession = activeSessionItem,
                connectionState = connectionState,
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
                onReconnect = { terminalSession.connect() },
                onClear = { terminalSession.clearScreen() },
                onOpenSettings = { showServerSettingsSheet = true }
            )

            // Virtual Terminal View Area (VT Buffer with in-place line rewrite and 2D scroll + Pinch zoom)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
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
                        shadowElevation = 6.dp
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

            // Quick Shortcut Bar (ENTER, ESC, TAB, CTRL-C, CTRL-D, Arrows)
            QuickShortcutBar(
                onSendRaw = { terminalSession.sendRaw(it) },
                onInsertText = { inputText += it }
            )

            // Expandable Bottom Input Panel (Fully rounded pill shape)
            ExpandableInputPanel(
                text = inputText,
                onTextChanged = { inputText = it },
                isExpanded = isExpanded,
                isDark = isDark,
                onToggleExpand = { isExpanded = !isExpanded },
                onSend = {
                    if (inputText.isNotBlank()) {
                        terminalSession.sendCommand(inputText)
                        inputText = ""
                    }
                },
                onQuickPrompt = { prompt ->
                    inputText = prompt
                },
                onRunStartupScript = {
                    if (currentServer.startupScript.isNotBlank()) {
                        for (line in currentServer.startupScript.lines().filter { it.isNotBlank() && !it.startsWith("#") }) {
                            terminalSession.sendCommand(line)
                        }
                    }
                },
                onSendEnter = { terminalSession.sendRaw(byteArrayOf(13)) },
                hasStartupScript = currentServer.startupScript.isNotBlank()
            )
        }
    }

    // Session Switcher Bottom Sheet
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

    // Server Settings Sheet (Theme, Font, Display & Startup Script)
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
    activeSession: ServerSessionItem,
    connectionState: ConnectionState,
    softWrap: Boolean,
    isDark: Boolean,
    onToggleSoftWrap: () -> Unit,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onTitleClick: () -> Unit,
    onReconnect: () -> Unit,
    onClear: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Back button & Server / Session Info (Clickable for session switcher)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTitleClick)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${server.name} · ${activeSession.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Switch Sessions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )

                    // Connection Status Pill
                    val (dotColor, statusText) = when (connectionState) {
                        is ConnectionState.Connected -> AccentGreen to "已连接"
                        is ConnectionState.Connecting -> AccentOrange to "连接中"
                        is ConnectionState.Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant to "未连接"
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Right Action buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "切换主题模式",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onToggleSoftWrap) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.WrapText,
                    contentDescription = if (softWrap) "禁用自动换行" else "启用自动换行",
                    tint = if (softWrap) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Server Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                        color = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
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
        QuickKeyChip(label = "回车 ↵", isHighlight = true) { onSendRaw(byteArrayOf(13)) }
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
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                when {
                    isHighlight -> MaterialTheme.colorScheme.primary
                    isDanger -> AccentRed.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outline
                },
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isHighlight -> MaterialTheme.colorScheme.onPrimary
                    isDanger -> AccentRed
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        )
    }
}

/**
 * Expandable Input Panel
 * Fully rounded pill shape (全圆角)
 */
@Composable
fun ExpandableInputPanel(
    text: String,
    onTextChanged: (String) -> Unit,
    isExpanded: Boolean,
    isDark: Boolean,
    onToggleExpand: () -> Unit,
    onSend: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    onRunStartupScript: () -> Unit,
    onSendEnter: () -> Unit = {},
    hasStartupScript: Boolean
) {
    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) 480.dp else 68.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "panelHeight"
    )

    val panelShape = if (isExpanded) {
        RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(34.dp)
    }

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
                shape = panelShape
            ),
        shape = panelShape,
        color = if (isDark) InputPanelWhite else MaterialTheme.colorScheme.surface,
        border = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isExpanded) {
                ExpandedPanelContent(
                    text = text,
                    onTextChanged = onTextChanged,
                    onToggleExpand = onToggleExpand,
                    onSend = onSend,
                    onSendEnter = onSendEnter,
                    onQuickPrompt = onQuickPrompt,
                    onRunStartupScript = onRunStartupScript,
                    hasStartupScript = hasStartupScript
                )
            } else {
                CollapsedPanelContent(
                    text = text,
                    isDark = isDark,
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
    isDark: Boolean,
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isDark) InputPanelSurface else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onToggleExpand),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.OpenInFull,
                contentDescription = "Expand input",
                tint = if (isDark) InputTextDark else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        // Full round inner text input container (全圆角)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(if (isDark) InputPanelSurface else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "输入指令发送到 Agent 终端...",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        color = if (isDark) InputTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = if (isDark) InputTextDark else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(if (isDark) InputTextDark else MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = text.isNotBlank(), onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
    onSendEnter: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    onRunStartupScript: () -> Unit,
    hasStartupScript: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
            PromptChip(
                label = "回车 ↵",
                isHighlight = true,
                onClick = onSendEnter
            )
            PromptChip(label = "agent --status", onClick = { onQuickPrompt("agent --status") })
            PromptChip(label = "ls -la", onClick = { onQuickPrompt("ls -la") })
            PromptChip(label = "git status", onClick = { onQuickPrompt("git status") })
            PromptChip(label = "python3 agent.py", onClick = { onQuickPrompt("python3 agent.py") })
            PromptChip(label = "htop", onClick = { onQuickPrompt("htop") })
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
