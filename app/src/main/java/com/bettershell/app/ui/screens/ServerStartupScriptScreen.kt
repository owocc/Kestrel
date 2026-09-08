package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.StandardPageHeader

/**
 * 独立的服务器启动脚本配置页面 (单独压入栈，带保存圆钮，支持预见式返回)
 */
@Composable
fun ServerStartupScriptScreen(
    server: ServerConfig,
    onSaveServer: (ServerConfig) -> Unit,
    onBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var startupScript by remember { mutableStateOf(server.startupScript) }

    fun doSave() {
        onSaveServer(server.copy(startupScript = startupScript))
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
            isDark = isDark,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OpenAiSectionCard(
                headerTitle = "自动执行 Shell 脚本",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "每次进入 SSH 终端会话后，会自动逐行注入执行这些 Shell 指令：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = startupScript,
                        onValueChange = { startupScript = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        shape = RoundedCornerShape(14.dp),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
