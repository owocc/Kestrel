package com.bettershell.app.terminal

enum class AgentEventLevel {
    INFO,
    TOOL,
    STEP,
    ERROR
}

data class AgentEventLogItem(
    val id: String,
    val timeStr: String,
    val type: String,
    val summary: String,
    val detail: String? = null,
    val level: AgentEventLevel = AgentEventLevel.INFO
)
