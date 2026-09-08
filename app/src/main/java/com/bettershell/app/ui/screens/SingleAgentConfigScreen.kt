package com.bettershell.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.agent.ThinkingLevel
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.AccentOrange

/**
 * 独立的单 CLI Agent 专属配置页面 (全量压入路由栈，享受预见式返回与右进右出):
 * - 可执行文件路径
 * - 默认推理模型切换
 * - 思考深度参数调节
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SingleAgentConfigScreen(
    agent: DiscoveredAgent,
    onSaveAgent: (DiscoveredAgent) -> Unit,
    onBack: () -> Unit
) {
    val meta = SupportedAgentsCatalog.findMeta(agent.command)
    val isDark = isSystemInDarkTheme()

    var currentAgent by remember(agent.id) { mutableStateOf(agent) }

    fun doSave() {
        onSaveAgent(currentAgent)
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "${meta.displayName} 配置",
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
            // 执行路径
            OpenAiSectionCard(isDark = isDark) {
                OpenAiSettingRow(
                    title = "可执行文件路径",
                    subtitle = currentAgent.path.ifBlank { "由环境变量动态解析" },
                    icon = LucideIcons.Terminal,
                    showDivider = false,
                    isDark = isDark
                )
            }

            // 模型选择
            OpenAiSectionCard(
                headerTitle = "默认推理模型 (Model Routing)",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentAgent.models.forEach { model ->
                            val isSelected = currentAgent.selectedModel == model || (currentAgent.selectedModel.isBlank() && model == "default")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier.clickable {
                                    currentAgent = currentAgent.copy(selectedModel = model)
                                }
                            ) {
                                Text(
                                    text = model,
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 思考深度
            OpenAiSectionCard(
                headerTitle = "思考深度参数 (Thinking Level)",
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThinkingLevel.entries.forEach { level ->
                            val isSelected = currentAgent.thinkingLevel == level
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AccentOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) AccentOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier.clickable {
                                    currentAgent = currentAgent.copy(thinkingLevel = level)
                                }
                            ) {
                                Text(
                                    text = level.displayName,
                                    style = TextStyle(fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
