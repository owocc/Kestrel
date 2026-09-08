package com.bettershell.app.agent

import com.bettershell.app.terminal.AgentEventLevel
import com.bettershell.app.terminal.AgentEventLogItem
import com.bettershell.app.terminal.ChatMessage
import com.bettershell.app.terminal.ChatSender
import com.bettershell.app.terminal.ExecutionStep
import com.bettershell.app.terminal.StepType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface IAgentRunner {
    val currentAgent: StateFlow<DiscoveredAgent?>
    val messages: StateFlow<List<ChatMessage>>
    val isBusy: StateFlow<Boolean>
    val currentStatus: StateFlow<String?>
    val eventLogs: StateFlow<List<AgentEventLogItem>>
    val rawLogs: StateFlow<String>

    fun setAgent(agent: DiscoveredAgent?)
    fun setModel(model: String)
    fun setThinkingLevel(level: ThinkingLevel)
    fun sendPrompt(prompt: String)
    fun onRemoteChunk(chunk: String)
    fun clearMessages()
}

/**
 * 通用多 Agent 协调执行器
 */
class UniversalAgentRunner(
    initialAgent: DiscoveredAgent?,
    private val sendRawCommand: (String) -> Unit
) : IAgentRunner {

    private val _currentAgent = MutableStateFlow(initialAgent)
    override val currentAgent: StateFlow<DiscoveredAgent?> = _currentAgent.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "init",
                sender = ChatSender.SYSTEM,
                content = if (initialAgent != null) {
                    "${initialAgent.type.displayName} Agent 引擎已就绪。"
                } else {
                    "尚未探测到已安装的 AI Agent。请点击输入框左侧 '+' 进行扫描或配置。"
                }
            )
        )
    )
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    override val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _currentStatus = MutableStateFlow<String?>(null)
    override val currentStatus: StateFlow<String?> = _currentStatus.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<AgentEventLogItem>>(emptyList())
    override val eventLogs: StateFlow<List<AgentEventLogItem>> = _eventLogs.asStateFlow()

    private val _rawLogs = MutableStateFlow("")
    override val rawLogs: StateFlow<String> = _rawLogs.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private val lineBuffer = StringBuilder()

    override fun setAgent(agent: DiscoveredAgent?) {
        _currentAgent.value = agent
        if (agent != null) {
            addEventLog(now(), "agent_switch", "当前生效 Agent: ${agent.type.displayName}", level = AgentEventLevel.INFO)
        }
    }

    override fun setModel(model: String) {
        val cur = _currentAgent.value ?: return
        _currentAgent.value = cur.copy(selectedModel = model)
        addEventLog(now(), "model_switch", "切换模型为: $model", level = AgentEventLevel.INFO)
    }

    override fun setThinkingLevel(level: ThinkingLevel) {
        val cur = _currentAgent.value ?: return
        _currentAgent.value = cur.copy(thinkingLevel = level)
        addEventLog(now(), "thinking_switch", "思考级别设定为: ${level.displayName}", level = AgentEventLevel.INFO)
    }

    override fun clearMessages() {
        _messages.value = listOf(
            ChatMessage(
                id = "init_${System.currentTimeMillis()}",
                sender = ChatSender.SYSTEM,
                content = "已重置会话上下文。"
            )
        )
    }

    override fun sendPrompt(prompt: String) {
        val timeStr = now()
        val agent = _currentAgent.value
        if (agent == null) {
            _messages.value = _messages.value + ChatMessage(
                id = "err_${System.currentTimeMillis()}",
                sender = ChatSender.SYSTEM,
                content = "当前未选择可用的 Agent，请点击 '+' 按钮检查已安装的 Agent。"
            )
            return
        }

        val cmdBuilder = StringBuilder()
        when (agent.type) {
            AgentType.OMP -> {
                cmdBuilder.append("omp --mode json -p")
                if (agent.selectedModel.isNotBlank() && agent.selectedModel != "default") {
                    cmdBuilder.append(" --model \"${agent.selectedModel}\"")
                }
                if (agent.thinkingLevel != ThinkingLevel.AUTO) {
                    cmdBuilder.append(" --thinking \"${agent.thinkingLevel.value}\"")
                }
            }
            AgentType.CLAUDE -> {
                cmdBuilder.append("claude -p")
                if (agent.selectedModel.isNotBlank() && agent.selectedModel != "default") {
                    cmdBuilder.append(" --model \"${agent.selectedModel}\"")
                }
            }
            AgentType.CODEX -> {
                cmdBuilder.append("codex exec")
            }
            else -> {
                cmdBuilder.append("${agent.command} -p")
            }
        }

        val safePrompt = prompt.replace("'", "'\\''")
        cmdBuilder.append(" '$safePrompt'\n")
        val fullCommand = cmdBuilder.toString()

        _rawLogs.value = (_rawLogs.value + "\n\n[$timeStr] >>> [USER_PROMPT] $prompt\n[$timeStr] >>> [COMMAND] $fullCommand").takeLast(60000)
        addEventLog(timeStr, "prompt", "用户下发任务 (${agent.type.displayName})", prompt, AgentEventLevel.INFO)

        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = ChatSender.USER,
            content = prompt
        )
        _messages.value = _messages.value + userMsg
        _isBusy.value = true
        _currentStatus.value = "Thinking..."

        val assistantMsg = ChatMessage(
            id = "agent_${System.currentTimeMillis()}",
            sender = ChatSender.AGENT,
            content = "",
            isStreaming = true
        )
        _messages.value = _messages.value + assistantMsg

        sendRawCommand(fullCommand)
    }

    override fun onRemoteChunk(chunk: String) {
        val timeStr = now()
        _rawLogs.value = (_rawLogs.value + "\n[$timeStr] <<< $chunk").takeLast(60000)

        lineBuffer.append(chunk)

        while (true) {
            val newlineIndex = lineBuffer.indexOf('\n')
            if (newlineIndex == -1) break

            val completeLine = lineBuffer.substring(0, newlineIndex).trim()
            lineBuffer.delete(0, newlineIndex + 1)

            if (completeLine.startsWith("{") && completeLine.endsWith("}")) {
                processJsonLine(completeLine, timeStr)
            }
        }
    }

    private fun processJsonLine(line: String, timeStr: String) {
        try {
            val obj = json.parseToJsonElement(line).jsonObject
            val type = obj["type"]?.jsonPrimitive?.content ?: return

            when (type) {
                "agent_start" -> {
                    addEventLog(timeStr, "agent_start", "Agent 任务执行开始", level = AgentEventLevel.INFO)
                }
                "tool_execution_start" -> {
                    val toolName = obj["toolName"]?.jsonPrimitive?.content ?: "tool"
                    val intent = obj["intent"]?.jsonPrimitive?.content
                        ?: obj["args"]?.toString()
                        ?: "调用工具: $toolName"
                    _rawLogs.value = (_rawLogs.value + "\n[$timeStr] [STEP_START] $toolName: $intent").takeLast(60000)
                    _currentStatus.value = formatToolStatus(toolName)
                    addEventLog(timeStr, "tool_start", "调用工具: $toolName", intent, AgentEventLevel.TOOL)
                    addStepToCurrentAgent(
                        ExecutionStep(
                            id = obj["toolCallId"]?.jsonPrimitive?.content ?: "step_${System.currentTimeMillis()}",
                            type = StepType.TOOL_CALL,
                            toolName = toolName,
                            title = getToolDisplayTitle(toolName),
                            detail = intent,
                            isRunning = true
                        )
                    )
                }
                "tool_execution_end" -> {
                    val toolCallId = obj["toolCallId"]?.jsonPrimitive?.content
                    val resultStr = obj["result"]?.toString()
                    finishStep(toolCallId, resultStr)
                    _currentStatus.value = "Thinking..."
                    addEventLog(timeStr, "tool_end", "工具完成执行", resultStr?.take(200), AgentEventLevel.STEP)
                }
                "message_update" -> {
                    val eventObj = obj["assistantMessageEvent"]?.jsonObject
                    if (eventObj != null && eventObj["type"]?.jsonPrimitive?.content == "text_delta") {
                        val delta = eventObj["delta"]?.jsonPrimitive?.content ?: ""
                        appendAgentText(delta)
                    }
                }
                "agent_end" -> {
                    _isBusy.value = false
                    _currentStatus.value = null
                    markCurrentAgentDone()
                    addEventLog(timeStr, "agent_end", "Agent 任务执行完成", level = AgentEventLevel.INFO)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun appendAgentText(delta: String) {
        val list = _messages.value.toMutableList()
        val index = list.indexOfLast { it.sender == ChatSender.AGENT }
        if (index != -1) {
            val cur = list[index]
            list[index] = cur.copy(content = cur.content + delta)
            _messages.value = list
        }
    }

    private fun addStepToCurrentAgent(step: ExecutionStep) {
        val list = _messages.value.toMutableList()
        val index = list.indexOfLast { it.sender == ChatSender.AGENT }
        if (index != -1) {
            val cur = list[index]
            list[index] = cur.copy(steps = cur.steps + step)
            _messages.value = list
        }
    }

    private fun finishStep(toolCallId: String?, result: String?) {
        val list = _messages.value.toMutableList()
        val index = list.indexOfLast { it.sender == ChatSender.AGENT }
        if (index != -1) {
            val cur = list[index]
            val updatedSteps = cur.steps.map { s ->
                if (toolCallId != null && s.id == toolCallId) {
                    s.copy(isRunning = false, detail = result ?: s.detail)
                } else if (s.isRunning) {
                    s.copy(isRunning = false)
                } else {
                    s
                }
            }
            list[index] = cur.copy(steps = updatedSteps)
            _messages.value = list
        }
    }

    private fun markCurrentAgentDone() {
        val list = _messages.value.toMutableList()
        val index = list.indexOfLast { it.sender == ChatSender.AGENT }
        if (index != -1) {
            val cur = list[index]
            list[index] = cur.copy(isStreaming = false)
            _messages.value = list
        }
    }

    private fun addEventLog(timeStr: String, type: String, summary: String, detail: String? = null, level: AgentEventLevel = AgentEventLevel.INFO) {
        val item = AgentEventLogItem(
            id = "evt_${System.currentTimeMillis()}_${(0..999).random()}",
            timeStr = timeStr,
            type = type,
            summary = summary,
            detail = detail,
            level = level
        )
        _eventLogs.value = (_eventLogs.value + item).takeLast(200)
    }

    private fun getToolDisplayTitle(tool: String): String {
        return when (tool.lowercase()) {
            "bash", "exec" -> "执行终端命令"
            "read" -> "读取文件内容"
            "write" -> "创建/覆盖文件"
            "edit", "ast_edit" -> "修改代码结构"
            "grep", "glob" -> "检索项目文件"
            "lsp" -> "查询代码定义"
            "web_search" -> "网络检索"
            else -> "调用工具: $tool"
        }
    }

    private fun formatToolStatus(tool: String): String {
        return when (tool.lowercase()) {
            "bash" -> "Running command..."
            "read", "glob" -> "Reading files..."
            "grep" -> "Searching code..."
            "edit", "write" -> "Applying edits..."
            "lsp" -> "Inspecting symbols..."
            "web_search" -> "Searching web..."
            else -> "Calling $tool..."
        }
    }

    private fun now(): String {
        return java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
    }
}
