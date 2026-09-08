package com.bettershell.app.terminal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class ChatSender {
    USER,
    AGENT,
    SYSTEM
}

enum class StepType {
    THINKING,
    TOOL_CALL,
    TOOL_RESULT,
    ERROR
}

data class ExecutionStep(
    val id: String,
    val type: StepType,
    val toolName: String? = null,
    val title: String,
    val detail: String? = null,
    val isRunning: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val sender: ChatSender,
    val content: String,
    val steps: List<ExecutionStep> = emptyList(),
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Multica 风格的高级 Agent 执行协调器
 * 负责接收并解析 omp --mode json 输出的完整生命周期事件：
 * - 思考阶段 (Thinking)
 * - 工具调用与参数 (Tool Use)
 * - 工具执行结果 (Tool Result)
 * - 文本流增量 (Text Delta)
 * - 任务收敛与完成 (Agent End)
 */
class OmpAgentClient(
    private val sendRawCommand: (String) -> Unit
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "init",
                sender = ChatSender.SYSTEM,
                content = "omp (Oh My Pi) Agent 引擎已就绪。支持深度思考、文件读写、语法分析与命令自执行。"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isAgentBusy = MutableStateFlow(false)
    val isAgentBusy: StateFlow<Boolean> = _isAgentBusy.asStateFlow()

    private val _currentStatus = MutableStateFlow<String?>(null)
    val currentStatus: StateFlow<String?> = _currentStatus.asStateFlow()

    private val _rawLogs = MutableStateFlow<String>("")
    val rawLogs: StateFlow<String> = _rawLogs.asStateFlow()

    // 仿 Multica 结构化事件流 (一行一个事件记录)
    private val _eventLogs = MutableStateFlow<List<AgentEventLogItem>>(emptyList())
    val eventLogs: StateFlow<List<AgentEventLogItem>> = _eventLogs.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private var lineBuffer = StringBuilder()
    fun sendPrompt(prompt: String) {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        _rawLogs.value = (_rawLogs.value + "\n\n[$timeStr] >>> [USER_PROMPT] $prompt\n[$timeStr] >>> [SEND_COMMAND] omp --mode json -p '$prompt'\n").takeLast(60000)
        addEventLog(timeStr, "prompt", "用户下发指令", prompt, AgentEventLevel.INFO)
        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = ChatSender.USER,
            content = prompt
        )
        _messages.value = _messages.value + userMsg
        _isAgentBusy.value = true
        _currentStatus.value = "Thinking..."

        // 占位一个 Assistant 响应，用以承载执行流和 steps
        val assistantMsg = ChatMessage(
            id = "agent_${System.currentTimeMillis()}",
            sender = ChatSender.AGENT,
            content = "",
            isStreaming = true
        )
        _messages.value = _messages.value + assistantMsg

        val safePrompt = prompt.replace("'", "'\\''")
        sendRawCommand("omp --mode json -p '$safePrompt'\n")
    }

    /**
     * 处理来自 SSH 管道的增量文本 chunk (流式行缓冲，一行一行精准解析，不重复投递)
     */
    fun onRemoteOutput(chunk: String) {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
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
                    addEventLog(timeStr, "agent_start", "Agent 任务已启动", level = AgentEventLevel.INFO)
                }
                "tool_execution_start" -> {
                    val toolName = obj["toolName"]?.jsonPrimitive?.content ?: "tool"
                    val intent = obj["intent"]?.jsonPrimitive?.content
                        ?: obj["args"]?.toString()
                        ?: "调用工具: $toolName"
                    _rawLogs.value = (_rawLogs.value + "\n[$timeStr] [STEP_START] $toolName: $intent").takeLast(60000)
                    _currentStatus.value = formatToolStatus(toolName)
                    addEventLog(timeStr, "tool_start", "执行工具: $toolName", intent, AgentEventLevel.TOOL)
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
                    addEventLog(timeStr, "tool_end", "工具执行完毕", resultStr?.take(200), AgentEventLevel.STEP)
                }

                "message_update" -> {
                    val eventObj = obj["assistantMessageEvent"]?.jsonObject
                    if (eventObj != null) {
                        val eventType = eventObj["type"]?.jsonPrimitive?.content
                        if (eventType == "text_delta") {
                            val delta = eventObj["delta"]?.jsonPrimitive?.content ?: ""
                            appendAgentText(delta)
                        }
                    }
                }

                "agent_end" -> {
                    _isAgentBusy.value = false
                    _currentStatus.value = null
                    markCurrentAgentDone()
                    addEventLog(timeStr, "agent_end", "任务已完成 (Agent End)", level = AgentEventLevel.INFO)
                }
            }
        } catch (_: Exception) {
            // 忽略异常 JSON
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

    private fun getToolDisplayTitle(tool: String): String {
        return when (tool.lowercase()) {
            "bash", "exec" -> "执行终端命令"
            "read" -> "读取文件内容"
            "write" -> "创建/覆盖文件"
            "edit", "ast_edit" -> "修改代码结构"
            "grep", "glob" -> "检索项目文件"
            "lsp" -> "查询代码智能/定义"
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
}
