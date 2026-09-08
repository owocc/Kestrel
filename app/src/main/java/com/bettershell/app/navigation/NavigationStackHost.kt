package com.bettershell.app.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 屏幕项状态包装：管理每个压栈页面的物理进入/退出位移与透明度
 */
private class ScreenStackItem<T>(
    val key: Any,
    val screen: T,
    val translationXFraction: Animatable<Float, *>,
    val alphaAnim: Animatable<Float, *>
)

/**
 * 统一无闪烁全栈导航容器 (Unified Zero-Flicker Stack Navigation Host)
 *
 * 彻底解决多层导航在 Android 16 Predictive Back 与标准编程式路由动画之间的冲突：
 * 1. 【无论多少层，预见式返回绝对不闪烁】：手势过程中，顶层随手指滑动位移，底层按 30% 视差跟进；
 * 2. 【直接滑动松手退出】：手势松开提交时，顺势物理滑出到屏幕右侧外 (100%)，动画落地后才出栈，绝不在松手时发生瞬间重置跳变；
 * 3. 【动画独立不冲突】：
 *    - 若是手势返回：手势已经完成当前页的退出物理动作，上一个路由静止接管，绝不二次播放进场或退场动画；
 *    - 若是点击进入新页面：新页面单独播放自右向左进场 (100% -> 0%)，底层页面后退 30%；
 *    - 若是点击顶部返回按钮：当前页面单独播放自左向右退场 (0% -> 100%)，上一页归位。
 */
@Composable
fun <T : Any> NavigationStackHost(
    stack: List<T>,
    onPop: () -> Unit,
    renderScreen: @Composable (screen: T, onBack: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 内部持久维护的已渲染屏幕列表（支持物理平移动画期间的过渡留存）
    val renderedItems = remember { mutableStateListOf<ScreenStackItem<T>>() }
    var previousStack by remember { mutableStateOf<List<T>>(emptyList()) }

    // 手势预测性返回状态
    val gestureProgress = remember { Animatable(0f) }
    var isGestureInProgress by remember { mutableStateOf(false) }

    // 监听外部 stack 变更：精准区分【前进压栈】、【点击返回按钮出栈】、【手势返回完成】
    LaunchedEffect(stack) {
        if (renderedItems.isEmpty()) {
            // 首次初始化
            stack.forEachIndexed { index, s ->
                renderedItems.add(
                    ScreenStackItem(
                        key = s,
                        screen = s,
                        translationXFraction = Animatable(0f),
                        alphaAnim = Animatable(1f)
                    )
                )
            }
            previousStack = stack
            return@LaunchedEffect
        }

        val oldSize = previousStack.size
        val newSize = stack.size

        if (newSize > oldSize) {
            // ================= 1. 点击进入新页面：新页面从右边滑入 (1.0 -> 0.0) =================
            val newScreens = stack.drop(oldSize)
            newScreens.forEach { newScreen ->
                val item = ScreenStackItem(
                    key = newScreen,
                    screen = newScreen,
                    translationXFraction = Animatable(1.0f),
                    alphaAnim = Animatable(0.6f)
                )
                renderedItems.add(item)
                coroutineScope.launch {
                    item.alphaAnim.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                }
                coroutineScope.launch {
                    item.translationXFraction.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            }
        } else if (newSize < oldSize) {
            // ================= 2. 出栈（点击返回按钮 或 手势完成） =================
            val poppedItems = renderedItems.takeLast(oldSize - newSize)
            if (isGestureInProgress) {
                // 如果是刚刚通过手势滑出的，当前顶层已经物理在屏幕边缘或屏幕外，直接静默清理
                renderedItems.removeAll(poppedItems)
            } else {
                // 如果是点击返回按钮退出的：单独执行退出动画自左向右滑出 (0.0 -> 1.0)
                poppedItems.forEach { item ->
                    coroutineScope.launch {
                        item.alphaAnim.animateTo(0.2f, spring(stiffness = Spring.StiffnessMediumLow))
                    }
                    coroutineScope.launch {
                        item.translationXFraction.animateTo(
                            targetValue = 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                        renderedItems.remove(item)
                    }
                }
            }
        }
        previousStack = stack
    }

    val canPredictiveBack = stack.size > 1

    // 统一拦截 Android 16 预见式返回手势
    PredictiveBackHandler(enabled = canPredictiveBack) { progressFlow: Flow<BackEventCompat> ->
        isGestureInProgress = true
        try {
            progressFlow.collect { event ->
                val progress = FastOutSlowInEasing.transform(event.progress)
                gestureProgress.snapTo(progress)
            }
            // 用户松手确认退出 (Commit)：顺势执行快速物理滑出到屏幕最右侧 1.0f
            coroutineScope.launch {
                gestureProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 160)
                )
                // 此时页面已完全滑出可视区域，触发 onPop() 出栈
                onPop()
                gestureProgress.snapTo(0f)
                isGestureInProgress = false
            }
        } catch (e: CancellationException) {
            // 用户放弃滑动，回弹复位
            coroutineScope.launch {
                gestureProgress.animateTo(0f)
                isGestureInProgress = false
            }
        }
    }

    val currentGestureFraction = gestureProgress.value
    val isGestureActive = isGestureInProgress || currentGestureFraction > 0f

    // 渲染所有存活的页面层级（保证上一层与当前层完全处于独立的视差渲染树中）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val totalCount = renderedItems.size

        renderedItems.forEachIndexed { index, item ->
            key(item.key) {
                val isTopScreen = index == totalCount - 1
                val isPreviousScreen = index == totalCount - 2

                // 物理位移与透明度计算
                var layerTranslationX = 0f
                var layerAlpha = item.alphaAnim.value

                if (isGestureActive) {
                    if (isTopScreen) {
                        // 顶层正在手势滑出
                        layerTranslationX = currentGestureFraction
                        layerAlpha = (1f - currentGestureFraction * 0.7f).coerceIn(0f, 1f)
                    } else if (isPreviousScreen) {
                        // 上一层正在从 -30% 跟手透视推进到 0%
                        layerTranslationX = -0.30f * (1f - currentGestureFraction)
                        layerAlpha = (0.6f + currentGestureFraction * 0.4f).coerceIn(0f, 1f)
                    } else {
                        // 更深层级全部保持在左侧静止
                        layerTranslationX = -0.30f
                        layerAlpha = 0f
                    }
                } else {
                    // 非手势模式：遵循自身的动画通道
                    layerTranslationX = item.translationXFraction.value
                    if (!isTopScreen && layerTranslationX == 0f) {
                        // 作为普通后台页面，静止等待
                        layerTranslationX = 0f
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = size.width * layerTranslationX
                            alpha = layerAlpha
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    renderScreen(item.screen) { onPop() }
                }
            }
        }
    }
}
