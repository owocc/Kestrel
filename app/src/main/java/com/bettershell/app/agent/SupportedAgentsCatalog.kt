package com.bettershell.app.agent

import kotlinx.serialization.Serializable

@Serializable
data class SupportedAgentMeta(
    val id: String,
    val command: String,
    val displayName: String,
    val shortName: String,
    val avatarBgColorHex: Long,
    val avatarTextColorHex: Long = 0xFFFFFFFF,
    val description: String,
    val defaultModels: List<String> = listOf("default")
)

object SupportedAgentsCatalog {
    /**
     * 完整对标 Multica 的 defaultAgentCommandNames
     */
    val ALL_SUPPORTED_AGENTS = listOf(
        SupportedAgentMeta("omp", "omp", "Oh My Pi", "OMP", 0xFF6366F1, description = "IDE wired terminal coding agent", defaultModels = listOf("default", "gemini-3.8-flash", "claude-sonnet", "gpt-4o", "deepseek-chat")),
        SupportedAgentMeta("claude", "claude", "Claude Code", "CC", 0xFFD97706, description = "Anthropic Claude coding agent", defaultModels = listOf("default", "claude-sonnet-4-6", "claude-haiku-4-5")),
        SupportedAgentMeta("codex", "codex", "Codex", "CDX", 0xFF10B981, description = "OpenAI Codex agent environment", defaultModels = listOf("default", "gpt-5-codex")),
        SupportedAgentMeta("opencode", "opencode", "OpenCode", "OC", 0xFF3B82F6, description = "OpenCode CLI coding agent", defaultModels = listOf("default")),
        SupportedAgentMeta("pi", "pi", "Pi Agent", "PI", 0xFF8B5CF6, description = "Minimal coding agent harness", defaultModels = listOf("default")),
        SupportedAgentMeta("cursor", "cursor-agent", "Cursor", "CUR", 0xFF06B6D4, description = "Cursor background agent CLI", defaultModels = listOf("default")),
        SupportedAgentMeta("kimi", "kimi", "Kimi CLI", "KIM", 0xFFEC4899, description = "Moonshot Kimi coding agent", defaultModels = listOf("default")),
        SupportedAgentMeta("copilot", "copilot", "Copilot", "COP", 0xFF4B5563, description = "GitHub Copilot agent CLI", defaultModels = listOf("default")),
        SupportedAgentMeta("codearts", "codearts", "CodeArts", "CA", 0xFFE11D48, description = "Huawei CodeArts CLI agent", defaultModels = listOf("default")),
        SupportedAgentMeta("deveco", "deveco", "DevEco", "DEV", 0xFF2563EB, description = "DevEco studio terminal agent", defaultModels = listOf("default")),
        SupportedAgentMeta("openclaw", "openclaw", "OpenClaw", "OCL", 0xFFF59E0B, description = "OpenClaw autonomous agent", defaultModels = listOf("default")),
        SupportedAgentMeta("hermes", "hermes", "Hermes", "HER", 0xFF14B8A6, description = "Hermes coding agent", defaultModels = listOf("default")),
        SupportedAgentMeta("qoder", "qodercli", "Qoder", "QOD", 0xFF84CC16, description = "Qoder terminal agent", defaultModels = listOf("default")),
        SupportedAgentMeta("trae", "traecli", "Trae", "TR", 0xFF6D28D9, description = "ByteDance Trae CLI agent", defaultModels = listOf("default")),
        SupportedAgentMeta("grok", "grok", "Grok CLI", "GRK", 0xFF111827, description = "xAI Grok terminal agent", defaultModels = listOf("default"))
    )

    fun findMeta(cmd: String): SupportedAgentMeta {
        return ALL_SUPPORTED_AGENTS.firstOrNull { it.command.equals(cmd, ignoreCase = true) || it.id.equals(cmd, ignoreCase = true) }
            ?: SupportedAgentMeta(cmd, cmd, cmd, cmd.take(2).uppercase(), 0xFF64748B, description = "Custom Agent CLI")
    }
}
