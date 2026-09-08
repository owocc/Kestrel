package com.bettershell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.ServerRepository
import com.bettershell.app.navigation.NavigationStackHost
import com.bettershell.app.ui.screens.AppAboutScreen
import com.bettershell.app.ui.screens.AppFontSettingsScreen
import com.bettershell.app.ui.screens.AppSettingsScreen
import com.bettershell.app.ui.screens.RawLogsScreen
import com.bettershell.app.ui.screens.ServerAgentSettingsScreen
import com.bettershell.app.ui.screens.ServerBasicSettingsScreen
import com.bettershell.app.ui.screens.ServerListScreen
import com.bettershell.app.ui.screens.ServerSettingsScreen
import com.bettershell.app.ui.screens.ServerStartupScriptScreen
import com.bettershell.app.ui.screens.SessionScreen
import com.bettershell.app.ui.screens.SingleAgentConfigScreen
import com.bettershell.app.ui.theme.BetterShellTheme
import kotlinx.coroutines.launch

sealed interface Screen {
    data object ServerList : Screen
    data object AddServer : Screen
    data object AppSettings : Screen
    data object AppFontSettings : Screen
    data object AppAbout : Screen
    data object OpenSourceLicenses : Screen
    data class LicenseDetail(val library: com.bettershell.app.data.OpenSourceLibraryDetail) : Screen
    data object Acknowledgements : Screen
    data class ServerSettings(val server: ServerConfig, val fromSession: Boolean = false) : Screen
    data class ServerBasicSettings(val server: ServerConfig) : Screen
    data class ServerAgentSettings(val server: ServerConfig) : Screen
    data class SingleAgentConfig(val server: ServerConfig, val agent: DiscoveredAgent) : Screen
    data class ServerStartupScript(val server: ServerConfig) : Screen
    data class Session(val server: ServerConfig) : Screen
    data class LogScreen(val server: ServerConfig, val rawLogs: String, val eventLogs: List<com.bettershell.app.terminal.AgentEventLogItem>) : Screen
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
                    var screenStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.ServerList)) }

                    fun pushScreen(screen: Screen) {
                        screenStack = screenStack + screen
                    }

                    fun popScreen() {
                        if (screenStack.size > 1) {
                            screenStack = screenStack.dropLast(1)
                        }
                    }

                    @Composable
                    fun ScreenRenderer(screen: Screen, onBackAction: () -> Unit) {
                        when (screen) {
                            is Screen.ServerList -> {
                                ServerListScreen(
                                    repository = repository,
                                    prefsRepository = terminalPrefsRepo,
                                    onOpenAppSettings = { pushScreen(Screen.AppSettings) },
                                    onOpenAddServer = { pushScreen(Screen.AddServer) },
                                    onOpenServerSettings = { server ->
                                        pushScreen(Screen.ServerSettings(server, fromSession = false))
                                    },
                                    onSelectServer = { server ->
                                        pushScreen(Screen.Session(server))
                                    }
                                )
                            }
                            is Screen.AddServer -> {
                                com.bettershell.app.ui.screens.AddServerScreen(
                                    onSave = { newServer ->
                                        coroutineScope.launch {
                                            repository.addServer(newServer)
                                        }
                                        popScreen()
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.AppSettings -> {
                                AppSettingsScreen(
                                    prefsRepository = terminalPrefsRepo,
                                    onNavigateToFontSettings = { pushScreen(Screen.AppFontSettings) },
                                    onNavigateToAbout = { pushScreen(Screen.AppAbout) },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.AppFontSettings -> {
                                AppFontSettingsScreen(
                                    prefsRepository = terminalPrefsRepo,
                                    onBack = onBackAction
                                )
                            }
                            is Screen.AppAbout -> {
                                AppAboutScreen(
                                    onNavigateToLicenses = { pushScreen(Screen.OpenSourceLicenses) },
                                    onNavigateToAcknowledgements = { pushScreen(Screen.Acknowledgements) },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.OpenSourceLicenses -> {
                                com.bettershell.app.ui.screens.OpenSourceLicensesScreen(
                                    onSelectLibrary = { lib -> pushScreen(Screen.LicenseDetail(lib)) },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.LicenseDetail -> {
                                com.bettershell.app.ui.screens.LicenseDetailScreen(
                                    library = screen.library,
                                    onBack = onBackAction
                                )
                            }
                            is Screen.Acknowledgements -> {
                                com.bettershell.app.ui.screens.AcknowledgementsScreen(
                                    onBack = onBackAction
                                )
                            }
                            is Screen.ServerSettings -> {
                                ServerSettingsScreen(
                                    server = screen.server,
                                    agentDiscoveryRepo = agentDiscoveryRepo,
                                    onNavigateToBasic = { pushScreen(Screen.ServerBasicSettings(screen.server)) },
                                    onNavigateToAgents = { pushScreen(Screen.ServerAgentSettings(screen.server)) },
                                    onNavigateToStartup = { pushScreen(Screen.ServerStartupScript(screen.server)) },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.ServerBasicSettings -> {
                                ServerBasicSettingsScreen(
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
                                ServerAgentSettingsScreen(
                                    server = screen.server,
                                    agentDiscoveryRepo = agentDiscoveryRepo,
                                    onNavigateToSingleAgent = { agent ->
                                        pushScreen(Screen.SingleAgentConfig(screen.server, agent))
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.SingleAgentConfig -> {
                                SingleAgentConfigScreen(
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
                                ServerStartupScriptScreen(
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
                                    onOpenLogsScreen = { raw, events ->
                                        pushScreen(Screen.LogScreen(screen.server, raw, events))
                                    },
                                    onOpenServerSettings = {
                                        pushScreen(Screen.ServerSettings(screen.server, fromSession = true))
                                    },
                                    onBack = onBackAction
                                )
                            }
                            is Screen.LogScreen -> {
                                com.bettershell.app.ui.screens.LogScreen(
                                    rawLogs = screen.rawLogs,
                                    eventLogs = screen.eventLogs,
                                    onBack = onBackAction
                                )
                            }
                        }
                    }

                    // 全局聚合导航器：彻底融合手势与编程式转场
                    NavigationStackHost(
                        stack = screenStack,
                        onPop = { popScreen() },
                        renderScreen = { screen, onBack ->
                            ScreenRenderer(screen = screen, onBackAction = onBack)
                        }
                    )
                }
            }
        }
    }
}
