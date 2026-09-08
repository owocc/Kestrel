package com.bettershell.app.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 聚合式全栈导航容器 (Unified Stack Navigation Host)
 * 彻底解决 Compose 中手势预测性返回与编程式页面切换冲突的两大顽疾：
 * 1. 【二次播放进入/退出动画】：通过严格区分手势完成 vs 按钮点击，手势完成时切换为绝对静止 (EnterTransition.None)；
 * 2. 【直接退出/松手一帧闪烁】：手势提交后，将退出的页面稳定保留在屏幕外，直到状态完全更迭，绝不在退出的瞬间发生一帧白屏或跳跃。
 */
@Composable
fun <T> NavigationStackHost(
    stack: List<T>,
    onPop: () -> Unit,
    renderScreen: @Composable (screen: T, onBack: () -> Unit) -> Unit
) {
    val currentScreen = stack.last()
    val previousScreen = if (stack.size > 1) stack[stack.size - 2] else null

    // 手势进度动画
    val gestureProgress = remember { Animatable(0f) }
    var isGestureInProgress by remember { mutableStateOf(false) }
    var isPredictivePopping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 追踪栈深变化以决定普通点击时的前进与后退方向
    var lastRecordedDepth by remember { mutableIntStateOf(stack.size) }

    // 核心拦截手势
    PredictiveBackHandler(enabled = previousScreen != null) { progressFlow: Flow<BackEventCompat> ->
        isGestureInProgress = true
        try {
            progressFlow.collect { event ->
                val progress = FastOutSlowInEasing.transform(event.progress)
                gestureProgress.snapTo(progress)
            }
            // 手势提交 (Commit)
            isPredictivePopping = true
            onPop()
        } catch (e: CancellationException) {
            // 手势取消 (Cancel)
            scope.launch {
                gestureProgress.animateTo(0f)
                isGestureInProgress = false
            }
        }
    }

    // 当栈状态完成弹出后，重置手势状态，杜绝任何一帧的闪烁
    LaunchedEffect(stack) {
        if (isPredictivePopping) {
            gestureProgress.snapTo(0f)
            isGestureInProgress = false
            isPredictivePopping = false
        }
    }

    val currentProgress = gestureProgress.value
    val isShowingGesturePreview = isGestureInProgress || currentProgress > 0f

    val foregroundTranslationFraction = currentProgress
    val foregroundAlpha = 1f - (currentProgress * 0.75f)
    val backgroundTranslationFraction = -0.30f * (1f - currentProgress)
    val backgroundAlpha = 0.5f + (currentProgress * 0.5f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 底层上一页：手势进行中真实透出预览
        if (isShowingGesturePreview && previousScreen != null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = size.width * backgroundTranslationFraction
                        alpha = backgroundAlpha
                    },
                color = MaterialTheme.colorScheme.background
            ) {
                renderScreen(previousScreen) {}
            }
        }

        // 顶层主内容：手势时直接位移，非手势时交由平滑的横向滑动动画接管
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (isShowingGesturePreview) {
                        translationX = size.width * foregroundTranslationFraction
                        alpha = foregroundAlpha
                    }
                }
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val isPopping = stack.size < lastRecordedDepth

                    if (isPredictivePopping) {
                        // 关键：手势返回已经物理完成了滑出，新到页面绝对不播放任何多余进场/出场动画
                        EnterTransition.None.togetherWith(ExitTransition.None)
                    } else if (isPopping) {
                        // 普通点击返回按钮退回
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        )
                    } else {
                        // 压栈进入新页面
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        )
                    }
                },
                label = "navigationTransition"
            ) { screen ->
                renderScreen(screen) { onPop() }
            }
        }
    }

    lastRecordedDepth = stack.size
}
