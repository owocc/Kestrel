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
 * 真正单向、无竞争、绝对零闪烁的物理分层导航容器 (Rock-Solid Multi-Layer Navigation Host)
 *
 * 彻底理顺的逻辑链条：
 * 1. 【单向数据流与状态独占】：手势中（isGestureActive）由手势 Animatable 独占驱动；非手势由各图层独立 Animatable 驱动。两者绝不交叉干扰；
 * 2. 【手势提交 (Commit)】：手势流触发完成时，使用 suspend 动画顺滑推进到 1.0f（完全滑出可视区），落地后再调用 onPop() 出栈并静默清理图层，绝不触发任何进入动画；
 * 3. 【手势取消 (Cancel)】：放弃返回时，使用 suspend 动画平滑弹簧复位到 0.0f，复位彻底完成后才退出手势状态，上一页绝不重新播放进入；
 * 4. 【压栈进入新页面 (Push)】：新页面自 1.0f 平滑滑入到 0.0f，上一个页面增加暗化遮罩；
 * 5. 【点击返回按钮退出 (Pop)】：顶层页面自 0.0f 平滑滑出到 1.0f，完全离开视口后销毁。
 */
@Composable
fun <T : Any> NavigationStackHost(
    stack: List<T>,
    onPop: () -> Unit,
    renderScreen: @Composable (screen: T, onBack: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 真实维护的图层树
    val layers = remember { mutableStateListOf<NavigationLayer<T>>() }
    var previousStackSnapshot by remember { mutableStateOf<List<T>>(emptyList()) }

    // 手势驱动的物理参数
    val gestureProgress = remember { Animatable(0f) }
    var isGestureActive by remember { mutableStateOf(false) }

    // 监听外部路由栈推入/弹出
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
                // 手势已经在物理上滑至 1.0f（屏幕外），直接静默清理
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

    // 核心拦截系统预见式返回手势
    PredictiveBackHandler(enabled = canBack) { progressFlow: Flow<BackEventCompat> ->
        isGestureActive = true
        try {
            progressFlow.collect { event ->
                val eased = FastOutSlowInEasing.transform(event.progress)
                gestureProgress.snapTo(eased)
            }
            // 【手势提交 Commit】：顺序等待平滑动画到 100%（滑出屏幕外）
            gestureProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )
            onPop()
            gestureProgress.snapTo(0f)
            isGestureActive = false
        } catch (e: CancellationException) {
            // 【手势取消 Cancel】：顺序等待物理回弹完全落地到 0f
            // 关键：在当前 suspend 挂起块内顺序执行，直到动画完全归零才重置 isGestureActive，
            // 绝不触发任何外层中间态，彻底消灭“上一页闪一下进入”！
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

                // 物理位移与遮罩计算：
                // 1. 手势活跃时：由 gestureProgress 绝对独占驱动顶层和上一层，底层完全静止；
                // 2. 非手势时：各页面遵循自身 translationFraction / dimAlpha，互不干扰。
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
