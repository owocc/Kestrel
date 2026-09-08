package com.bettershell.app.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class ServerRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val file: File
        get() = File(context.filesDir, "servers.json")

    private val _servers = MutableStateFlow<List<ServerConfig>>(emptyList())
    val servers: StateFlow<List<ServerConfig>> = _servers.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        loadServers()
    }

    private fun loadServers() {
        scope.launch {
            val list = withContext(Dispatchers.IO) {
                if (file.exists()) {
                    try {
                        val content = file.readText()
                        json.decodeFromString<List<ServerConfig>>(content)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        defaultServers()
                    }
                } else {
                    val defaults = defaultServers()
                    saveToFile(defaults)
                    defaults
                }
            }
            _servers.value = list
        }
    }

    private fun defaultServers(): List<ServerConfig> {
        return listOf(
            ServerConfig(
                id = "mock-agent-1",
                name = "Agent Shell (Local Demo)",
                host = "127.0.0.1",
                port = 22,
                username = "agent",
                authType = AuthType.DEMO_MOCK,
                description = "本地安全沙箱模拟环境，用于探索 Coding Agent",
                isMock = true,
                startupScript = "echo \"Starting AI Agent Runtime...\"\nsource /opt/agent/env.sh\nagent --status",
                lastConnected = System.currentTimeMillis()
            ),
            ServerConfig(
                id = UUID.randomUUID().toString(),
                name = "Cloud GPU Server",
                host = "192.168.1.100",
                port = 22,
                username = "ubuntu",
                authType = AuthType.PASSWORD,
                description = "高性能云端工作站，运行模型推理与后台编译"
            )
        )
    }

    suspend fun addServer(server: ServerConfig) {
        withContext(Dispatchers.IO) {
            val updated = _servers.value + server
            _servers.value = updated
            saveToFile(updated)
        }
    }

    suspend fun updateServer(server: ServerConfig) {
        withContext(Dispatchers.IO) {
            val updated = _servers.value.map {
                if (it.id == server.id) server else it
            }
            _servers.value = updated
            saveToFile(updated)
        }
    }

    suspend fun deleteServer(id: String) {
        withContext(Dispatchers.IO) {
            val updated = _servers.value.filterNot { it.id == id }
            _servers.value = updated
            saveToFile(updated)
        }
    }

    fun getServer(id: String): ServerConfig? {
        return _servers.value.find { it.id == id }
    }

    private fun saveToFile(list: List<ServerConfig>) {
        try {
            val content = json.encodeToString(list)
            file.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
