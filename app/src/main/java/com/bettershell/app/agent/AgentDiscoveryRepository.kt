package com.bettershell.app.agent

import android.content.Context
import com.bettershell.app.data.LocalAgentDatabaseHelper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Agent 发现与本地 SQL 缓存仓库
 * - 只有首次进入或用户手动触发时才进行扫描
 * - 存储到本地 LibSQL / SQLite 数据库中
 * - 绝不默认假定用户安装了 omp，未安装就是未安装，完全取决于真实扫描
 */
class AgentDiscoveryRepository(context: Context) {
    private val dbHelper = LocalAgentDatabaseHelper(context.applicationContext)
    private val json = Json { ignoreUnknownKeys = true }

    fun hasScanned(serverId: String): Boolean {
        return dbHelper.hasScanned(serverId)
    }

    fun getCachedAgents(serverId: String): List<DiscoveredAgent> {
        return dbHelper.getDiscoveredAgents(serverId)
    }

    fun saveAgents(serverId: String, agents: List<DiscoveredAgent>) {
        dbHelper.saveDiscoveredAgents(serverId, agents)
    }

    fun getSelectedAgentId(serverId: String): String? {
        return dbHelper.getSelectedAgentId(serverId)
    }

    fun saveSelectedAgentId(serverId: String, agentId: String) {
        dbHelper.setSelectedAgentId(serverId, agentId)
    }

    /**
     * 解析远程探针脚本输出的 JSON (严格对应实际探测到的真实结果，不自动伪造任何 agent)
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

                val meta = SupportedAgentsCatalog.findMeta(cmd)
                list.add(
                    DiscoveredAgent(
                        id = id,
                        type = AgentType.fromBinary(cmd),
                        command = cmd,
                        path = path,
                        version = version,
                        models = meta.defaultModels,
                        selectedModel = "default",
                        thinkingLevel = ThinkingLevel.AUTO
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }
}
