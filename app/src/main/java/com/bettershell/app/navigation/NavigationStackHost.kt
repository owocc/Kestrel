package com.bettershell.app.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 屏幕图层项
 */
private class NavigationLayer<T>(
    val key: Any,
    val screen: T,
    val translationFraction: Animatable<Float, *>, // 0.0f = 在可视区, 1.0f = 在右侧屏幕外
    val dimAlpha: Animatable<Float, *>             // 0.0f ~ 0.35f 视差遮罩
)

/**
 * 严格顺序闭环、彻底消灭退栈重播的导航容器
 */
@Composable
fun <T : Any> NavigationStackHost(
    stack: List<T>,
    onPop: () -> Unit,
    renderScreen: @Composable (screen: T, onBack: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val layers = remember { mutableStateListOf<NavigationLayer<T>>() }
    var previousStackSnapshot by remember { mutableStateOf<List<T>>(emptyList()) }

    val gestureProgress = remember { Animatable(0f) }
    var isGestureActive by remember { mutableStateOf(false) }

    LaunchedEffect(stack) {
        if (layers.isEmpty()) {
            stack.forEach { s ->
                layers.add(
                    NavigationLayer(
                        key = s,
                        screen = s,
                        translationFraction = Animatable(0f),
                        dimAlpha = Animatable(0f)
                    )
                )
            }
            previousStackSnapshot = stack
            return@LaunchedEffect
        }

        val oldSize = previousStackSnapshot.size
        val newSize = stack.size

        if (newSize > oldSize) {
            // ================= 1. 点击进入新页面 (Push) =================
            val topExisting = layers.lastOrNull()
            topExisting?.let {
                coroutineScope.launch {
                    it.dimAlpha.animateTo(0.35f, tween(260))
                }
            }

            val incomingScreens = stack.drop(oldSize)
            incomingScreens.forEach { incoming ->
                val newLayer = NavigationLayer(
                    key = incoming,
                    screen = incoming,
                    translationFraction = Animatable(1.0f),
                    dimAlpha = Animatable(0f)
                )
                layers.add(newLayer)
                coroutineScope.launch {
                    newLayer.translationFraction.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                    )
                }
            }
        } else if (newSize < oldSize) {
            // ================= 2. 出栈 (Pop) =================
            val poppedCount = oldSize - newSize
            val poppedLayers = layers.takeLast(poppedCount)

            val activeTop = layers.getOrNull(newSize - 1)
            activeTop?.let {
                coroutineScope.launch {
                    it.dimAlpha.animateTo(0f, tween(240))
                }
            }

            if (isGestureActive) {
                // 手势提交时已经在物理滑出后静默移除，无需再做任何处理
                layers.removeAll(poppedLayers)
            } else {
                // 点击返回按钮：以动画滑出屏幕外后移除
                poppedLayers.forEach { layer ->
                    coroutineScope.launch {
                        layer.translationFraction.animateTo(
                            targetValue = 1.0f,
                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                        )
                        layers.remove(layer)
                    }
                }
            }
        }
        previousStackSnapshot = stack
    }

    val canBack = stack.size > 1

    PredictiveBackHandler(enabled = canBack) { progressFlow: Flow<BackEventCompat> ->
        isGestureActive = true
        try {
            progressFlow.collect { event ->
                val eased = FastOutSlowInEasing.transform(event.progress)
                gestureProgress.snapTo(eased)
            }
            // 【手势提交 Commit】
            // 步骤 1：先顺着手势滑出到 1.0f（屏幕最右侧完全离开可视区）
            gestureProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )

            // 步骤 2【核心排查的关键点】：
            // 在手势完全滑出屏幕后、并且在重置 gestureProgress 之前，直接从 layers 中把被退出的顶层页面移除！
            // 绝不给它在 gestureProgress 复位到 0f 时露出屏幕的机会！
            if (layers.size > 1) {
                layers.removeAt(layers.size - 1)
            }
            // 步骤 3：重置手势偏移并解除手势独占锁
            gestureProgress.snapTo(0f)
            isGestureActive = false

            // 步骤 4：通知外部出栈（此时顶层图层早已物理销毁，绝对没有任何重播可能）
            onPop()
        } catch (e: CancellationException) {
            // 【手势取消 Cancel】
            gestureProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            isGestureActive = false
        }
    }

    val currentGesture = gestureProgress.value
    val totalLayers = layers.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        layers.forEachIndexed { index, layer ->
            key(layer.key) {
                val isTop = index == totalLayers - 1
                val isPrevious = index == totalLayers - 2

                val transFraction = when {
                    isGestureActive && isTop -> currentGesture
                    isGestureActive && isPrevious -> -0.30f * (1f - currentGesture)
                    !isGestureActive && isTop -> layer.translationFraction.value
                    !isGestureActive && isPrevious -> {
                        val topTrans = layers.lastOrNull()?.translationFraction?.value ?: 0f
                        -0.30f * (1f - topTrans)
                    }
                    else -> 0f
                }

                val currentDimAlpha = when {
                    isGestureActive && isPrevious -> 0.35f * (1f - currentGesture)
                    else -> layer.dimAlpha.value
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = size.width * transFraction
                        }
                        .drawWithContent {
                            drawContent()
                            if (currentDimAlpha > 0f) {
                                drawRect(Color.Black.copy(alpha = currentDimAlpha.coerceIn(0f, 0.35f)))
                            }
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    renderScreen(layer.screen) { onPop() }
                }
            }
        }
    }
}
