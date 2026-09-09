package com.bettershell.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.agent.SupportedAgentsCatalog
import com.bettershell.app.ui.theme.isAppInDarkTheme

/**
 * 规范化模型切换底栏弹窗 (SelectModelBottomSheet)
 * - 严格遵循工程 2/3 高度规范与顶部渐隐描边包裹小手柄
 * - 纯黑/纯白背景 (MaterialTheme.colorScheme.background)
 * - 居中标题与独立单选色块卡片 (CardPosition)
 * - 右侧内嵌 ChatGPT 同心单选标记 (Radio Circle)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectModelBottomSheet(
    agent: DiscoveredAgent,
    selectedModel: String,
    onSelectModel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme
    val topBorderColor = if (isDark) Color(0xFF383838) else Color(0xFFD1D5DB)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val catalogModels: List<String> = SupportedAgentsCatalog.findMeta(agent.command).defaultModels
    val combinedModels: List<String> = remember(agent.models, catalogModels) {
        (listOf("default") + agent.models + catalogModels).distinct().filter { it.isNotBlank() }
    }

    val filteredModels: List<String> = remember(combinedModels, searchQuery) {
        if (searchQuery.isBlank()) {
            combinedModels
        } else {
            combinedModels.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f)
                .bottomSheetTopBorder(strokeWidth = 1.dp, color = topBorderColor, cornerRadius = 28.dp)
                .padding(horizontal = 20.dp)
        ) {
            // 内置顶层小横条 (Drag Handle)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
                }
            }

            // 规范化纯净居中标题
            Text(
                text = "切换模型 (${agent.type.displayName})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            // 搜索过滤与自定义输入栏
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索模型或输入自定义模型 ID...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                leadingIcon = {
                    Icon(
                        androidx.compose.material.icons.Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                androidx.compose.material.icons.Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                val showCustomCard = searchQuery.isNotBlank() && !combinedModels.any { it.equals(searchQuery.trim(), ignoreCase = true) }
                val listToRender = filteredModels
                val count = listToRender.size + (if (showCustomCard) 1 else 0)

                listToRender.forEachIndexed { index, modelName ->
                    val isSelected = modelName == selectedModel
                    val position = when {
                        count == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == count - 1 && !showCustomCard -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }

                    val displayName = if (modelName == "default") "默认模型 (Default)" else modelName
                    val subtitle = when {
                        modelName == "default" -> "遵循 CLI 本地环境配置的默认 LLM 模型"
                        modelName.contains("gemini", ignoreCase = true) -> "Google 高效多模态推理模型"
                        modelName.contains("claude", ignoreCase = true) -> "Anthropic 卓越代码与深度逻辑推理模型"
                        modelName.contains("gpt-5", ignoreCase = true) -> "OpenAI 次世代智能体核心模型"
                        modelName.contains("gpt", ignoreCase = true) -> "OpenAI 旗舰通用推理大模型"
                        modelName.contains("deepseek", ignoreCase = true) -> "DeepSeek 开源高效编码与推理模型"
                        modelName.contains("kimi", ignoreCase = true) -> "Moonshot 长上下文长思考模型"
                        modelName.contains("qwen", ignoreCase = true) -> "通义千问高效代码模型"
                        modelName.contains("grok", ignoreCase = true) -> "xAI Grok 极速实时推理模型"
                        else -> "专用模型: $modelName"
                    }

                    OpenAiRadioOptionCard(
                        title = displayName,
                        subtitle = subtitle,
                        selected = isSelected,
                        position = position,
                        isDark = isDark,
                        onClick = {
                            onSelectModel(modelName)
                            onDismiss()
                        }
                    )
                }

                if (showCustomCard) {
                    val customModel = searchQuery.trim()
                    OpenAiRadioOptionCard(
                        title = "使用自定义模型: $customModel",
                        subtitle = "直接传递至 CLI 的 --model 参数执行",
                        selected = selectedModel == customModel,
                        position = if (listToRender.isEmpty()) CardPosition.SINGLE else CardPosition.BOTTOM,
                        isDark = isDark,
                        onClick = {
                            onSelectModel(customModel)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
