package com.bettershell.app.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 平行页面预测式返回 Surface：
 * - 纯平行位移 (TranslationX) 与透明度 (Alpha) 控制，不改变缩放 (Scale 保持 1.0)
 * - 跟随手指右滑向右移出，透明度平滑衰减
 * - 松手提交 (Commit) 则退出；取消则弹性滑回原位
 */
@Composable
fun PredictiveBackSurface(
    enabled: Boolean = true,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    PredictiveBackHandler(enabled = enabled) { progressFlow: Flow<BackEventCompat> ->
        try {
            progressFlow.collect { event ->
                val target = FastOutSlowInEasing.transform(event.progress)
                progress.snapTo(target)
            }
            onBack()
        } catch (e: CancellationException) {
            scope.launch {
                progress.animateTo(0f)
            }
        }
    }

    val currentProgress = progress.value
    // 平行滑动：随着手势向右推移至屏幕宽度的 40%
    val translationFraction = currentProgress * 0.40f
    // 透明度从 1.0f 衰减到 0.4f
    val alpha = 1f - (currentProgress * 0.6f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = size.width * translationFraction
                    this.alpha = alpha
                },
            color = color
        ) {
            content()
        }
    }
}
