package com.bettershell.app

import com.bettershell.app.agent.SupportedAgentsCatalog
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedAgentsCatalogTest {

    @Test
    fun testAllMajorAgentsExist() {
        val agents = SupportedAgentsCatalog.ALL_SUPPORTED_AGENTS
        assertTrue("Should have at least 10 major agents", agents.size >= 10)

        val commands = agents.map { it.command }
        assertTrue("Contains omp", commands.contains("omp"))
        assertTrue("Contains claude", commands.contains("claude"))
        assertTrue("Contains codex", commands.contains("codex"))
        assertTrue("Contains opencode", commands.contains("opencode"))
        assertTrue("Contains kimi", commands.contains("kimi"))
        assertTrue("Contains copilot", commands.contains("copilot"))
    }

    @Test
    fun testRichModelCatalogs() {
        val ompMeta = SupportedAgentsCatalog.findMeta("omp")
        assertTrue("OMP has multiple models", ompMeta.defaultModels.size >= 5)
        assertTrue("OMP contains claude-3-7-sonnet", ompMeta.defaultModels.any { it.contains("claude-3-7-sonnet") })
        assertTrue("OMP contains deepseek", ompMeta.defaultModels.any { it.contains("deepseek") })

        val claudeMeta = SupportedAgentsCatalog.findMeta("claude")
        assertTrue("Claude has sonnet models", claudeMeta.defaultModels.any { it.contains("sonnet") })

        val codexMeta = SupportedAgentsCatalog.findMeta("codex")
        assertTrue("Codex has gpt-5/sol models", codexMeta.defaultModels.any { it.contains("sol") || it.contains("gpt") })
    }

    @Test
    fun testFindMetaFallback() {
        val unknown = SupportedAgentsCatalog.findMeta("unknown-custom-cli")
        assertNotNull("Should fallback gracefully", unknown)
        assertTrue("Fallback command matches", unknown.command == "unknown-custom-cli")
    }
}
