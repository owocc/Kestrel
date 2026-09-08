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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bettershell.app.agent.DiscoveredAgent
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

    val modelsList = if (agent.models.isNotEmpty()) agent.models else listOf("default")
    val count = modelsList.size

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

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                modelsList.forEachIndexed { index, modelName ->
                    val isSelected = modelName == selectedModel
                    val position = when {
                        count == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == count - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }

                    val displayName = if (modelName == "default") "默认模型 (Default)" else modelName
                    val subtitle = when {
                        modelName == "default" -> "遵循 CLI 本地环境配置的默认 LLM 模型"
                        modelName.contains("gemini", ignoreCase = true) -> "Google 高效多模态推理模型"
                        modelName.contains("claude", ignoreCase = true) -> "Anthropic 卓越代码与逻辑推理模型"
                        modelName.contains("gpt", ignoreCase = true) -> "OpenAI 旗舰通用大模型"
                        modelName.contains("deepseek", ignoreCase = true) -> "DeepSeek 高效编码与深度推理模型"
                        else -> "专用模型配置"
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
            }
        }
    }
}
