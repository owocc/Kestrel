package com.bettershell.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
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
 * 基于 Android 官方 SeekableTransitionState 驱动
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

    // 官方底层 SeekableTransitionState
    val seekableTransitionState = remember { SeekableTransitionState(currentScreen) }

    var isGestureActive by remember { mutableStateOf(false) }
    var lastRecordedDepth by remember { mutableIntStateOf(stack.size) }

    // 监听栈变化：当通过编程式（点击按钮）导致路由栈变化时执行平滑转场
    LaunchedEffect(currentScreen) {
        if (!isGestureActive && seekableTransitionState.currentState != currentScreen) {
            seekableTransitionState.animateTo(currentScreen)
        }
    }

    // 接入官方规范的 PredictiveBackHandler + SeekableTransitionState
    PredictiveBackHandler(enabled = previousScreen != null) { progressFlow: Flow<BackEventCompat> ->
        if (previousScreen == null) return@PredictiveBackHandler
        isGestureActive = true
        try {
            // 告诉状态机目标是上一页 previousScreen
            seekableTransitionState.seekTo(0f, targetState = previousScreen)
            progressFlow.collect { event ->
                val progress = FastOutSlowInEasing.transform(event.progress)
                seekableTransitionState.seekTo(fraction = progress, targetState = previousScreen)
            }
            // 手势确认提交 (Commit)：继续以动画完成剩余位移，落地后执行出栈并立即对齐状态
            seekableTransitionState.animateTo(targetState = previousScreen)
            onPop()
            // 关键：出栈后 currentState 已经是 previousScreen，直接 snapTo 对齐，杜绝二次触发
            seekableTransitionState.snapTo(targetState = previousScreen)
            isGestureActive = false
        } catch (e: CancellationException) {
            // 手势取消：平滑倒放恢复到当前页 currentScreen
            // 关键修复：必须使用 await 方式完成倒放，确保在恢复到 currentScreen 后才标记手势结束，
            // 绝不触发 LaunchedEffect 或反向重播！
            scope.launch {
                seekableTransitionState.animateTo(targetState = currentScreen)
                seekableTransitionState.snapTo(targetState = currentScreen)
                isGestureActive = false
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
                val isPopping = stack.size < lastRecordedDepth || isGestureActive

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
