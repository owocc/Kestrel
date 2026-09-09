package com.bettershell.app.terminal

import android.content.Context
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.agent.UniversalAgentRunner
import com.bettershell.app.data.LocalAgentDatabaseHelper
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.TerminalSessionRecord
import com.bettershell.app.data.WorkSessionRecord
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
    val tmuxSessionName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkSessionItem(
    val id: String = UUID.randomUUID().toString(),
    val serverId: String,
    val title: String,
    val cwd: String = "~",
    val agentId: String? = null,
    val selectedModel: String = "default",
    val thinkingLevel: ThinkingLevel = ThinkingLevel.AUTO,
    val isCustomTitle: Boolean = false,
    val cliResumeId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

class SessionManager(private val context: Context? = null) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dbHelper = context?.let { LocalAgentDatabaseHelper(it) }

    // ==================== 1. Terminal 会话状态管理 ====================

    // Map: serverId -> List of ServerSessionItem
    private val _sessionsMap = MutableStateFlow<Map<String, List<ServerSessionItem>>>(emptyMap())
    val sessionsMap: StateFlow<Map<String, List<ServerSessionItem>>> = _sessionsMap.asStateFlow()

    // Currently active terminal session ID per serverId
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

        // 尝试从持久化数据库恢复已保存的终端会话
        val savedRecords = dbHelper?.getTerminalSessions(server.id) ?: emptyList()
        if (savedRecords.isNotEmpty()) {
            val restoredList = savedRecords.map { rec ->
                val termSession = TerminalSession(
                    server = server,
                    scope = scope,
                    tmuxSessionName = rec.tmuxSessionName
                )
                val item = ServerSessionItem(
                    id = rec.id,
                    serverId = server.id,
                    title = rec.title,
                    terminalSession = termSession,
                    isCustomTitle = rec.isCustomTitle,
                    tmuxSessionName = rec.tmuxSessionName,
                    createdAt = rec.createdAt
                )
                observeTerminalTitle(server.id, item)
                item
            }
            _sessionsMap.value = _sessionsMap.value + (server.id to restoredList)
            val firstId = restoredList.first().id
            _activeSessionMap.value = _activeSessionMap.value + (server.id to firstId)
            return restoredList.first()
        }

        return createSession(server, title = "会话 1")
    }

    fun createSession(server: ServerConfig, title: String? = null): ServerSessionItem {
        val currentList = _sessionsMap.value[server.id] ?: emptyList()
        val sessionNumber = currentList.size + 1
        val sessionTitle = title ?: "会话 $sessionNumber"
        val sessionId = UUID.randomUUID().toString()
        val tmuxName = "bs_${server.id.take(6)}_${sessionId.take(6)}"

        val termSession = TerminalSession(
            server = server,
            scope = scope,
            tmuxSessionName = tmuxName
        )
        val item = ServerSessionItem(
            id = sessionId,
            serverId = server.id,
            title = sessionTitle,
            terminalSession = termSession,
            isCustomTitle = title != null,
            tmuxSessionName = tmuxName
        )

        // 持久化保存
        dbHelper?.saveTerminalSession(
            TerminalSessionRecord(
                id = item.id,
                serverId = server.id,
                title = item.title,
                isCustomTitle = item.isCustomTitle,
                tmuxSessionName = tmuxName,
                createdAt = item.createdAt,
                updatedAt = item.createdAt
            )
        )

        observeTerminalTitle(server.id, item)

        val updated = currentList + item
        _sessionsMap.value = _sessionsMap.value + (server.id to updated)
        _activeSessionMap.value = _activeSessionMap.value + (server.id to item.id)
        return item
    }

    private fun observeTerminalTitle(serverId: String, item: ServerSessionItem) {
        scope.launch {
            item.terminalSession.windowTitle.collect { terminalTitle ->
                if (terminalTitle.isNotBlank()) {
                    updateAutoTitle(serverId, item.id, terminalTitle)
                }
            }
        }
    }

    fun setActiveSession(serverId: String, sessionId: String) {
        _activeSessionMap.value = _activeSessionMap.value + (serverId to sessionId)
    }

    fun renameSession(serverId: String, sessionId: String, newTitle: String) {
        val currentList = _sessionsMap.value[serverId] ?: return
        val cleanTitle = newTitle.ifBlank { "会话" }
        val updated = currentList.map {
            if (it.id == sessionId) {
                it.copy(title = cleanTitle, isCustomTitle = true)
            } else {
                it
            }
        }
        _sessionsMap.value = _sessionsMap.value + (serverId to updated)
        dbHelper?.renameTerminalSession(sessionId, cleanTitle)
    }

    private fun updateAutoTitle(serverId: String, sessionId: String, autoTitle: String) {
        val currentList = _sessionsMap.value[serverId] ?: return
        val sessionItem = currentList.find { it.id == sessionId } ?: return
        if (sessionItem.isCustomTitle) {
            return
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

        dbHelper?.deleteTerminalSession(sessionId)
    }

    // ==================== 2. Work (Agent Chat) 多会话状态管理 ====================

    // Map: serverId -> List of WorkSessionItem
    private val _workSessionsMap = MutableStateFlow<Map<String, List<WorkSessionItem>>>(emptyMap())
    val workSessionsMap: StateFlow<Map<String, List<WorkSessionItem>>> = _workSessionsMap.asStateFlow()

    // Currently active work session ID per serverId
    private val _activeWorkSessionMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val activeWorkSessionMap: StateFlow<Map<String, String>> = _activeWorkSessionMap.asStateFlow()

    // 内存中活跃的 Runner 缓存 (workSessionId -> UniversalAgentRunner)
    private val _runnersCache = mutableMapOf<String, UniversalAgentRunner>()

    fun getWorkSessionsForServer(serverId: String): List<WorkSessionItem> {
        return _workSessionsMap.value[serverId] ?: emptyList()
    }

    fun getActiveWorkSession(serverId: String): WorkSessionItem? {
        val sessions = getWorkSessionsForServer(serverId)
        val activeId = _activeWorkSessionMap.value[serverId]
        return sessions.find { it.id == activeId } ?: sessions.firstOrNull()
    }

    fun getOrCreateInitialWorkSession(server: ServerConfig, defaultAgent: DiscoveredAgent?): WorkSessionItem {
        val existing = getActiveWorkSession(server.id)
        if (existing != null) {
            return existing
        }

        // 尝试从持久化数据库恢复已保存的 Work 会话
        val savedRecords = dbHelper?.getWorkSessions(server.id) ?: emptyList()
        if (savedRecords.isNotEmpty()) {
            val restoredList = savedRecords.map { rec ->
                WorkSessionItem(
                    id = rec.id,
                    serverId = server.id,
                    title = rec.title,
                    cwd = rec.cwd,
                    agentId = rec.agentId,
                    selectedModel = rec.selectedModel,
                    thinkingLevel = try { ThinkingLevel.valueOf(rec.thinkingLevel) } catch (_: Exception) { ThinkingLevel.AUTO },
                    isCustomTitle = rec.isCustomTitle,
                    cliResumeId = rec.cliResumeId,
                    createdAt = rec.createdAt,
                    updatedAt = rec.updatedAt
                )
            }
            _workSessionsMap.value = _workSessionsMap.value + (server.id to restoredList)
            val firstId = restoredList.first().id
            _activeWorkSessionMap.value = _activeWorkSessionMap.value + (server.id to firstId)
            return restoredList.first()
        }

        // 创建初始 Work 会话 (优先采用服务器配置的预置目录)
        val initialCwd = server.presetDirectories.firstOrNull() ?: "~"
        return createWorkSession(
            server = server,
            title = "新任务 1",
            cwd = initialCwd,
            agentId = server.defaultAgentId ?: defaultAgent?.id,
            model = defaultAgent?.selectedModel ?: "default",
            thinkingLevel = defaultAgent?.thinkingLevel ?: ThinkingLevel.AUTO
        )
    }

    fun createWorkSession(
        server: ServerConfig,
        title: String? = null,
        cwd: String? = null,
        agentId: String? = null,
        model: String = "default",
        thinkingLevel: ThinkingLevel = ThinkingLevel.AUTO,
        cliResumeId: String? = null
    ): WorkSessionItem {
        val currentList = _workSessionsMap.value[server.id] ?: emptyList()
        val sessionNumber = currentList.size + 1
        val sessionTitle = title ?: "任务 $sessionNumber"
        val resolvedCwd = cwd ?: server.presetDirectories.firstOrNull() ?: "~"

        val item = WorkSessionItem(
            id = UUID.randomUUID().toString(),
            serverId = server.id,
            title = sessionTitle,
            cwd = resolvedCwd,
            agentId = agentId ?: server.defaultAgentId,
            selectedModel = model,
            thinkingLevel = thinkingLevel,
            isCustomTitle = title != null,
            cliResumeId = cliResumeId
        )

        // 持久化存储
        dbHelper?.saveWorkSession(
            WorkSessionRecord(
                id = item.id,
                serverId = server.id,
                title = item.title,
                cwd = item.cwd,
                agentId = item.agentId,
                selectedModel = item.selectedModel,
                thinkingLevel = item.thinkingLevel.name,
                isCustomTitle = item.isCustomTitle,
                cliResumeId = item.cliResumeId,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        )

        val updated = listOf(item) + currentList
        _workSessionsMap.value = _workSessionsMap.value + (server.id to updated)
        _activeWorkSessionMap.value = _activeWorkSessionMap.value + (server.id to item.id)
        return item
    }

    fun setActiveWorkSession(serverId: String, sessionId: String) {
        _activeWorkSessionMap.value = _activeWorkSessionMap.value + (serverId to sessionId)
    }

    fun renameWorkSession(serverId: String, sessionId: String, newTitle: String) {
        val currentList = _workSessionsMap.value[serverId] ?: return
        val cleanTitle = newTitle.ifBlank { "任务" }
        val updated = currentList.map {
            if (it.id == sessionId) {
                it.copy(title = cleanTitle, isCustomTitle = true, updatedAt = System.currentTimeMillis())
            } else {
                it
            }
        }
        _workSessionsMap.value = _workSessionsMap.value + (serverId to updated)
        dbHelper?.renameWorkSession(sessionId, cleanTitle)
    }

    fun updateWorkSessionCwd(serverId: String, sessionId: String, newCwd: String) {
        val currentList = _workSessionsMap.value[serverId] ?: return
        val updated = currentList.map {
            if (it.id == sessionId) {
                it.copy(cwd = newCwd, updatedAt = System.currentTimeMillis())
            } else {
                it
            }
        }
        _workSessionsMap.value = _workSessionsMap.value + (serverId to updated)
        _runnersCache[sessionId]?.updateWorkingDirectory(newCwd)
        dbHelper?.updateWorkSessionCwd(sessionId, newCwd)
    }

    fun updateWorkSessionAgent(
        serverId: String,
        sessionId: String,
        agentId: String?,
        model: String,
        thinkingLevel: ThinkingLevel
    ) {
        val currentList = _workSessionsMap.value[serverId] ?: return
        val updated = currentList.map {
            if (it.id == sessionId) {
                it.copy(
                    agentId = agentId,
                    selectedModel = model,
                    thinkingLevel = thinkingLevel,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                it
            }
        }
        _workSessionsMap.value = _workSessionsMap.value + (serverId to updated)
        dbHelper?.updateWorkSessionAgent(sessionId, agentId, model, thinkingLevel.name)
    }

    fun closeWorkSession(serverId: String, sessionId: String) {
        val currentList = _workSessionsMap.value[serverId] ?: return
        val updated = currentList.filterNot { it.id == sessionId }
        _workSessionsMap.value = _workSessionsMap.value + (serverId to updated)

        if (_activeWorkSessionMap.value[serverId] == sessionId) {
            val nextActive = updated.firstOrNull()?.id ?: ""
            _activeWorkSessionMap.value = _activeWorkSessionMap.value + (serverId to nextActive)
        }

        _runnersCache.remove(sessionId)
        dbHelper?.deleteWorkSession(sessionId)
    }

    fun getOrCreateWorkRunner(
        workSession: WorkSessionItem,
        currentAgent: DiscoveredAgent?,
        sendRawCommand: (String) -> Unit
    ): UniversalAgentRunner {
        val existing = _runnersCache[workSession.id]
        if (existing != null) {
            return existing
        }

        val runner = UniversalAgentRunner(
            sessionId = workSession.id,
            cwd = workSession.cwd,
            cliResumeId = workSession.cliResumeId,
            initialAgent = currentAgent,
            sendRawCommand = sendRawCommand,
            onMessageSaved = { message ->
                dbHelper?.saveWorkMessage(workSession.id, message)
            }
        )

        // 加载 SQLite 中持久化存储的历史对话消息
        val history = dbHelper?.getWorkMessages(workSession.id) ?: emptyList()
        if (history.isNotEmpty()) {
            runner.loadHistory(history)
        }

        _runnersCache[workSession.id] = runner
        return runner
    }

    // ==================== 3. Chat 专属通信管道 ====================

    // Chat 专属独立后台 SSH 连接管道 (与用户交互式 Shell 终端彻底隔离)
    private val _chatSessionsMap = MutableStateFlow<Map<String, ServerSessionItem>>(emptyMap())
    val chatSessionsMap: StateFlow<Map<String, ServerSessionItem>> = _chatSessionsMap.asStateFlow()

    fun getOrCreateChatSession(server: ServerConfig): ServerSessionItem {
        val existing = _chatSessionsMap.value[server.id]
        if (existing != null && existing.terminalSession.connectionState.value !is ConnectionState.Disconnected) {
            return existing
        }
        val termSession = TerminalSession(
            server = server,
            scope = scope,
            tmuxSessionName = null, // 后台通道无需分配交互式 tmux
            isAgentChannel = true   // 启用 10000 列宽屏 dumb 模式，彻底避免行折行与终端转义污染
        )
        val item = ServerSessionItem(
            serverId = server.id,
            title = "Agent Runner Channel",
            terminalSession = termSession,
            isCustomTitle = true
        )
        _chatSessionsMap.value = _chatSessionsMap.value + (server.id to item)
        return item
    }
}
