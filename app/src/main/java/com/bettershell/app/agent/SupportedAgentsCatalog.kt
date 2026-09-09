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
        SupportedAgentMeta(
            id = "omp",
            command = "omp",
            displayName = "Oh My Pi",
            shortName = "OMP",
            avatarBgColorHex = 0xFF6366F1,
            description = "IDE wired terminal coding agent",
            defaultModels = listOf(
                "default",
                "anthropic/claude-3-7-sonnet",
                "anthropic/claude-3-5-sonnet",
                "anthropic/claude-3-5-haiku",
                "openai/gpt-4o",
                "openai/o3-mini",
                "openai/o1",
                "deepseek/deepseek-chat",
                "deepseek/deepseek-reasoner",
                "google/gemini-2.0-flash",
                "google/gemini-2.0-pro",
                "moonshot/kimi-k1.5",
                "qwen/qwen-2.5-coder-32b"
            )
        ),
        SupportedAgentMeta(
            id = "claude",
            command = "claude",
            displayName = "Claude Code",
            shortName = "CC",
            avatarBgColorHex = 0xFFD97706,
            description = "Anthropic Claude coding agent",
            defaultModels = listOf(
                "default",
                "claude-sonnet-4-6",
                "claude-sonnet-4-5",
                "claude-opus-4-7",
                "claude-opus-4-6",
                "claude-haiku-4-5",
                "claude-3-7-sonnet",
                "claude-3-5-sonnet"
            )
        ),
        SupportedAgentMeta(
            id = "codex",
            command = "codex",
            displayName = "Codex",
            shortName = "CDX",
            avatarBgColorHex = 0xFF10B981,
            description = "OpenAI Codex agent environment",
            defaultModels = listOf(
                "default",
                "gpt-5.6-sol",
                "gpt-5.6-terra",
                "gpt-5.6-luna",
                "gpt-5.5",
                "gpt-5.4",
                "gpt-5.4-mini",
                "gpt-5.3-codex",
                "gpt-5.2",
                "gpt-4o",
                "o3-mini",
                "o1"
            )
        ),
        SupportedAgentMeta(
            id = "opencode",
            command = "opencode",
            displayName = "OpenCode",
            shortName = "OC",
            avatarBgColorHex = 0xFF3B82F6,
            description = "OpenCode CLI coding agent",
            defaultModels = listOf(
                "default",
                "openai/gpt-4o",
                "openai/o3-mini",
                "anthropic/claude-3-5-sonnet",
                "anthropic/claude-3-7-sonnet",
                "deepseek/deepseek-chat",
                "deepseek/deepseek-reasoner",
                "google/gemini-2.0-flash",
                "qwen/qwen-2.5-coder-32b"
            )
        ),
        SupportedAgentMeta(
            id = "pi",
            command = "pi",
            displayName = "Pi Agent",
            shortName = "PI",
            avatarBgColorHex = 0xFF8B5CF6,
            description = "Minimal coding agent harness",
            defaultModels = listOf(
                "default",
                "gemini-2.0-flash",
                "claude-3-5-sonnet",
                "deepseek-chat",
                "gpt-4o"
            )
        ),
        SupportedAgentMeta(
            id = "cursor",
            command = "cursor-agent",
            displayName = "Cursor",
            shortName = "CUR",
            avatarBgColorHex = 0xFF06B6D4,
            description = "Cursor background agent CLI",
            defaultModels = listOf(
                "default",
                "claude-3.7-sonnet",
                "claude-3.5-sonnet",
                "gpt-4o",
                "o3-mini",
                "o1-mini",
                "cursor-fast"
            )
        ),
        SupportedAgentMeta(
            id = "kimi",
            command = "kimi",
            displayName = "Kimi CLI",
            shortName = "KIM",
            avatarBgColorHex = 0xFFEC4899,
            description = "Moonshot Kimi coding agent",
            defaultModels = listOf(
                "default",
                "kimi-k1.5",
                "kimi-k2",
                "moonshot-v1-8k",
                "moonshot-v1-32k",
                "moonshot-v1-128k"
            )
        ),
        SupportedAgentMeta(
            id = "copilot",
            command = "copilot",
            displayName = "Copilot",
            shortName = "COP",
            avatarBgColorHex = 0xFF4B5563,
            description = "GitHub Copilot agent CLI",
            defaultModels = listOf(
                "default",
                "gpt-5.5",
                "gpt-5.4",
                "gpt-5.4-mini",
                "claude-opus-4.7",
                "claude-sonnet-4.6",
                "claude-haiku-4.5",
                "gpt-4o"
            )
        ),
        SupportedAgentMeta(
            id = "codearts",
            command = "codearts",
            displayName = "CodeArts",
            shortName = "CA",
            avatarBgColorHex = 0xFFE11D48,
            description = "Huawei CodeArts CLI agent",
            defaultModels = listOf("default", "pangu-coder-v2", "pangu-coder-v1")
        ),
        SupportedAgentMeta(
            id = "deveco",
            command = "deveco",
            displayName = "DevEco",
            shortName = "DEV",
            avatarBgColorHex = 0xFF2563EB,
            description = "DevEco studio terminal agent",
            defaultModels = listOf("default", "deveco-assistant-v2", "code-companion")
        ),
        SupportedAgentMeta(
            id = "openclaw",
            command = "openclaw",
            displayName = "OpenClaw",
            shortName = "OCL",
            avatarBgColorHex = 0xFFF59E0B,
            description = "OpenClaw autonomous agent",
            defaultModels = listOf("default", "openclaw-agent-v1", "deepseek-coder", "qwen-coder")
        ),
        SupportedAgentMeta(
            id = "hermes",
            command = "hermes",
            displayName = "Hermes",
            shortName = "HER",
            avatarBgColorHex = 0xFF14B8A6,
            description = "Hermes coding agent",
            defaultModels = listOf(
                "default",
                "nous:anthropic/claude-opus-4.7",
                "nous:moonshotai/kimi-k2.5",
                "nous:deepseek/deepseek-v3",
                "openai-codex:gpt-5.6-terra"
            )
        ),
        SupportedAgentMeta(
            id = "qoder",
            command = "qodercli",
            displayName = "Qoder",
            shortName = "QOD",
            avatarBgColorHex = 0xFF84CC16,
            description = "Qoder terminal agent",
            defaultModels = listOf("default", "qoder-auto", "claude-3.5-sonnet", "gpt-4o", "deepseek-v3")
        ),
        SupportedAgentMeta(
            id = "trae",
            command = "traecli",
            displayName = "Trae",
            shortName = "TR",
            avatarBgColorHex = 0xFF6D28D9,
            description = "ByteDance Trae CLI agent",
            defaultModels = listOf("default", "claude-3-5-sonnet", "gpt-4o", "doubao-pro-32k", "deepseek-v3")
        ),
        SupportedAgentMeta(
            id = "grok",
            command = "grok",
            displayName = "Grok CLI",
            shortName = "GRK",
            avatarBgColorHex = 0xFF111827,
            description = "xAI Grok terminal agent",
            defaultModels = listOf("default", "grok-2", "grok-2-mini", "grok-beta", "grok-vision-beta")
        )
    )

    fun findMeta(cmd: String): SupportedAgentMeta {
        return ALL_SUPPORTED_AGENTS.firstOrNull { it.command.equals(cmd, ignoreCase = true) || it.id.equals(cmd, ignoreCase = true) }
            ?: SupportedAgentMeta(cmd, cmd, cmd, cmd.take(2).uppercase(), 0xFF64748B, description = "Custom Agent CLI")
    }
}
