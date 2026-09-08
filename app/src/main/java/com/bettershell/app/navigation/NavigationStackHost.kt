package com.bettershell.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * 极简、单向、绝对零白屏的全栈导航容器 (Zero-Flicker Clean Navigation Host)
 *
 * 核心原理解析：
 * 1. 彻底废弃易出 bug 的“中间层状态同步”；直接让 Compose 的 AnimatedContent 接管页面；
 * 2. 为什么之前预见式返回会二次播放进入动画？
 *    因为 AnimatedContent 看到 stack 变了，默认走 popExit/popEnter；
 *    当由手势退出时，标记 isPredictiveExit = true，让 AnimatedContent 采用 EnterTransition.None / ExitTransition.None，
 *    新页面静止呈现，绝不回放！
 * 3. 为什么手势滑动时看得见上一页？
 *    在手势发生期间 (isGestureActive)，底层直接透明展示 previousScreen，前景当前页随手势移动；
 *    松手触发 onPop()，当前页直接无动画交接给目标页，0ms 无缝衔接，绝无白屏！
 */
@Composable
fun <T : Any> NavigationStackHost(
    stack: List<T>,
    onPop: () -> Unit,
    renderScreen: @Composable (screen: T, onBack: () -> Unit) -> Unit
) {
    val currentScreen = stack.last()
    val previousScreen = if (stack.size > 1) stack[stack.size - 2] else null

    // 手势滑动进度 (0f ~ 1f)
    var gestureProgress by remember { mutableStateOf(0f) }
    var isGestureActive by remember { mutableStateOf(false) }
    // 标记是否刚从预见式返回手势退出
    var isFromGestureExit by remember { mutableStateOf(false) }

    var lastStackSize by remember { mutableIntStateOf(stack.size) }

    // 拦截系统的预测性返回手势
    PredictiveBackHandler(enabled = previousScreen != null) { progressFlow: Flow<BackEventCompat> ->
        isGestureActive = true
        gestureProgress = 0f
        try {
            progressFlow.collect { event ->
                gestureProgress = FastOutSlowInEasing.transform(event.progress)
            }
            // 手势确认提交：标记来自手势，直接出栈！
            // 由于手势已经将当前页滑走，AnimatedContent 此时切换为 None + None，绝对不播放多余动画！
            isFromGestureExit = true
            isGestureActive = false
            gestureProgress = 0f
            onPop()
        } catch (e: CancellationException) {
            // 手势取消：复位
            isGestureActive = false
            gestureProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 【手势滑动时的上一页真实透视层】
        // 仅在手势滑动中渲染，具有 30% 视差与黑色半透明遮罩
        if (isGestureActive && previousScreen != null) {
            val bgTranslation = -0.30f * (1f - gestureProgress)
            val bgDim = 0.30f * (1f - gestureProgress)

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = size.width * bgTranslation
                    },
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    renderScreen(previousScreen) {}
                    if (bgDim > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = bgDim))
                        )
                    }
                }
            }
        }

        // 【主页面内容容器】
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 手势中：主页面跟手向右位移与淡出
                    if (isGestureActive) {
                        translationX = size.width * gestureProgress
                        alpha = 1f - (gestureProgress * 0.5f)
                    }
                }
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val isPopping = stack.size < lastStackSize

                    if (isFromGestureExit) {
                        // 核心：如果是手势返回退出的，上一页直接静止呈现，退出的当前页直接静止消失！
                        // 彻底杜绝“又播放一次进入动画然后又消失”的幽灵动画！
                        EnterTransition.None.togetherWith(ExitTransition.None)
                    } else if (isPopping) {
                        // 普通点击左上角返回按钮：正常平滑滑出
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(durationMillis = 260)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(durationMillis = 260))
                        )
                    } else {
                        // 普通点击压栈进入新页面：新页面自右侧滑入
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(durationMillis = 260)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(durationMillis = 260))
                        )
                    }
                },
                label = "navigationTransition"
            ) { screen ->
                renderScreen(screen) {
                    isFromGestureExit = false
                    onPop()
                }
            }
        }
    }

    lastStackSize = stack.size
    // 渲染完成后重置手势标记
    if (isFromGestureExit) {
        isFromGestureExit = false
    }
}
