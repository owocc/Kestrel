package com.bettershell.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bettershell.app.agent.AgentType
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.terminal.ChatMessage
import com.bettershell.app.terminal.ChatSender
import com.bettershell.app.terminal.ExecutionStep
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class WorkSessionRecord(
    val id: String,
    val serverId: String,
    val title: String,
    val cwd: String = "~",
    val agentId: String? = null,
    val selectedModel: String = "default",
    val thinkingLevel: String = "AUTO",
    val isCustomTitle: Boolean = false,
    val cliResumeId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TerminalSessionRecord(
    val id: String,
    val serverId: String,
    val title: String,
    val isCustomTitle: Boolean = false,
    val tmuxSessionName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 本地嵌入式 SQL (LibSQL / SQLite) 存储
 * 持久化记录每个服务器的扫描状态与探测到的 Agent 列表，以及 Work 和 Terminal 会话与历史消息
 */
class LocalAgentDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "bettershell_agents.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        // 服务器扫描元数据表 (记录是否首次已扫描等)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS server_scan_meta (
                server_id TEXT PRIMARY KEY,
                last_scanned_at INTEGER NOT NULL,
                has_scanned INTEGER NOT NULL DEFAULT 0,
                selected_agent_id TEXT
            )
            """.trimIndent()
        )

        // 探测到的 Agent 表
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS server_discovered_agents (
                server_id TEXT NOT NULL,
                agent_id TEXT NOT NULL,
                command TEXT NOT NULL,
                path TEXT NOT NULL,
                version TEXT,
                selected_model TEXT,
                thinking_level TEXT,
                models_json TEXT,
                PRIMARY KEY (server_id, agent_id)
            )
            """.trimIndent()
        )

        createSessionTables(db)
    }

    private fun createSessionTables(db: SQLiteDatabase) {
        // Work 多会话表
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS work_sessions (
                id TEXT PRIMARY KEY,
                server_id TEXT NOT NULL,
                title TEXT NOT NULL,
                cwd TEXT NOT NULL DEFAULT '~',
                agent_id TEXT,
                selected_model TEXT,
                thinking_level TEXT,
                is_custom_title INTEGER NOT NULL DEFAULT 0,
                cli_resume_id TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Work 历史对话消息表
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS work_messages (
                id TEXT PRIMARY KEY,
                session_id TEXT NOT NULL,
                sender TEXT NOT NULL,
                content TEXT NOT NULL,
                image_uris_json TEXT NOT NULL DEFAULT '[]',
                steps_json TEXT NOT NULL DEFAULT '[]',
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Terminal 会话持久化表
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS terminal_sessions (
                id TEXT PRIMARY KEY,
                server_id TEXT NOT NULL,
                title TEXT NOT NULL,
                is_custom_title INTEGER NOT NULL DEFAULT 0,
                tmux_session_name TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createSessionTables(db)
        }
    }
    private val json = Json { ignoreUnknownKeys = true }

    fun hasScanned(serverId: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT has_scanned FROM server_scan_meta WHERE server_id = ?",
            arrayOf(serverId)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 1
            }
        }
        return false
    }

    fun getSelectedAgentId(serverId: String): String? {
        readableDatabase.rawQuery(
            "SELECT selected_agent_id FROM server_scan_meta WHERE server_id = ?",
            arrayOf(serverId)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    fun setSelectedAgentId(serverId: String, agentId: String) {
        val values = ContentValues().apply {
            put("selected_agent_id", agentId)
        }
        writableDatabase.update("server_scan_meta", values, "server_id = ?", arrayOf(serverId))
    }

    fun getDiscoveredAgents(serverId: String): List<DiscoveredAgent> {
        val list = mutableListOf<DiscoveredAgent>()
        readableDatabase.rawQuery(
            "SELECT agent_id, command, path, version, selected_model, thinking_level, models_json FROM server_discovered_agents WHERE server_id = ?",
            arrayOf(serverId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val cmd = cursor.getString(1)
                val path = cursor.getString(2)
                val version = cursor.getString(3) ?: ""
                val selectedModel = cursor.getString(4) ?: "default"
                val thinkingStr = cursor.getString(5) ?: "AUTO"
                val modelsJson = cursor.getString(6) ?: "[]"

                val meta = SupportedAgentsCatalog.findMeta(cmd)
                val models = try {
                    json.decodeFromString<List<String>>(modelsJson)
                } catch (_: Exception) {
                    meta.defaultModels
                }

                val thinking = try {
                    ThinkingLevel.valueOf(thinkingStr)
                } catch (_: Exception) {
                    ThinkingLevel.AUTO
                }

                list.add(
                    DiscoveredAgent(
                        id = id,
                        type = AgentType.fromBinary(cmd),
                        command = cmd,
                        path = path,
                        version = version,
                        isAvailable = true,
                        models = models,
                        selectedModel = selectedModel,
                        thinkingLevel = thinking
                    )
                )
            }
        }
        return list
    }

    fun saveDiscoveredAgents(serverId: String, agents: List<DiscoveredAgent>) {
        writableDatabase.beginTransaction()
        try {
            // 1. 更新 server_scan_meta
            val metaValues = ContentValues().apply {
                put("server_id", serverId)
                put("last_scanned_at", System.currentTimeMillis())
                put("has_scanned", 1)
            }
            writableDatabase.insertWithOnConflict(
                "server_scan_meta",
                null,
                metaValues,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            // 2. 清理旧 agent 记录并重写
            writableDatabase.delete("server_discovered_agents", "server_id = ?", arrayOf(serverId))

            agents.forEach { agent ->
                val agentValues = ContentValues().apply {
                    put("server_id", serverId)
                    put("agent_id", agent.id)
                    put("command", agent.command)
                    put("path", agent.path)
                    put("version", agent.version)
                    put("selected_model", agent.selectedModel)
                    put("thinking_level", agent.thinkingLevel.name)
                    put("models_json", json.encodeToString(agent.models))
                }
                writableDatabase.insertWithOnConflict(
                    "server_discovered_agents",
                    null,
                    agentValues,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    // ==================== Work 多会话持久化 ====================

    fun getWorkSessions(serverId: String): List<WorkSessionRecord> {
        val list = mutableListOf<WorkSessionRecord>()
        readableDatabase.rawQuery(
            """
            SELECT id, server_id, title, cwd, agent_id, selected_model, thinking_level, is_custom_title, cli_resume_id, created_at, updated_at
            FROM work_sessions
            WHERE server_id = ?
            ORDER BY updated_at DESC
            """.trimIndent(),
            arrayOf(serverId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    WorkSessionRecord(
                        id = cursor.getString(0),
                        serverId = cursor.getString(1),
                        title = cursor.getString(2),
                        cwd = cursor.getString(3) ?: "~",
                        agentId = cursor.getString(4),
                        selectedModel = cursor.getString(5) ?: "default",
                        thinkingLevel = cursor.getString(6) ?: "AUTO",
                        isCustomTitle = cursor.getInt(7) == 1,
                        cliResumeId = cursor.getString(8),
                        createdAt = cursor.getLong(9),
                        updatedAt = cursor.getLong(10)
                    )
                )
            }
        }
        return list
    }

    fun getWorkSession(sessionId: String): WorkSessionRecord? {
        readableDatabase.rawQuery(
            """
            SELECT id, server_id, title, cwd, agent_id, selected_model, thinking_level, is_custom_title, cli_resume_id, created_at, updated_at
            FROM work_sessions
            WHERE id = ?
            """.trimIndent(),
            arrayOf(sessionId)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return WorkSessionRecord(
                    id = cursor.getString(0),
                    serverId = cursor.getString(1),
                    title = cursor.getString(2),
                    cwd = cursor.getString(3) ?: "~",
                    agentId = cursor.getString(4),
                    selectedModel = cursor.getString(5) ?: "default",
                    thinkingLevel = cursor.getString(6) ?: "AUTO",
                    isCustomTitle = cursor.getInt(7) == 1,
                    cliResumeId = cursor.getString(8),
                    createdAt = cursor.getLong(9),
                    updatedAt = cursor.getLong(10)
                )
            }
        }
        return null
    }

    fun saveWorkSession(session: WorkSessionRecord) {
        val values = ContentValues().apply {
            put("id", session.id)
            put("server_id", session.serverId)
            put("title", session.title)
            put("cwd", session.cwd)
            put("agent_id", session.agentId)
            put("selected_model", session.selectedModel)
            put("thinking_level", session.thinkingLevel)
            put("is_custom_title", if (session.isCustomTitle) 1 else 0)
            put("cli_resume_id", session.cliResumeId)
            put("created_at", session.createdAt)
            put("updated_at", session.updatedAt)
        }
        writableDatabase.insertWithOnConflict(
            "work_sessions",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun deleteWorkSession(sessionId: String) {
        writableDatabase.delete("work_sessions", "id = ?", arrayOf(sessionId))
        writableDatabase.delete("work_messages", "session_id = ?", arrayOf(sessionId))
    }

    fun renameWorkSession(sessionId: String, title: String) {
        val values = ContentValues().apply {
            put("title", title)
            put("is_custom_title", 1)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("work_sessions", values, "id = ?", arrayOf(sessionId))
    }

    fun updateWorkSessionCwd(sessionId: String, cwd: String) {
        val values = ContentValues().apply {
            put("cwd", cwd)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("work_sessions", values, "id = ?", arrayOf(sessionId))
    }

    fun updateWorkSessionAgent(sessionId: String, agentId: String?, model: String, thinkingLevel: String) {
        val values = ContentValues().apply {
            put("agent_id", agentId)
            put("selected_model", model)
            put("thinking_level", thinkingLevel)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("work_sessions", values, "id = ?", arrayOf(sessionId))
    }

    // ==================== Work 消息历史持久化 ====================

    fun getWorkMessages(sessionId: String): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        readableDatabase.rawQuery(
            """
            SELECT id, sender, content, image_uris_json, steps_json, created_at
            FROM work_messages
            WHERE session_id = ?
            ORDER BY created_at ASC
            """.trimIndent(),
            arrayOf(sessionId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val senderStr = cursor.getString(1)
                val content = cursor.getString(2)
                val imagesJson = cursor.getString(3) ?: "[]"
                val stepsJson = cursor.getString(4) ?: "[]"
                val createdAt = cursor.getLong(5)

                val sender = try {
                    ChatSender.valueOf(senderStr)
                } catch (_: Exception) {
                    ChatSender.SYSTEM
                }

                val imageUris = try {
                    json.decodeFromString<List<String>>(imagesJson)
                } catch (_: Exception) {
                    emptyList()
                }

                val steps = try {
                    json.decodeFromString<List<ExecutionStep>>(stepsJson)
                } catch (_: Exception) {
                    emptyList()
                }

                // 过滤掉旧版本中可能被错误持久化的 Shell 终端命令行回显噪音
                if (sender == ChatSender.SYSTEM && (
                    content.trim().isEmpty() ||
                    content.trim() == ">" ||
                    content.startsWith(">") ||
                    content.startsWith("∙") ||
                    content.contains("[Context of prior discussion") ||
                    content.contains("[Current user request]") ||
                    content.contains("omp --mode") ||
                    content.contains("@omarchy") ||
                    content.contains("[coco@") ||
                    content.trim() == "'"
                )) {
                    continue
                }

                list.add(
                    ChatMessage(
                        id = id,
                        sender = sender,
                        content = content,
                        steps = steps,
                        isStreaming = false,
                        timestamp = createdAt,
                        imageUris = imageUris
                    )
                )
            }
        }
        return list
    }

    fun saveWorkMessage(sessionId: String, message: ChatMessage) {
        val values = ContentValues().apply {
            put("id", message.id)
            put("session_id", sessionId)
            put("sender", message.sender.name)
            put("content", message.content)
            put("image_uris_json", json.encodeToString(message.imageUris))
            put("steps_json", json.encodeToString(message.steps))
            put("created_at", message.timestamp)
        }
        writableDatabase.insertWithOnConflict(
            "work_messages",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        // 更新会话的 updated_at
        val sessionValues = ContentValues().apply {
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("work_sessions", sessionValues, "id = ?", arrayOf(sessionId))
    }

    fun saveWorkMessages(sessionId: String, messages: List<ChatMessage>) {
        writableDatabase.beginTransaction()
        try {
            messages.forEach { msg ->
                val values = ContentValues().apply {
                    put("id", msg.id)
                    put("session_id", sessionId)
                    put("sender", msg.sender.name)
                    put("content", msg.content)
                    put("image_uris_json", json.encodeToString(msg.imageUris))
                    put("steps_json", json.encodeToString(msg.steps))
                    put("created_at", msg.timestamp)
                }
                writableDatabase.insertWithOnConflict(
                    "work_messages",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun clearWorkMessages(sessionId: String) {
        writableDatabase.delete("work_messages", "session_id = ?", arrayOf(sessionId))
    }

    // ==================== Terminal 会话持久化 ====================

    fun getTerminalSessions(serverId: String): List<TerminalSessionRecord> {
        val list = mutableListOf<TerminalSessionRecord>()
        readableDatabase.rawQuery(
            """
            SELECT id, server_id, title, is_custom_title, tmux_session_name, created_at, updated_at
            FROM terminal_sessions
            WHERE server_id = ?
            ORDER BY created_at ASC
            """.trimIndent(),
            arrayOf(serverId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    TerminalSessionRecord(
                        id = cursor.getString(0),
                        serverId = cursor.getString(1),
                        title = cursor.getString(2),
                        isCustomTitle = cursor.getInt(3) == 1,
                        tmuxSessionName = cursor.getString(4),
                        createdAt = cursor.getLong(5),
                        updatedAt = cursor.getLong(6)
                    )
                )
            }
        }
        return list
    }

    fun saveTerminalSession(session: TerminalSessionRecord) {
        val values = ContentValues().apply {
            put("id", session.id)
            put("server_id", session.serverId)
            put("title", session.title)
            put("is_custom_title", if (session.isCustomTitle) 1 else 0)
            put("tmux_session_name", session.tmuxSessionName)
            put("created_at", session.createdAt)
            put("updated_at", session.updatedAt)
        }
        writableDatabase.insertWithOnConflict(
            "terminal_sessions",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun deleteTerminalSession(sessionId: String) {
        writableDatabase.delete("terminal_sessions", "id = ?", arrayOf(sessionId))
    }

    fun renameTerminalSession(sessionId: String, title: String) {
        val values = ContentValues().apply {
            put("title", title)
            put("is_custom_title", 1)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("terminal_sessions", values, "id = ?", arrayOf(sessionId))
    }
}
