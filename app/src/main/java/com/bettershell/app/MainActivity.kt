package com.bettershell.app

import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.ServerRepository
import com.bettershell.app.ui.screens.ServerListScreen
import com.bettershell.app.ui.screens.SessionScreen
import com.bettershell.app.ui.theme.BetterShellTheme
import com.bettershell.app.ui.theme.TerminalBlack

sealed interface Screen {
    data object ServerList : Screen
    data object AppSettings : Screen
    data object AppFontSettings : Screen
    data object AppAbout : Screen
    data class ServerSettings(val server: ServerConfig, val fromSession: Boolean = false) : Screen
    data class ServerBasicSettings(val server: ServerConfig) : Screen
    data class ServerAgentSettings(val server: ServerConfig) : Screen
    data class SingleAgentConfig(val server: ServerConfig, val agent: DiscoveredAgent) : Screen
    data class ServerStartupScript(val server: ServerConfig) : Screen
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
                    // 屏幕路由栈：每一次点击进入新的子页面，完全压入栈中！
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
                                    onNavigateToFontSettings = { pushScreen(Screen.AppFontSettings) },
                                    onNavigateToAbout = { pushScreen(Screen.AppAbout) },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.AppFontSettings -> {
                                com.bettershell.app.ui.screens.AppFontSettingsScreen(
                                    prefsRepository = terminalPrefsRepo,
                                    onBack = onBackAction
                                )
                            }
                            is Screen.AppAbout -> {
                                com.bettershell.app.ui.screens.AppAboutScreen(
                                    onBack = onBackAction
                                )
                            }
                            is Screen.ServerSettings -> {
                                com.bettershell.app.ui.screens.ServerSettingsScreen(
                                    server = screen.server,
                                    agentDiscoveryRepo = agentDiscoveryRepo,
                                    onNavigateToBasic = { pushScreen(Screen.ServerBasicSettings(screen.server)) },
                                    onNavigateToAgents = { pushScreen(Screen.ServerAgentSettings(screen.server)) },
                                    onNavigateToStartup = { pushScreen(Screen.ServerStartupScript(screen.server)) },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.ServerBasicSettings -> {
                                com.bettershell.app.ui.screens.ServerBasicSettingsScreen(
                                    server = screen.server,
                                    onSaveServer = { updatedServer ->
                                        coroutineScope.launch {
                                            repository.updateServer(updatedServer)
                                        }
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.ServerAgentSettings -> {
                                com.bettershell.app.ui.screens.ServerAgentSettingsScreen(
                                    server = screen.server,
                                    agentDiscoveryRepo = agentDiscoveryRepo,
                                    onNavigateToSingleAgent = { agent ->
                                        pushScreen(Screen.SingleAgentConfig(screen.server, agent))
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.SingleAgentConfig -> {
                                com.bettershell.app.ui.screens.SingleAgentConfigScreen(
                                    agent = screen.agent,
                                    onSaveAgent = { updatedAgent ->
                                        val currentAgents = agentDiscoveryRepo.getCachedAgents(screen.server.id)
                                        val updated = currentAgents.map { if (it.id == updatedAgent.id) updatedAgent else it }
                                        agentDiscoveryRepo.saveAgents(screen.server.id, updated)
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.ServerStartupScript -> {
                                com.bettershell.app.ui.screens.ServerStartupScriptScreen(
                                    server = screen.server,
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

                    // 全局预见式返回与右进右出平行转场 (针对所有路由栈页面通用！)
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
                        RenderScreen(screen = currentScreen, onBackAction = {})
                    }
                }
            }
        }
    }
}
