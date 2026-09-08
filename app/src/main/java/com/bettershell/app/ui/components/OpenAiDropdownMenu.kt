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
 * 严格互锁与状态隔离：
 * 1. 【手势返回后绝不二次播放返回动画】：手势在物理上已经缩回，提交时直接落地销毁，杜绝再次触发 dismiss 动画；
 * 2. 【手势取消后绝不重新播放弹出动画】：手势放弃时，在当前手势通道内弹簧平滑吸附回 0f，绝不重新重绘或触发展开动画；
 * 3. 【出入同源 Flip】：从右上角 (1f, 0f) 锚点弹出和缩回。
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

    val parentDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    // 预见式手势进度与活跃标志
    val gestureProgress = remember { Animatable(0f) }
    var isGestureTracking by remember { mutableStateOf(false) }

    // 普通编程式关闭（点击外部或点击选项）
    fun dismissWithAnimation() {
        if (isGestureTracking) return
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
            if (!isVisible) {
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
            }
        } else if (isVisible && !isGestureTracking) {
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
                        // 核心防二次重播：手势确认退出时，在手势通道内顺势完成缩小到 1.0f，然后直接静默销毁！
                        // 绝对不调用 dismissWithAnimation()！杜绝二次播放返回动画！
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
                        // 核心防二次弹出：手势放弃时，在当前手势通道内平滑弹簧复位到 0f，
                        // 绝不触碰 animScale/animAlpha，绝对不重新播放弹出动画！
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
                    // Android 原生 Flip 核心：以右上角 (1f, 0f) 为锚点
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
