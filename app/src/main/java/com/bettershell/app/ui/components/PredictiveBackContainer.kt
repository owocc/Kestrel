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
 * 具有双层透视预览的预测式返回容器 (Predictive Back with Previous Screen Preview)
 * - 当用户侧滑返回时，底部的 previousContent (上一页) 在底层以 -30% 平行滑出至 0%
 * - 前景当前的 content 随着手势平移向右移出，透明度由 1.0 渐变衰减
 * - 这样用户在手势滑动过程中能够 100% 实时看见即将返回的目的地！
 */
@Composable
fun PredictiveBackContainer(
    enabled: Boolean = true,
    onBack: () -> Unit,
    previousContent: @Composable () -> Unit,
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
    // 前景页面：跟随手指平移向右 (0% -> 100%)，透明度平滑从 1.0f 衰减到 0.2f
    val foregroundTranslationFraction = currentProgress
    val foregroundAlpha = 1f - (currentProgress * 0.8f)

    // 底层上一页：从左侧 -30% 随进度推入到 0%
    val backgroundTranslationFraction = -0.30f * (1f - currentProgress)
    val backgroundAlpha = 0.6f + (currentProgress * 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 底层：上一页 (预见式返回的目标页面，手势激活时完全可见)
        if (currentProgress > 0f) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = size.width * backgroundTranslationFraction
                        alpha = backgroundAlpha
                    },
                color = MaterialTheme.colorScheme.background
            ) {
                previousContent()
            }
        }

        // 顶层：当前页面
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = size.width * foregroundTranslationFraction
                    alpha = foregroundAlpha
                },
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
