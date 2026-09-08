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
    val iconTint: Color = Color.Unspecified,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * Android 原生 Flip 弹出动画 & 真实系统窗口级 OnBackAnimationCallback 预见式手势收起菜单
 * 解决触摸未选导致的锁定 Bug：无论何时只要 expanded 变为 false，必然重置所有手势锁并执行关闭！
 */
@Composable
fun OpenAiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<OpenAiMenuItemData>,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    width: Dp = 210.dp,
    alignment: Alignment = Alignment.TopEnd,
    transformOrigin: TransformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0f)
) {
    var isVisible by remember { mutableStateOf(false) }
    val animScale = remember { Animatable(0.75f) }
    val animAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val parentDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    // 预见式手势进度与活跃标志
    val gestureProgress = remember { Animatable(0f) }
    var isGestureTracking by remember { mutableStateOf(false) }

    // 普通编程式关闭（点击外部、点击选项或外部状态重置）
    fun dismissWithAnimation() {
        isGestureTracking = false
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
            isGestureTracking = false
            gestureProgress.snapTo(0f)
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

    val density = androidx.compose.ui.platform.LocalDensity.current
    val offsetX = with(density) { offset.x.roundToPx() }
    val offsetY = with(density) { offset.y.roundToPx() }

    Popup(
        alignment = alignment,
        offset = IntOffset(x = offsetX, y = offsetY),
        onDismissRequest = { dismissWithAnimation() },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = false,
            dismissOnClickOutside = true
        )
    ) {
        val popupView = LocalView.current

        // 核心：在独立窗口直接接入 Android 14/15/16 原生 OnBackAnimationCallback
        DisposableEffect(popupView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val dispatcher = popupView.findOnBackInvokedDispatcher()
                val callback = object : OnBackAnimationCallback {
                    override fun onBackStarted(backEvent: BackEvent) {
                        isGestureTracking = true
                        val eased = FastOutSlowInEasing.transform(backEvent.progress)
                        scope.launch { gestureProgress.snapTo(eased) }
                    }

                    override fun onBackProgressed(backEvent: BackEvent) {
                        val eased = FastOutSlowInEasing.transform(backEvent.progress)
                        scope.launch { gestureProgress.snapTo(eased) }
                    }

                    override fun onBackInvoked() {
                        scope.launch {
                            gestureProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
                            )
                            isGestureTracking = false
                            isVisible = false
                            onDismissRequest()
                        }
                    }

                    override fun onBackCancelled() {
                        scope.launch {
                            gestureProgress.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
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
                    isGestureTracking = false
                }
            } else {
                onDispose {}
            }
        }

        // 低版本兼顾
        if (parentDispatcherOwner != null) {
            CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides parentDispatcherOwner) {
                androidx.activity.compose.BackHandler(enabled = isVisible && !isGestureTracking) {
                    dismissWithAnimation()
                }
            }
        }

        val effectiveScale = if (isGestureTracking || gestureProgress.value > 0f) {
            (1f - (gestureProgress.value * 0.40f)).coerceIn(0.60f, 1f)
        } else {
            animScale.value
        }

        val effectiveAlpha = if (isGestureTracking || gestureProgress.value > 0f) {
            (1f - (gestureProgress.value * 0.90f)).coerceIn(0f, 1f)
        } else {
            animAlpha.value
        }

        Surface(
            modifier = modifier
                .width(width)
                .graphicsLayer {
                    this.transformOrigin = transformOrigin
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
    iconTint: Color = Color.Unspecified,
    isDestructive: Boolean = false,
    isDark: Boolean = isAppInDarkTheme,
    onClick: () -> Unit
) {
    val textColor = when {
        isDestructive -> Color(0xFFEF4444)
        isDark -> Color(0xFFF3F4F6)
        else -> Color(0xFF111827)
    }

    val resolvedIconTint = when {
        iconTint != Color.Unspecified -> iconTint
        isDestructive -> Color(0xFFEF4444)
        isDark -> Color(0xFFE5E7EB)
        else -> Color(0xFF374151)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 19.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolvedIconTint,
                modifier = Modifier.size(20.dp)
            )

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
