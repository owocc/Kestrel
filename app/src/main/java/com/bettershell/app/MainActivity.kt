package com.bettershell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    data class Session(val server: ServerConfig) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ServerRepository(applicationContext)
        val terminalPrefsRepo = com.bettershell.app.data.TerminalPreferencesRepository(applicationContext)

        setContent {
            BetterShellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TerminalBlack
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.ServerList) }

                    Crossfade(
                        targetState = currentScreen,
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            is Screen.ServerList -> {
                                ServerListScreen(
                                    repository = repository,
                                    onSelectServer = { server ->
                                        currentScreen = Screen.Session(server)
                                    }
                                )
                            }
                            is Screen.Session -> {
                                BackHandler {
                                    currentScreen = Screen.ServerList
                                }
                                SessionScreen(
                                    server = screen.server,
                                    repository = repository,
                                    prefsRepository = terminalPrefsRepo,
                                    onBack = {
                                        currentScreen = Screen.ServerList
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
