package com.bettershell.app.terminal

import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class TerminalEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isCommand: Boolean = false,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class TerminalSession(
    val server: ServerConfig,
    private val scope: CoroutineScope
) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _rawOutput = MutableStateFlow("")
    val rawOutput: StateFlow<String> = _rawOutput.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private var jschSession: Session? = null
    private var channel: ChannelShell? = null
    private var outputStream: OutputStream? = null
    private var readerJob: Job? = null

    init {
        connect()
    }

    fun connect() {
        if (_connectionState.value == ConnectionState.Connecting || _connectionState.value == ConnectionState.Connected) {
            return
        }

        _connectionState.value = ConnectionState.Connecting
        appendOutput("\u001B[90m[BetterShell] Connecting to ${server.name} (${server.host}:${server.port})...\u001B[0m\n")

        if (server.isMock || server.authType == AuthType.DEMO_MOCK) {
            connectMock()
        } else {
            connectSsh()
        }
    }

    private fun connectMock() {
        scope.launch {
            delay(600) // Realistic connection delay
            _connectionState.value = ConnectionState.Connected
            appendOutput("\u001B[32m✔ Connected to Agent Environment (mock-agent-1)\u001B[0m\n")
            appendOutput("\u001B[36m╭────────────────────────────────────────────────────────────╮\u001B[0m\n")
            appendOutput("\u001B[36m│\u001B[0m  \u001B[1;37mBetterShell Agent Runtime v1.0\u001B[0m                            \u001B[36m│\u001B[0m\n")
            appendOutput("\u001B[36m│\u001B[0m  Linux 6.8.0-agent-arm64 #1 SMP PREEMPT                     \u001B[36m│\u001B[0m\n")
            appendOutput("\u001B[36m│\u001B[0m  Optimized for Mobile Agent Control                         \u001B[36m│\u001B[0m\n")
            appendOutput("\u001B[36m╰────────────────────────────────────────────────────────────╯\u001B[0m\n\n")

            // Execute startup script if configured
            if (server.startupScript.isNotBlank()) {
                appendOutput("\u001B[33m[Startup Script] Executing configured bash hook...\u001B[0m\n")
                val lines = server.startupScript.lines().filter { it.isNotBlank() && !it.startsWith("#") }
                for (line in lines) {
                    delay(300)
                    appendOutput("\u001B[35m$ \u001B[0m$line\n")
                    delay(200)
                    handleMockCommand(line, isStartup = true)
                }
                appendOutput("\u001B[32m[Startup Script] Hook completed successfully.\u001B[0m\n\n")
            }

            appendOutput("\u001B[1;32magent@better-shell\u001B[0m:\u001B[1;34m~\u001B[0m$ ")
        }
    }

    private fun connectSsh() {
        scope.launch(Dispatchers.IO) {
            try {
                val jsch = JSch()

                if (server.authType == AuthType.PRIVATE_KEY && server.privateKey.isNotBlank()) {
                    val keyBytes = server.privateKey.toByteArray(StandardCharsets.UTF_8)
                    val passBytes = if (server.passphrase.isNotBlank()) server.passphrase.toByteArray(StandardCharsets.UTF_8) else null
                    jsch.addIdentity("custom-key", keyBytes, null, passBytes)
                }

                val session = jsch.getSession(server.username, server.host, server.port)
                if (server.authType == AuthType.PASSWORD && server.password.isNotBlank()) {
                    session.setPassword(server.password)
                }

                val config = java.util.Properties()
                config["StrictHostKeyChecking"] = "no"
                config["PreferredAuthentications"] = if (server.authType == AuthType.PASSWORD) "password,keyboard-interactive" else "publickey,password"
                session.setConfig(config)
                session.timeout = 15000

                session.connect()
                jschSession = session

                val ch = session.openChannel("shell") as ChannelShell
                ch.setPtyType("xterm-256color", 100, 40, 800, 600)
                val inStream = ch.inputStream
                outputStream = ch.outputStream
                ch.connect(5000)
                channel = ch

                _connectionState.value = ConnectionState.Connected
                appendOutput("\u001B[32m✔ Connected to ${server.username}@${server.host}:${server.port}\u001B[0m\n\n")

                // Start reading SSH stream
                startReader(inStream)

                // Execute startup script if provided
                if (server.startupScript.isNotBlank()) {
                    delay(500)
                    val lines = server.startupScript.lines().filter { it.isNotBlank() && !it.startsWith("#") }
                    for (line in lines) {
                        sendCommand(line)
                        delay(250)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _connectionState.value = ConnectionState.Error(e.localizedMessage ?: "Connection failed")
                appendOutput("\n\u001B[31m✖ Connection failed: ${e.localizedMessage}\u001B[0m\n")
            }
        }
    }

    private fun startReader(inputStream: InputStream) {
        readerJob?.cancel()
        readerJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(4096)
            try {
                while (isActive) {
                    val read = inputStream.read(buffer)
                    if (read == -1) break
                    val text = String(buffer, 0, read, StandardCharsets.UTF_8)
                    appendOutput(text)
                }
            } catch (e: Exception) {
                if (isActive) {
                    appendOutput("\n\u001B[31m[Connection lost: ${e.message}]\u001B[0m\n")
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }

    fun sendCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isNotBlank()) {
            _history.value = (_history.value + trimmed).takeLast(50)
        }

        if (server.isMock || server.authType == AuthType.DEMO_MOCK) {
            scope.launch {
                appendOutput("$trimmed\n")
                delay(100)
                handleMockCommand(trimmed, isStartup = false)
                appendOutput("\u001B[1;32magent@better-shell\u001B[0m:\u001B[1;34m~\u001B[0m$ ")
            }
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                outputStream?.let { stream ->
                    stream.write("$command\n".toByteArray(StandardCharsets.UTF_8))
                    stream.flush()
                }
            } catch (e: Exception) {
                appendOutput("\n\u001B[31m[Send failed: ${e.message}]\u001B[0m\n")
            }
        }
    }

    fun sendRaw(bytes: ByteArray) {
        if (server.isMock || server.authType == AuthType.DEMO_MOCK) {
            // Handle Ctrl+C in mock
            if (bytes.contentEquals(byteArrayOf(3))) {
                appendOutput("^C\n\u001B[1;32magent@better-shell\u001B[0m:\u001B[1;34m~\u001B[0m$ ")
            }
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                outputStream?.let { stream ->
                    stream.write(bytes)
                    stream.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearScreen() {
        _rawOutput.value = "\u001B[1;32magent@better-shell\u001B[0m:\u001B[1;34m~\u001B[0m$ "
    }

    private fun appendOutput(text: String) {
        // Keep output buffer bounded to avoid memory exhaustion on long sessions
        val current = _rawOutput.value
        val updated = current + text
        _rawOutput.value = if (updated.length > 100_000) {
            updated.takeLast(80_000)
        } else {
            updated
        }
    }

    private fun handleMockCommand(cmd: String, isStartup: Boolean) {
        val parts = cmd.split(" ").filter { it.isNotBlank() }
        val name = parts.firstOrNull()?.lowercase() ?: ""

        when (name) {
            "help" -> {
                appendOutput("\u001B[1mAvailable commands:\u001B[0m\n")
                appendOutput("  \u001B[36magent\u001B[0m [--status | --task <desc>] : Manage Agent tasks\n")
                appendOutput("  \u001B[36mls\u001B[0m, \u001B[36mpwd\u001B[0m, \u001B[36mcat\u001B[0m, \u001B[36mecho\u001B[0m           : Basic file utilities\n")
                appendOutput("  \u001B[36mtop\u001B[0m, \u001B[36mps\u001B[0m, \u001B[36muname\u001B[0m                   : System inspection\n")
                appendOutput("  \u001B[36mclear\u001B[0m                              : Clear terminal screen\n")
            }
            "ls" -> {
                appendOutput("\u001B[1;34magent-workspace/\u001B[0m  \u001B[1;34mvenv/\u001B[0m  \u001B[32mrun.sh*\u001B[0m  agent.py  README.md  models.json\n")
            }
            "pwd" -> {
                appendOutput("/home/agent\n")
            }
            "uname", "uname -a" -> {
                appendOutput("Linux better-shell-agent 6.8.0-arm64 #1 SMP aarch64 GNU/Linux\n")
            }
            "clear" -> {
                _rawOutput.value = ""
            }
            "agent", "agent --status" -> {
                appendOutput("\u001B[1;32m● Agent Status:\u001B[0m IDLE (Awaiting prompt)\n")
                appendOutput("  \u001B[90mModel:\u001B[0m Claude-3.7-Sonnet / GPT-4o\n")
                appendOutput("  \u001B[90mMemory:\u001B[0m 2.4 GB / 16.0 GB (15%)\n")
                appendOutput("  \u001B[90mActive Tools:\u001B[0m bash, edit, read, web_search, python\n")
            }
            "cat" -> {
                val file = parts.getOrNull(1)
                if (file == "README.md") {
                    appendOutput("# BetterShell Agent Node\nDesigned for frictionless mobile control of CLI tools and agent instances.\n")
                } else if (file != null) {
                    appendOutput("Contents of $file [Sample file data]\n")
                } else {
                    appendOutput("cat: missing file operand\n")
                }
            }
            "echo" -> {
                val text = parts.drop(1).joinToString(" ").removeSurrounding("\"").removeSurrounding("'")
                appendOutput("$text\n")
            }
            "source" -> {
                appendOutput("\u001B[90m[OK] Environment loaded.\u001B[0m\n")
            }
            "python", "python3" -> {
                appendOutput("Python 3.12.3 (main, Jul 21 2026)\nType \"help()\" or run scripts.\n")
            }
            "git", "git status" -> {
                appendOutput("On branch main\nYour branch is up to date with 'origin/main'.\nnothing to commit, working tree clean\n")
            }
            else -> {
                if (cmd.isNotBlank()) {
                    appendOutput("\u001B[32m[agent-exec]\u001B[0m Running: $cmd\n")
                    appendOutput("Execution finished (exit code 0)\n")
                }
            }
        }
    }

    fun disconnect() {
        readerJob?.cancel()
        readerJob = null
        try {
            channel?.disconnect()
            jschSession?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        channel = null
        jschSession = null
        outputStream = null
        _connectionState.value = ConnectionState.Disconnected
        appendOutput("\n\u001B[90m[Disconnected from ${server.name}]\u001B[0m\n")
    }
}
