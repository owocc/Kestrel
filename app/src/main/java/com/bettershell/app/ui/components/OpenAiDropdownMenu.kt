package com.bettershell.app.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bettershell.app.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class OpenAiMenuItemData(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * Android 原生 Flip 弹出动画 & 预测性手势收回的高级浮层菜单 (Flip Animated Predictive Dropdown Menu)
 *
 * 核心动效规范：
 * 1. 【出入同源 Flip】：从右上角锚点（transformOrigin = 1f, 0f）弹性伸展展开，收起时原路折叠缩回；
 * 2. 【支持预见式手势返回】：展开状态下，侧滑返回手指滑动时，菜单随进度平滑缩减 scale 和 alpha，松手提交优雅缩回关闭；
 * 3. 【无上下内边距】：容器零边距，首尾条目贴合 20dp 大圆角，高度 19dp 纵深，扩散 20dp 柔和晕染阴影。
 */
@Composable
fun OpenAiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<OpenAiMenuItemData>,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    width: Dp = 210.dp
) {
    var isVisible by remember { mutableStateOf(false) }
    val animScale = remember { Animatable(0.75f) }
    val animAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val gestureProgress = remember { Animatable(0f) }
    var isBackGestureActive by remember { mutableStateOf(false) }

    fun dismissWithAnimation() {
        scope.launch {
            animScale.animateTo(
                targetValue = 0.75f,
                animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
            )
        }
        scope.launch {
            animAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 140)
            )
            isVisible = false
            onDismissRequest()
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            isVisible = true
            animScale.snapTo(0.75f)
            animAlpha.snapTo(0f)
            scope.launch {
                animScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            scope.launch {
                animAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 160)
                )
            }
        } else if (isVisible) {
            dismissWithAnimation()
        }
    }

    if (!isVisible) return

    val menuBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 0, y = 14),
        onDismissRequest = { dismissWithAnimation() },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = false, // 由内层专门的 PredictiveBackHandler 接管
            dismissOnClickOutside = true
        )
    ) {
        // 核心：独占拦截当前浮层的 Android 16 预见式返回手势！
        PredictiveBackHandler(enabled = isVisible) { progressFlow: Flow<BackEventCompat> ->
            isBackGestureActive = true
            try {
                progressFlow.collect { event ->
                    val eased = FastOutSlowInEasing.transform(event.progress)
                    gestureProgress.snapTo(eased)
                }
                // 手势确认退出 (Commit)：顺势完全缩回原点并消失
                gestureProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
                )
                gestureProgress.snapTo(0f)
                isBackGestureActive = false
                isVisible = false
                onDismissRequest()
            } catch (e: CancellationException) {
                // 手势取消 (Cancel)：弹簧复位展开
                scope.launch {
                    gestureProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    isBackGestureActive = false
                }
            }
        }

        val effectiveScale = if (isBackGestureActive) {
            (1f - (gestureProgress.value * 0.35f)).coerceIn(0.65f, 1f)
        } else {
            animScale.value
        }

        val effectiveAlpha = if (isBackGestureActive) {
            (1f - (gestureProgress.value * 0.85f)).coerceIn(0f, 1f)
        } else {
            animAlpha.value
        }

        Surface(
            modifier = modifier
                .width(width)
                .graphicsLayer {
                    // Android 原生 Flip 核心：将折叠缩放锚点固定在右上角 (1f, 0f)
                    // “从哪里出来就从哪里缩放回去”！
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0f)
                    scaleX = effectiveScale
                    scaleY = effectiveScale
                    alpha = effectiveAlpha
                }
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.40f else 0.12f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(menuBg)
                .border(1.dp, borderColor.copy(alpha = if (isDark) 0.5f else 0.7f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = menuBg,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                items.forEach { item ->
                    OpenAiDropdownMenuItemRow(
                        title = item.title,
                        icon = item.icon,
                        iconTint = item.iconTint,
                        isDestructive = item.isDestructive,
                        isDark = isDark,
                        onClick = {
                            dismissWithAnimation()
                            item.onClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OpenAiDropdownMenuItemRow(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    isDestructive: Boolean = false,
    isDark: Boolean = isAppInDarkTheme,
    onClick: () -> Unit
) {
    val defaultIconColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF262626)
    val actualIconTint = when {
        isDestructive -> MaterialTheme.colorScheme.error
        iconTint != null -> iconTint
        else -> defaultIconColor
    }
    val textColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        else -> if (isDark) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 19.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = actualIconTint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
