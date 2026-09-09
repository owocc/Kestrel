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
import kotlinx.serialization.json.jsonArray
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
    fun sendPrompt(prompt: String, imageUris: List<String> = emptyList())
    fun onRemoteChunk(chunk: String)
    fun clearMessages()
    fun loadHistory(initialMessages: List<ChatMessage>)
    fun updateWorkingDirectory(newCwd: String)
}

/**
 * 通用多 Agent 协调执行器
 */
class UniversalAgentRunner(
    var sessionId: String = java.util.UUID.randomUUID().toString(),
    var cwd: String = "~",
    var cliResumeId: String? = null,
    initialAgent: DiscoveredAgent?,
    private val sendRawCommand: (String) -> Unit,
    var onMessageSaved: ((ChatMessage) -> Unit)? = null
) : IAgentRunner {
    private var remoteSessionId: String? = cliResumeId

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

    private fun isTerminalNoiseOrEcho(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return true
        if (trimmed.startsWith("[?") || trimmed.startsWith("]") || trimmed.startsWith("^")) return true
        if (trimmed.startsWith(">") || trimmed.startsWith("∙") || trimmed.startsWith("•")) return true
        if (trimmed.contains("@") && (trimmed.endsWith("$") || trimmed.endsWith("#") || trimmed.endsWith("%") || trimmed.contains("~ ✗") || trimmed.contains("~ $") || trimmed.contains("~]"))) return true
        if (trimmed.contains("omp --mode") || trimmed.contains("claude -p") || trimmed.contains("codex exec") || trimmed.contains("opencode run") || trimmed.startsWith("cd ")) return true
        if (trimmed.startsWith("[Context of prior discussion") || trimmed.startsWith("[Current user request]") || trimmed.startsWith("Human:") || trimmed.startsWith("Assistant:")) return true
        if (trimmed == "'" || trimmed == "''" || trimmed == "\\" || trimmed.endsWith("'")) return true
        return false
    }

    private val _rawLogs = MutableStateFlow("")
    override val rawLogs: StateFlow<String> = _rawLogs.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private val lineBuffer = StringBuilder()
    private val ansiRegex = Regex("\u001B\\[[0-9;?]*[a-zA-Z]|\u001B\\][^\u0007\u001B]*[\u0007\u001B\\\\]|\u001B[()][A-Z0-9]")
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
        remoteSessionId = null
        _messages.value = listOf(
            ChatMessage(
                id = "init_${System.currentTimeMillis()}",
                sender = ChatSender.SYSTEM,
                content = "已重置会话上下文。"
            )
        )
    }

    override fun loadHistory(initialMessages: List<ChatMessage>) {
        if (initialMessages.isNotEmpty()) {
            _messages.value = initialMessages
        }
    }

    override fun updateWorkingDirectory(newCwd: String) {
        cwd = newCwd
    }

    override fun sendPrompt(prompt: String, imageUris: List<String>) {
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

        // 1. 切换至当前会话工作目录 (cwd)
        if (cwd.isNotBlank() && cwd != "~") {
            cmdBuilder.append("cd \"$cwd\" && ")
        }

        // 2. 拼接对应 CLI 命令与会话标识 (仅在用户显式导入 CLI 历史会话时传递 --resume)
        val explicitResumeId = cliResumeId?.trim()?.takeIf { it.isNotBlank() }
        when (agent.type) {
            AgentType.OMP -> {
                cmdBuilder.append("omp --mode json -p")
                if (explicitResumeId != null) {
                    cmdBuilder.append(" --resume \"$explicitResumeId\"")
                }
                if (agent.selectedModel.isNotBlank() && agent.selectedModel != "default") {
                    cmdBuilder.append(" --model \"${agent.selectedModel}\"")
                }
                if (agent.thinkingLevel != ThinkingLevel.AUTO) {
                    cmdBuilder.append(" --thinking \"${agent.thinkingLevel.value}\"")
                }
            }
            AgentType.CLAUDE -> {
                cmdBuilder.append("claude -p")
                if (explicitResumeId != null) {
                    cmdBuilder.append(" --resume \"$explicitResumeId\"")
                }
                if (agent.selectedModel.isNotBlank() && agent.selectedModel != "default") {
                    cmdBuilder.append(" --model \"${agent.selectedModel}\"")
                }
            }
            AgentType.CODEX -> {
                cmdBuilder.append("codex exec")
                if (explicitResumeId != null) {
                    cmdBuilder.append(" --resume \"$explicitResumeId\"")
                }
            }
            AgentType.OPENCODE -> {
                cmdBuilder.append("opencode run")
                if (explicitResumeId != null) {
                    cmdBuilder.append(" --session \"$explicitResumeId\"")
                }
            }
            else -> {
                cmdBuilder.append("${agent.command} -p")
            }
        }
        // 3. 构建历史对话上下文：提取最近多轮问答，彻底解决单次请求无法获知上句话的问题
        val previousTurns = _messages.value.filter {
            (it.sender == ChatSender.USER || it.sender == ChatSender.AGENT) &&
            it.content.isNotBlank() &&
            !it.isStreaming
        }.takeLast(6)

        val promptWithContext = if (previousTurns.isNotEmpty()) {
            val contextBlock = previousTurns.joinToString("\n") { msg ->
                val role = if (msg.sender == ChatSender.USER) "Human" else "Assistant"
                "$role: ${msg.content.trim()}"
            }
            "[Context of prior discussion in this session]:\n$contextBlock\n\n[Current user request]:\n$prompt"
        } else {
            prompt
        }

        val fullPromptWithImages = if (imageUris.isNotEmpty()) {
            val imageListStr = imageUris.joinToString(", ")
            "$promptWithContext\n\n[Attached Images: $imageListStr]"
        } else {
            promptWithContext
        }

        // 使用标准 POSIX / Fish / Bash 通用单引号转义，保证跨 Shell 兼容性
        val safePrompt = fullPromptWithImages.replace("'", "'\\''")
        cmdBuilder.append(" '$safePrompt'")
        val fullCommand = cmdBuilder.toString()

        _rawLogs.value = (_rawLogs.value + "\n\n[$timeStr] >>> [USER_PROMPT] $prompt\n[$timeStr] >>> [COMMAND] $fullCommand").takeLast(60000)
        addEventLog(timeStr, "prompt", "用户下发任务 (${agent.type.displayName})", prompt, AgentEventLevel.INFO)

        // 保证客户端展现的气泡只包含当前用户纯净提问，绝对不泄露前置的 [Context ...] 构造文本
        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = ChatSender.USER,
            content = prompt,
            imageUris = imageUris
        )
        _messages.value = _messages.value + userMsg
        onMessageSaved?.invoke(userMsg)

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

            val rawLine = lineBuffer.substring(0, newlineIndex)
            lineBuffer.delete(0, newlineIndex + 1)

            val cleanLine = rawLine.replace(ansiRegex, "").trim()
            if (cleanLine.isBlank()) continue

            // 1. 优先提取行内的有效 JSON 数据块进行结构化解析
            val firstBrace = cleanLine.indexOf('{')
            val lastBrace = cleanLine.lastIndexOf('}')
            var handledJson = false
            if (firstBrace != -1 && lastBrace > firstBrace) {
                val candidateJson = cleanLine.substring(firstBrace, lastBrace + 1)
                handledJson = processJsonLine(candidateJson, timeStr)
            }

            // 2. 将无 JSON 包裹的内容区分为致命错误 vs 普通运行时日志
            if (!handledJson && _isBusy.value) {
                if (!isTerminalNoiseOrEcho(cleanLine)) {
                    val isFatalError = cleanLine.startsWith("Error:", ignoreCase = true) ||
                            cleanLine.contains("command not found", ignoreCase = true) ||
                            cleanLine.contains("No such file or directory", ignoreCase = true) ||
                            cleanLine.contains("exit=failure", ignoreCase = true)

                    if (isFatalError) {
                        addEventLog(timeStr, "error", cleanLine, level = AgentEventLevel.ERROR)
                        val sysMsg = ChatMessage(
                            id = "sys_${System.currentTimeMillis()}_${(0..999).random()}",
                            sender = ChatSender.SYSTEM,
                            content = cleanLine
                        )
                        _messages.value = _messages.value + sysMsg
                        onMessageSaved?.invoke(sysMsg)

                        _isBusy.value = false
                        _currentStatus.value = null
                        markCurrentAgentDone()
                    } else {
                        // SDK 警告、环境提示等属于后台运行时日志，记入执行日志，不干扰前台聊天主视图
                        addEventLog(timeStr, "runtime_notice", cleanLine, level = AgentEventLevel.INFO)
                    }
                }
            }
        }
    }

    private fun processJsonLine(line: String, timeStr: String): Boolean {
        try {
            val obj = json.parseToJsonElement(line).jsonObject
            val type = obj["type"]?.jsonPrimitive?.content ?: return false

            when (type) {
                "session" -> {
                    val sid = obj["id"]?.jsonPrimitive?.content
                    if (!sid.isNullOrBlank()) {
                        remoteSessionId = sid
                        addEventLog(timeStr, "session", "已绑定远端会话: $sid", level = AgentEventLevel.INFO)
                    }
                }
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
                "message_end" -> {
                    val msgObj = obj["message"]?.jsonObject
                    if (msgObj != null && msgObj["role"]?.jsonPrimitive?.content == "assistant") {
                        val contentArr = msgObj["content"]?.jsonArray
                        contentArr?.forEach { item ->
                            val itemObj = item.jsonObject
                            if (itemObj["type"]?.jsonPrimitive?.content == "text") {
                                val fullText = itemObj["text"]?.jsonPrimitive?.content ?: ""
                                if (fullText.isNotBlank()) {
                                    setAgentTextIfEmpty(fullText)
                                }
                            }
                        }
                    }
                }
                "turn_end" -> {
                    _isBusy.value = false
                    _currentStatus.value = null
                    markCurrentAgentDone()
                }
                "agent_end" -> {
                    _isBusy.value = false
                    _currentStatus.value = null
                    markCurrentAgentDone()
                    addEventLog(timeStr, "agent_end", "Agent 任务执行完成", level = AgentEventLevel.INFO)
                }
            }
            return true
        } catch (_: Exception) {
            return false
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

    private fun setAgentTextIfEmpty(text: String) {
        val list = _messages.value.toMutableList()
        val index = list.indexOfLast { it.sender == ChatSender.AGENT }
        if (index != -1) {
            val cur = list[index]
            if (cur.content.isBlank()) {
                list[index] = cur.copy(content = text)
                _messages.value = list
            }
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
            if (cur.content.isBlank() && cur.steps.isEmpty()) {
                // 若该 Agent 占位气泡尚未产生任何实质回复或步骤，清理空的占位气泡
                list.removeAt(index)
            } else {
                val doneMsg = cur.copy(isStreaming = false)
                list[index] = doneMsg
                onMessageSaved?.invoke(doneMsg)
            }
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
