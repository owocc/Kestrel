package com.bettershell.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.agent.DiscoveredAgent
import com.bettershell.app.ui.theme.AccentGreen

/**
 * 完整对标用户截图 1 & 截图 2 的 ChatGPT 融合胶囊风格输入框
 * - 外层为平滑的大圆角卡片容器 (RoundedCornerShape(24.dp))
 * - 内部上方：支持自适应增高的多行文本输入区域
 * - 内部下方：操作栏
 *   - 左侧：'+' 按钮 (点击展开 Agent 切换、模型切换与思考配置)
 *   - 居中偏左：当前生效的 Agent 标签与模型微徽标
 *   - 右侧：内置集成式圆形发送按钮 (空文本时为静止状态，有文本时为绿色升起高亮状态；如果 Agent 正在运行则显示停止方块)
 */
@Composable
fun ChatIntegratedInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    activeAgent: DiscoveredAgent,
    isAgentBusy: Boolean,
    isDark: Boolean,
    onOpenAgentPicker: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasText = text.isNotBlank()

    // 容器背景与边界色彩
    val containerBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val containerBorder = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerBg,
        border = BorderStroke(1.dp, containerBorder),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // 1. 上半部分：多行文本输入区域 (自动撑高，限制最高 140dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 28.dp, max = 130.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "询问 ${activeAgent.type.displayName}...",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(if (isDark) Color.White else Color(0xFF10A37F)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            // 2. 下半部分：操作工具条 (左侧 '+' 选 Agent，右侧融合圆形向上发送箭头)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧区：'+' 按钮与 Agent / 模型小胶囊
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5E7EB))
                            .clickable(onClick = onOpenAgentPicker),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "选择 Agent 与模型",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 当前 Agent & 模型徽章 (点击也能快速打开选择器)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = (if (isDark) Color(0xFF2B2B2B) else Color(0xFFE5E7EB)).copy(alpha = 0.7f),
                        modifier = Modifier.clickable(onClick = onOpenAgentPicker)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = activeAgent.type.displayName,
                                style = TextStyle(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (activeAgent.selectedModel.isNotBlank() && activeAgent.selectedModel != "default") {
                                Text(
                                    text = "· ${activeAgent.selectedModel}",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // 右侧融合式圆形操作按钮 (对齐截图 1 的绿色圆形向上箭头)
                val sendBtnBg by animateColorAsState(
                    targetValue = when {
                        isAgentBusy -> Color(0xFFEF4444) // 忙碌时红色 Stop
                        hasText -> Color(0xFF10A37F) // 有文本时标志性的 ChatGPT 墨绿高亮
                        else -> if (isDark) Color(0xFF333333) else Color(0xFFD1D5DB)
                    },
                    label = "sendBtnBg"
                )

                val sendBtnTint by animateColorAsState(
                    targetValue = when {
                        hasText || isAgentBusy -> Color.White
                        else -> if (isDark) Color(0xFF737373) else Color(0xFF9CA3AF)
                    },
                    label = "sendBtnTint"
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(sendBtnBg)
                        .clickable(enabled = hasText || isAgentBusy, onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAgentBusy) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Send",
                            tint = sendBtnTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
