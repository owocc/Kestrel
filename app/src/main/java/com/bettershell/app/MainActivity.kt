package com.bettershell.app

import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.ServerRepository
import com.bettershell.app.ui.screens.ServerListScreen
import com.bettershell.app.ui.screens.SessionScreen
import com.bettershell.app.ui.theme.BetterShellTheme
import com.bettershell.app.ui.theme.TerminalBlack

sealed interface Screen {
    data object ServerList : Screen
    data object AppSettings : Screen
    data class ServerSettings(val server: ServerConfig, val fromSession: Boolean = false) : Screen
    data class Session(val server: ServerConfig) : Screen
    data class RawLogs(val server: ServerConfig, val logs: String) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ServerRepository(applicationContext)
        val terminalPrefsRepo = com.bettershell.app.data.TerminalPreferencesRepository(applicationContext)
        val sessionManager = com.bettershell.app.terminal.SessionManager()
        val agentDiscoveryRepo = com.bettershell.app.agent.AgentDiscoveryRepository(applicationContext)

        setContent {
            val terminalPrefs by terminalPrefsRepo.preferences.collectAsState()
            BetterShellTheme(themeMode = terminalPrefs.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    // 屏幕导航栈：记录完整的历史页面，支持预见式返回时底层渲染真实的上一页
                    var screenStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.ServerList)) }
                    val currentScreen = screenStack.last()

                    fun pushScreen(screen: Screen) {
                        screenStack = screenStack + screen
                    }

                    fun popScreen() {
                        if (screenStack.size > 1) {
                            screenStack = screenStack.dropLast(1)
                        }
                    }

                    // 统一的页面 Composable 渲染器
                    @Composable
                    fun RenderScreen(screen: Screen, onBackAction: () -> Unit) {
                        when (screen) {
                            is Screen.ServerList -> {
                                ServerListScreen(
                                    repository = repository,
                                    prefsRepository = terminalPrefsRepo,
                                    onOpenAppSettings = { pushScreen(Screen.AppSettings) },
                                    onOpenServerSettings = { server ->
                                        pushScreen(Screen.ServerSettings(server, fromSession = false))
                                    },
                                    onSelectServer = { server ->
                                        pushScreen(Screen.Session(server))
                                    }
                                )
                            }
                            is Screen.AppSettings -> {
                                com.bettershell.app.ui.screens.AppSettingsScreen(
                                    prefsRepository = terminalPrefsRepo,
                                    onBack = onBackAction
                                )
                            }
                            is Screen.ServerSettings -> {
                                com.bettershell.app.ui.screens.ServerSettingsScreen(
                                    server = screen.server,
                                    agentDiscoveryRepo = agentDiscoveryRepo,
                                    onSaveServer = { updatedServer ->
                                        coroutineScope.launch {
                                            repository.updateServer(updatedServer)
                                        }
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.Session -> {
                                SessionScreen(
                                    server = screen.server,
                                    repository = repository,
                                    sessionManager = sessionManager,
                                    prefsRepository = terminalPrefsRepo,
                                    onOpenRawLogs = { logsText ->
                                        pushScreen(Screen.RawLogs(screen.server, logsText))
                                    },
                                    onOpenServerSettings = {
                                        pushScreen(Screen.ServerSettings(screen.server, fromSession = true))
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.RawLogs -> {
                                com.bettershell.app.ui.screens.RawLogsScreen(
                                    rawLogs = screen.logs,
                                    onBack = onBackAction
                                )
                            }
                        }
                    }

                    // 如果当前处于子页面，包裹 PredictiveBackContainer
                    // 底层透出渲染 screenStack 中倒数第二个页面 (真正的上一页！)
                    // 顶层渲染当前页面，侧滑时前景向右位移与淡出，底层上一页从 -30% 平行推入，完全符合官方预览规范！
                    if (screenStack.size > 1) {
                        val previousScreen = screenStack[screenStack.size - 2]
                        com.bettershell.app.ui.components.PredictiveBackContainer(
                            enabled = true,
                            onBack = { popScreen() },
                            previousContent = {
                                RenderScreen(screen = previousScreen, onBackAction = {})
                            }
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    (slideInHorizontally(
                                        initialOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                        ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                    )
                                },
                                label = "subScreenTransition"
                            ) { screen ->
                                RenderScreen(screen = screen, onBackAction = { popScreen() })
                            }
                        }
                    } else {
                        // 根首页 ServerList
                        RenderScreen(screen = currentScreen, onBackAction = {})
                    }
                }
            }
        }
    }
}
