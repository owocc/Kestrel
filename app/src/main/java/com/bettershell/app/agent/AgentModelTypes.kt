package com.bettershell.app.agent

import kotlinx.serialization.Serializable

@Serializable
enum class AgentType(val id: String, val displayName: String, val binaryName: String) {
    OMP("omp", "Oh My Pi", "omp"),
    CLAUDE("claude", "Claude Code", "claude"),
    CODEX("codex", "Codex", "codex"),
    OPENCODE("opencode", "OpenCode", "opencode"),
    CODEARTS("codearts", "CodeArts", "codearts"),
    DEVECO("deveco", "DevEco", "deveco"),
    OPENCLAW("openclaw", "OpenClaw", "openclaw"),
    HERMES("hermes", "Hermes", "hermes"),
    PI("pi", "Pi Agent", "pi"),
    CURSOR("cursor", "Cursor Agent", "cursor-agent"),
    COPILOT("copilot", "Copilot", "copilot"),
    KIMI("kimi", "Kimi CLI", "kimi"),
    REASONIX("reasonix", "Reasonix", "reasonix"),
    QODER("qoder", "Qoder", "qodercli"),
    TRAE("trae", "Trae", "traecli"),
    GROK("grok", "Grok", "grok"),
    CUSTOM("custom", "Custom Agent", "");

    companion object {
        fun fromBinary(binary: String): AgentType {
            return entries.firstOrNull { it.binaryName.equals(binary, ignoreCase = true) || it.id.equals(binary, ignoreCase = true) } ?: CUSTOM
        }
    }
}

@Serializable
enum class ThinkingLevel(val value: String, val displayName: String) {
    AUTO("auto", "自动 (Auto)"),
    OFF("off", "关闭 (Off)"),
    LOW("low", "轻度 (Low)"),
    MEDIUM("medium", "标准 (Medium)"),
    HIGH("high", "深入 (High)"),
    MAX("max", "极强 (Max)")
}

@Serializable
data class DiscoveredAgent(
    val id: String,
    val type: AgentType,
    val command: String,
    val path: String,
    val version: String = "",
    val isAvailable: Boolean = true,
    val models: List<String> = emptyList(),
    val selectedModel: String = "default",
    val thinkingLevel: ThinkingLevel = ThinkingLevel.AUTO
)
