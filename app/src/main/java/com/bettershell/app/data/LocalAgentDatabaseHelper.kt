package com.bettershell.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bettershell.app.agent.AgentType
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.agent.ThinkingLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 本地嵌入式 SQL (LibSQL / SQLite) 存储
 * 持久化记录每个服务器的扫描状态与探测到的 Agent 列表
 */
class LocalAgentDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "bettershell_agents.db", null, 1) {

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS server_scan_meta")
        db.execSQL("DROP TABLE IF EXISTS server_discovered_agents")
        onCreate(db)
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
}
