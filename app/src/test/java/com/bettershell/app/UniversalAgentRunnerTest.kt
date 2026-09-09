package com.bettershell.app

import com.bettershell.app.agent.AgentType
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.agent.UniversalAgentRunner
import com.bettershell.app.terminal.ChatMessage
import com.bettershell.app.terminal.ChatSender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalAgentRunnerTest {

    @Test
    fun testFirstTurnCommandDoesNotPassInvalidSessionIdAndContainsCwd() {
        var executedCommand = ""
        val agent = DiscoveredAgent(
            id = "omp",
            type = AgentType.OMP,
            command = "omp",
            path = "/usr/local/bin/omp",
            selectedModel = "claude-sonnet",
            thinkingLevel = ThinkingLevel.HIGH
        )

        val runner = UniversalAgentRunner(
            sessionId = "test-session-123",
            cwd = "/home/user/project",
            cliResumeId = null,
            initialAgent = agent,
            sendRawCommand = { cmd -> executedCommand = cmd }
        )

        runner.sendPrompt("Hello World")

        // Must change directory to cwd
        assertTrue("Command should cd into cwd", executedCommand.startsWith("cd \"/home/user/project\" && "))
        // Must NOT pass --resume on first turn when no session exists yet
        assertTrue("Command should not pass --resume on first turn", !executedCommand.contains("--resume"))
        // Must include model and thinking level
        assertTrue("Command should pass --model", executedCommand.contains("--model \"claude-sonnet\""))
        assertTrue("Command should pass --thinking", executedCommand.contains("--thinking \"high\""))
        assertTrue("Command should pass prompt", executedCommand.contains("Hello World"))
    }

    @Test
    fun testResumePassedWhenExplicitCliResumeIdSet() {
        var executedCommand = ""
        val agent = DiscoveredAgent(
            id = "omp",
            type = AgentType.OMP,
            command = "omp",
            path = "/usr/local/bin/omp"
        )

        val runner = UniversalAgentRunner(
            sessionId = "test-session-123",
            cwd = "/home/user/project",
            cliResumeId = "omp-imported-sess-999",
            initialAgent = agent,
            sendRawCommand = { cmd -> executedCommand = cmd }
        )

        runner.sendPrompt("Continue Turn")

        assertTrue("Command should pass --resume with explicit imported session ID", executedCommand.contains("--resume \"omp-imported-sess-999\""))
    }
    @Test
    fun testSecondTurnIncludesConversationContext() {
        var executedCommand = ""
        val agent = DiscoveredAgent(
            id = "omp",
            type = AgentType.OMP,
            command = "omp",
            path = "/usr/local/bin/omp"
        )

        val runner = UniversalAgentRunner(
            sessionId = "test-session-456",
            cwd = "~",
            cliResumeId = null,
            initialAgent = agent,
            sendRawCommand = { cmd -> executedCommand = cmd }
        )

        // Load existing conversation history: Turn 1
        runner.loadHistory(
            listOf(
                ChatMessage(id = "1", sender = ChatSender.USER, content = "我的项目是用 Kotlin 写的吗？"),
                ChatMessage(id = "2", sender = ChatSender.AGENT, content = "是的，这是一个 Kotlin Android 项目。")
            )
        )

        // Turn 2: User asks follow-up
        runner.sendPrompt("那帮我加一个按钮")

        // The command sent to agent must contain the prior context so the agent knows what was discussed!
        assertTrue("Command must contain conversation context marker", executedCommand.contains("[Context of prior discussion in this session]:"))
        assertTrue("Command must contain prior user query", executedCommand.contains("我的项目是用 Kotlin 写的吗？"))
        assertTrue("Command must contain prior assistant response", executedCommand.contains("这是一个 Kotlin Android 项目"))
        assertTrue("Command must contain current user request", executedCommand.contains("那帮我加一个按钮"))
    }

    @Test
    fun testClaudeResumeIdPassed() {
        var executedCommand = ""
        val agent = DiscoveredAgent(
            id = "claude",
            type = AgentType.CLAUDE,
            command = "claude",
            path = "/usr/local/bin/claude"
        )

        val runner = UniversalAgentRunner(
            sessionId = "bs-claude-01",
            cwd = "/var/www/site",
            cliResumeId = "claude-session-xyz",
            initialAgent = agent,
            sendRawCommand = { cmd -> executedCommand = cmd }
        )

        runner.sendPrompt("继续未完成的重构")

        assertTrue("Command should cd into cwd", executedCommand.contains("cd \"/var/www/site\" && "))
        assertTrue("Command should pass --resume for Claude Code", executedCommand.contains("claude -p --resume \"claude-session-xyz\""))
    }

    @Test
    fun testPromptWithAttachedImages() {
        var executedCommand = ""
        val agent = DiscoveredAgent(
            id = "omp",
            type = AgentType.OMP,
            command = "omp",
            path = "/usr/local/bin/omp"
        )

        val runner = UniversalAgentRunner(
            sessionId = "test-img",
            cwd = "~",
            initialAgent = agent,
            sendRawCommand = { cmd -> executedCommand = cmd }
        )

        runner.sendPrompt("帮我分析这张 UI 架构图", listOf("/tmp/uploads/diagram.png"))

        assertTrue("Command should reference attached image", executedCommand.contains("[Attached Images: /tmp/uploads/diagram.png]"))
    }
}
