package com.bettershell.app.agent

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Agent 缓存与发现管理器 (对标 Multica 针对每个 Server 缓存 runtimes)
 */
class AgentDiscoveryRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("agent_discovery_cache", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    // 默认内置兜底 Agent：omp (即便还未探测到，也保证开箱可用)
    val defaultFallbackAgent = DiscoveredAgent(
        id = "omp",
        type = AgentType.OMP,
        command = "omp",
        path = "/usr/local/bin/omp",
        models = listOf("default", "gemini-3.8-flash", "claude-sonnet", "gpt-4o", "deepseek-chat"),
        selectedModel = "default",
        thinkingLevel = ThinkingLevel.AUTO
    )

    fun getCachedAgents(serverId: String): List<DiscoveredAgent> {
        val raw = prefs.getString("agents_$serverId", null) ?: return listOf(defaultFallbackAgent)
        return try {
            json.decodeFromString<List<DiscoveredAgent>>(raw).ifEmpty { listOf(defaultFallbackAgent) }
        } catch (_: Exception) {
            listOf(defaultFallbackAgent)
        }
    }

    fun saveAgents(serverId: String, agents: List<DiscoveredAgent>) {
        try {
            val serialized = json.encodeToString(agents)
            prefs.edit().putString("agents_$serverId", serialized).apply()
        } catch (_: Exception) {
        }
    }

    fun getSelectedAgentId(serverId: String): String {
        return prefs.getString("selected_agent_$serverId", "omp") ?: "omp"
    }

    fun saveSelectedAgentId(serverId: String, agentId: String) {
        prefs.edit().putString("selected_agent_$serverId", agentId).apply()
    }

    /**
     * 解析远程探针脚本输出的 JSON
     */
    fun parseProbeResult(output: String): List<DiscoveredAgent>? {
        val marker = "BETTERSHELL_AGENT_PROBE_RESULT:"
        val index = output.indexOf(marker)
        if (index == -1) return null

        val jsonString = output.substring(index + marker.length).trim().lines().firstOrNull() ?: return null
        return try {
            val root = json.parseToJsonElement(jsonString).jsonArray
            val list = mutableListOf<DiscoveredAgent>()
            for (elem in root) {
                val obj = elem.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: continue
                val cmd = obj["cmd"]?.jsonPrimitive?.content ?: id
                val path = obj["path"]?.jsonPrimitive?.content ?: ""
                val version = obj["version"]?.jsonPrimitive?.content ?: ""

                val type = AgentType.fromBinary(cmd)
                val defaultModels = when (type) {
                    AgentType.OMP -> listOf("default", "gemini-3.8-flash", "claude-sonnet", "gpt-4o", "deepseek-chat")
                    AgentType.CLAUDE -> listOf("default", "claude-sonnet-4-6", "claude-haiku-4-5")
                    AgentType.CODEX -> listOf("default", "gpt-5-codex", "codex-mini")
                    else -> listOf("default")
                }

                list.add(
                    DiscoveredAgent(
                        id = id,
                        type = type,
                        command = cmd,
                        path = path,
                        version = version,
                        models = defaultModels,
                        selectedModel = "default",
                        thinkingLevel = ThinkingLevel.AUTO
                    )
                )
            }
            if (list.none { it.id == "omp" }) {
                list.add(0, defaultFallbackAgent)
            }
            list
        } catch (_: Exception) {
            null
        }
    }
}
