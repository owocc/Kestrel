package com.bettershell.app.terminal

import com.bettershell.app.data.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ServerSessionItem(
    val id: String = UUID.randomUUID().toString(),
    val serverId: String,
    val title: String,
    val terminalSession: TerminalSession,
    val isCustomTitle: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Map: serverId -> List of ServerSessionItem
    private val _sessionsMap = MutableStateFlow<Map<String, List<ServerSessionItem>>>(emptyMap())
    val sessionsMap: StateFlow<Map<String, List<ServerSessionItem>>> = _sessionsMap.asStateFlow()

    // Currently active session ID per serverId
    private val _activeSessionMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val activeSessionMap: StateFlow<Map<String, String>> = _activeSessionMap.asStateFlow()

    fun getSessionsForServer(serverId: String): List<ServerSessionItem> {
        return _sessionsMap.value[serverId] ?: emptyList()
    }

    fun getActiveSession(serverId: String): ServerSessionItem? {
        val sessions = getSessionsForServer(serverId)
        val activeId = _activeSessionMap.value[serverId]
        return sessions.find { it.id == activeId } ?: sessions.firstOrNull()
    }

    fun getOrCreateInitialSession(server: ServerConfig): ServerSessionItem {
        val existing = getActiveSession(server.id)
        if (existing != null) {
            return existing
        }
        return createSession(server, title = "会话 1")
    }

    fun createSession(server: ServerConfig, title: String? = null): ServerSessionItem {
        val currentList = _sessionsMap.value[server.id] ?: emptyList()
        val sessionNumber = currentList.size + 1
        val sessionTitle = title ?: "会话 $sessionNumber"

        val termSession = TerminalSession(server, scope)
        val item = ServerSessionItem(
            serverId = server.id,
            title = sessionTitle,
            terminalSession = termSession,
            isCustomTitle = title != null
        )

        // Observe terminal window title changes and auto-update session title if not custom-named
        scope.launch {
            termSession.windowTitle.collect { terminalTitle ->
                if (terminalTitle.isNotBlank()) {
                    updateAutoTitle(server.id, item.id, terminalTitle)
                }
            }
        }

        val updated = currentList + item
        _sessionsMap.value = _sessionsMap.value + (server.id to updated)
        _activeSessionMap.value = _activeSessionMap.value + (server.id to item.id)
        return item
    }

    fun setActiveSession(serverId: String, sessionId: String) {
        _activeSessionMap.value = _activeSessionMap.value + (serverId to sessionId)
    }

    fun renameSession(serverId: String, sessionId: String, newTitle: String) {
        val currentList = _sessionsMap.value[serverId] ?: return
        val updated = currentList.map {
            if (it.id == sessionId) {
                it.copy(title = newTitle.ifBlank { "会话" }, isCustomTitle = true)
            } else {
                it
            }
        }
        _sessionsMap.value = _sessionsMap.value + (serverId to updated)
    }

    private fun updateAutoTitle(serverId: String, sessionId: String, autoTitle: String) {
        val currentList = _sessionsMap.value[serverId] ?: return
        val sessionItem = currentList.find { it.id == sessionId } ?: return
        if (sessionItem.isCustomTitle) {
            return // Respect user's explicit custom title
        }
        val cleanTitle = autoTitle.trim().take(40)
        val updated = currentList.map {
            if (it.id == sessionId) {
                it.copy(title = cleanTitle)
            } else {
                it
            }
        }
        _sessionsMap.value = _sessionsMap.value + (serverId to updated)
    }

    fun closeSession(serverId: String, sessionId: String) {
        val currentList = _sessionsMap.value[serverId] ?: return
        val sessionToClose = currentList.find { it.id == sessionId }
        sessionToClose?.terminalSession?.disconnect()

        val updated = currentList.filterNot { it.id == sessionId }
        _sessionsMap.value = _sessionsMap.value + (serverId to updated)

        if (_activeSessionMap.value[serverId] == sessionId) {
            val nextActive = updated.firstOrNull()?.id ?: ""
            _activeSessionMap.value = _activeSessionMap.value + (serverId to nextActive)
        }
    }
}
