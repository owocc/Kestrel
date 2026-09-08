package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 独立的服务器启动脚本配置页面 (完全复用全局通用的 OpenAiSectionCard)
 */
@Composable
fun ServerStartupScriptScreen(
    server: ServerConfig,
    onSaveServer: (ServerConfig) -> Unit,
    onBack: () -> Unit
) {
    var startupScript by remember { mutableStateOf(server.startupScript) }

    fun doSave() {
        onSaveServer(server.copy(startupScript = startupScript.trim()))
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "启动脚本设置",
            onBack = onBack,
            showSave = true,
            onSave = { doSave() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OpenAiSectionCard(
                headerTitle = "自动执行 Shell 脚本"
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = startupScript,
                        onValueChange = { startupScript = it },
                        label = { Text("连接建立后执行的 Shell 命令") },
                        placeholder = { Text("例如：tmux attach || tmux new -s work") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
