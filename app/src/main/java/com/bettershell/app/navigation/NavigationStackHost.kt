package com.bettershell.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 官方标准全栈导航容器 (Official Standard Predictive Back Navigation Host)
 * 基于 Android 官方 SeekableTransitionState 驱动：
 * 1. 彻底解决手动计算 offset 带来的松手闪回原位/白屏问题；
 * 2. 手势滑动时实时 Seek 目标转场动画（popEnter/popExit）；
 * 3. 手势确认退出时直接向终点 Snap/Animate 落地，再同步状态；
 * 4. 按钮点击退出与压栈进入均走标准 TransitionSpec，无缝统一。
 */
@Composable
fun <T : Any> NavigationStackHost(
    stack: List<T>,
    onPop: () -> Unit,
    renderScreen: @Composable (screen: T, onBack: () -> Unit) -> Unit
) {
    val currentScreen = stack.last()
    val previousScreen = if (stack.size > 1) stack[stack.size - 2] else null
    val scope = rememberCoroutineScope()

    // 官方底层 SeekableTransitionState: 专为预测性返回设计的帧精准可搜寻状态机
    val seekableTransitionState = remember { SeekableTransitionState(currentScreen) }

    var isGestureInProgress by remember { mutableStateOf(false) }
    var lastRecordedDepth by remember { mutableIntStateOf(stack.size) }

    // 监听栈变化：当普通点击（非手势）导致栈变化时，执行平滑转场动画
    LaunchedEffect(currentScreen) {
        if (!isGestureInProgress && seekableTransitionState.currentState != currentScreen) {
            seekableTransitionState.animateTo(currentScreen)
        }
    }

    // 接入官方规范的 PredictiveBackHandler + SeekableTransitionState
    PredictiveBackHandler(enabled = previousScreen != null) { progressFlow: Flow<BackEventCompat> ->
        if (previousScreen == null) return@PredictiveBackHandler
        isGestureInProgress = true
        try {
            // 告诉状态机目标是上一页 previousScreen
            seekableTransitionState.seekTo(0f, targetState = previousScreen)
            progressFlow.collect { event ->
                val progress = FastOutSlowInEasing.transform(event.progress)
                // 核心：由系统将真实手势进度精准 Seek 进 Transition，无任何多余层与状态突变
                seekableTransitionState.seekTo(fraction = progress, targetState = previousScreen)
            }
            // 手势确认提交 (Commit)：继续完成剩余进度的动画，落地后触发 onPop
            scope.launch {
                seekableTransitionState.animateTo(targetState = previousScreen)
                onPop()
                isGestureInProgress = false
            }
        } catch (e: CancellationException) {
            // 手势取消：平滑倒放恢复到当前页
            scope.launch {
                seekableTransitionState.animateTo(targetState = currentScreen)
                isGestureInProgress = false
            }
        }
    }

    // 官方统一的 Transition 构造
    val transition = rememberTransition(
        transitionState = seekableTransitionState,
        label = "OfficialNavigationTransition"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        transition.AnimatedContent(
            transitionSpec = {
                val isPopping = stack.size < lastRecordedDepth || isGestureInProgress

                if (isPopping) {
                    // 后退/预测性返回：上一页自左侧 -30% 视差归位，当前页自 0% 向右滑出到 100%
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(durationMillis = 300))
                    )
                } else {
                    // 前进压栈：新页面自右侧 100% 滑入，旧页面向左后退 30%
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(durationMillis = 300))
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { targetScreen ->
            renderScreen(targetScreen) { onPop() }
        }
    }

    lastRecordedDepth = stack.size
}
