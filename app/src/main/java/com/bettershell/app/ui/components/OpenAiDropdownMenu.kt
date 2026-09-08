package com.bettershell.app.ui.components

import android.os.Build
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bettershell.app.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

data class OpenAiMenuItemData(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * Android 原生 Flip 弹出动画 & 真实系统窗口级 OnBackAnimationCallback 预见式手势收起菜单
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

    // 捕获宿主窗口的 Dispatcher Owner，透传到 Popup 内部
    val parentDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    // 预见式手势实时进度
    var gestureFraction by remember { mutableFloatStateOf(0f) }
    var isGestureTracking by remember { mutableStateOf(false) }

    fun dismissWithAnimation() {
        scope.launch {
            animScale.animateTo(
                targetValue = 0.75f,
                animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing)
            )
        }
        scope.launch {
            animAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 130)
            )
            isVisible = false
            onDismissRequest()
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            isVisible = true
            gestureFraction = 0f
            isGestureTracking = false
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
                    animationSpec = tween(durationMillis = 150)
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
            dismissOnBackPress = false, // 由底层的系统级回调精准接管并执行动画
            dismissOnClickOutside = true
        )
    ) {
        val popupView = LocalView.current

        // 核心技术突破：在 Popup 真实的 Android Window 级别直接注册 OnBackAnimationCallback！
        // 从而直接由系统 Framework 将侧滑手势逐帧派发到这里，绝对支持原生预测性返回！
        DisposableEffect(popupView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val dispatcher = popupView.findOnBackInvokedDispatcher()
                val callback = object : OnBackAnimationCallback {
                    override fun onBackStarted(backEvent: BackEvent) {
                        isGestureTracking = true
                        gestureFraction = FastOutSlowInEasing.transform(backEvent.progress)
                    }

                    override fun onBackProgressed(backEvent: BackEvent) {
                        gestureFraction = FastOutSlowInEasing.transform(backEvent.progress)
                    }

                    override fun onBackInvoked() {
                        // 手势确认提交：缩回并关闭
                        isGestureTracking = false
                        dismissWithAnimation()
                    }

                    override fun onBackCancelled() {
                        // 手势放弃：复位展开
                        scope.launch {
                            val anim = Animatable(gestureFraction)
                            anim.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) {
                                gestureFraction = value
                            }
                            isGestureTracking = false
                        }
                    }
                }

                dispatcher?.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_OVERLAY,
                    callback
                )

                onDispose {
                    dispatcher?.unregisterOnBackInvokedCallback(callback)
                }
            } else {
                onDispose {}
            }
        }

        // 针对低于 Android 14 或 fallback 的兼容保证
        if (parentDispatcherOwner != null) {
            CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides parentDispatcherOwner) {
                androidx.activity.compose.BackHandler(enabled = isVisible && !isGestureTracking) {
                    dismissWithAnimation()
                }
            }
        }

        val effectiveScale = if (isGestureTracking) {
            (1f - gestureFraction * 0.40f).coerceIn(0.60f, 1f)
        } else {
            animScale.value
        }

        val effectiveAlpha = if (isGestureTracking) {
            (1f - gestureFraction * 0.85f).coerceIn(0f, 1f)
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
