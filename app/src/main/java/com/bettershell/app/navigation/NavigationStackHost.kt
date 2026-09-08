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
 * 学习自 Telegram 的 BaseFragment 图层容器包装项：
 * 每个压栈页面在物理层面上始终真实保持渲染树挂载，由独立的 Animatable 驱动位移与遮罩。
 */
private class TelegramLayerItem<T>(
    val key: Any,
    val screen: T,
    val translationFraction: Animatable<Float, *>, // 0.0f = 停留在视口, 1.0f = 在右侧屏幕外
    val dimAlpha: Animatable<Float, *>             // 黑色视差遮罩层透明度 (0.0f ~ 0.4f)
)

/**
 * Telegram 级全栈物理视差导航容器 (Telegram-Style Navigation Stack Host)
 *
 * 彻底告别 Jetpack Compose AnimatedContent/SeekableTransitionState 带来的时序冲突与闪烁：
 * 1. 【永不销毁重建视口】：像 Telegram 的 ActionBarLayout 一样，整个栈是一组重叠的物理图层；
 * 2. 【零闪烁取消手势】：手指取消返回时，顶层页面以物理弹簧自然吸附回 0.0f，上一页遮罩同步复原，没有任何离散状态重置；
 * 3. 【零闪烁手势提交】：手指决定退出时，顺势补间滑动到 1.0f，彻底移出视口后再安全从列表中移除；
 * 4. 【独立压栈与出栈】：
 *    - 点击进入新页面：新页面自 1.0f 滑动至 0.0f，底层页面加深遮罩并后退 30% 视差；
 *    - 点击返回按钮：顶层页面平滑滑出至 1.0f 后移除。
 */
@Composable
fun <T : Any> NavigationStackHost(
    stack: List<T>,
    onPop: () -> Unit,
    renderScreen: @Composable (screen: T, onBack: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 像 Telegram 一样持久维护一个真实图层栈
    val layers = remember { mutableStateListOf<TelegramLayerItem<T>>() }
    var previousStackSnapshot by remember { mutableStateOf<List<T>>(emptyList()) }

    // 预见式返回手势中产生的连续偏移 (0.0f ~ 1.0f)
    val gestureProgress = remember { Animatable(0f) }
    var isGestureTracking by remember { mutableStateOf(false) }

    // 监听外部路由栈的变化，驱动 Telegram 风格的物理滑入/滑出
    LaunchedEffect(stack) {
        if (layers.isEmpty()) {
            // 首次初始化
            stack.forEach { s ->
                layers.add(
                    TelegramLayerItem(
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
            // 底层所有已有页面增加遮罩
            val topExisting = layers.lastOrNull()
            topExisting?.let {
                coroutineScope.launch {
                    it.dimAlpha.animateTo(0.35f, tween(300))
                }
            }

            // 新压入的页面自右向左物理滑入
            val incomingScreens = stack.drop(oldSize)
            incomingScreens.forEach { incoming ->
                val newLayer = TelegramLayerItem(
                    key = incoming,
                    screen = incoming,
                    translationFraction = Animatable(1.0f),
                    dimAlpha = Animatable(0f)
                )
                layers.add(newLayer)
                coroutineScope.launch {
                    newLayer.translationFraction.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    )
                }
            }
        } else if (newSize < oldSize) {
            // ================= 2. 出栈 (Pop) =================
            val poppedCount = oldSize - newSize
            val poppedLayers = layers.takeLast(poppedCount)

            // 恢复上一层的视差遮罩
            val activeTop = layers.getOrNull(newSize - 1)
            activeTop?.let {
                coroutineScope.launch {
                    it.dimAlpha.animateTo(0f, tween(260))
                }
            }

            if (isGestureTracking) {
                // 手势已经在物理上把页面滑到 1.0f 之外了，直接静默移除
                layers.removeAll(poppedLayers)
            } else {
                // 点击左上角返回按钮：顺滑滑出到屏幕右侧 (0.0f -> 1.0f)
                poppedLayers.forEach { layer ->
                    coroutineScope.launch {
                        layer.translationFraction.animateTo(
                            targetValue = 1.0f,
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                        )
                        layers.remove(layer)
                    }
                }
            }
        }
        previousStackSnapshot = stack
    }

    val canBack = stack.size > 1

    // 拦截系统的 Predictive Back 连续手势事件
    PredictiveBackHandler(enabled = canBack) { progressFlow: Flow<BackEventCompat> ->
        isGestureTracking = true
        try {
            progressFlow.collect { event ->
                val eased = FastOutSlowInEasing.transform(event.progress)
                gestureProgress.snapTo(eased)
            }
            // 手势确认提交 (Commit)：继续以动画平滑滑出到 100% 屏幕外，落地后触发出栈
            coroutineScope.launch {
                gestureProgress.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                )
                onPop()
                gestureProgress.snapTo(0f)
                isGestureTracking = false
            }
        } catch (e: CancellationException) {
            // 核心修复【取消手势】：像 Telegram 一样，物理弹簧无缝吸附回到 0f
            // 绝不触发路由重绘、绝不重播上一页进入动画！
            coroutineScope.launch {
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

    val currentGesture = gestureProgress.value
    val totalLayers = layers.size

    // 渲染所有真实层级
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        layers.forEachIndexed { index, layer ->
            key(layer.key) {
                val isTop = index == totalLayers - 1
                val isPrevious = index == totalLayers - 2

                // 物理位移与遮罩计算 (Telegram 视差算法)
                val transFraction = when {
                    isGestureTracking && isTop -> currentGesture
                    isGestureTracking && isPrevious -> -0.30f * (1f - currentGesture)
                    !isGestureTracking && isTop -> layer.translationFraction.value
                    !isGestureTracking && !isTop -> {
                        // 在后台的页面，如果有新的在压入/退回，保持微小视差
                        if (isPrevious) -0.30f * (1f - (layers.lastOrNull()?.translationFraction?.value ?: 0f)) else 0f
                    }
                    else -> 0f
                }

                val currentDimAlpha = when {
                    isGestureTracking && isPrevious -> 0.35f * (1f - currentGesture)
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
                            // Telegram 经典的黑色视差遮罩：上一页暗淡，随返回渐变透明
                            if (currentDimAlpha > 0f) {
                                drawRect(Color.Black.copy(alpha = currentDimAlpha.coerceIn(0f, 0.4f)))
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
