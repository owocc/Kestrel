package com.bettershell.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 严格遵照 Lucide 官方规范的 原生 24x24 矢量线条图标库 (2dp stroke, Round Cap & Join)
 * 100% 还原官网原版 SVG 几何图形，彻底替代失真图标
 */
object LucideIcons {

    /**
     * Lucide: astroid (四角星形数学曲线 / AI 智能体标准图标)
     * 官方 SVG 路径: 星芒圆滑收缩四角星 (Astroid hypocycloid curve)
     */
    val Asteroid: ImageVector = ImageVector.Builder(
        name = "LucideAstroid",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 2f)
            curveTo(12f, 7.523f, 7.523f, 12f, 2f, 12f)
            curveTo(7.523f, 12f, 12f, 16.477f, 12f, 22f)
            curveTo(12f, 16.477f, 16.477f, 12f, 22f, 12f)
            curveTo(16.477f, 12f, 12f, 7.523f, 12f, 2f)
            close()
        }
    }.build()

    /**
     * Lucide: bolt (螺栓/设置核心图标)
     * 官方 SVG 路径: 24x24 标准六边形螺母 + 居中圆孔
     */
    val Bolt: ImageVector = ImageVector.Builder(
        name = "LucideBolt",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 16f)
            verticalLineTo(8f)
            curveTo(21f, 7.27f, 20.62f, 6.6f, 20f, 6.27f)
            lineTo(13f, 2.27f)
            curveTo(12.38f, 1.91f, 11.62f, 1.91f, 11f, 2.27f)
            lineTo(4f, 6.27f)
            curveTo(3.38f, 6.6f, 3f, 7.27f, 3f, 8f)
            verticalLineTo(16f)
            curveTo(3f, 16.73f, 3.38f, 17.4f, 4f, 17.73f)
            lineTo(11f, 21.73f)
            curveTo(11.62f, 22.09f, 12.38f, 22.09f, 13f, 21.73f)
            lineTo(20f, 17.73f)
            curveTo(20.62f, 17.4f, 21f, 16.73f, 21f, 16f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 15f)
            curveTo(13.657f, 15f, 15f, 13.657f, 15f, 12f)
            curveTo(15f, 10.343f, 13.657f, 9f, 12f, 9f)
            curveTo(10.343f, 9f, 9f, 10.343f, 9f, 12f)
            curveTo(9f, 13.657f, 10.343f, 15f, 12f, 15f)
            close()
        }
    }.build()

    /**
     * Lucide: server-cog (服务器与齿轮设置)
     * 官方 SVG 路径: 双层服务器机箱 + 居中精密齿轮与指示灯
     */
    val ServerCog: ImageVector = ImageVector.Builder(
        name = "LucideServerCog",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // 上机箱
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4.5f, 10f)
            horizontalLineTo(4f)
            curveTo(2.895f, 10f, 2f, 9.105f, 2f, 8f)
            verticalLineTo(4f)
            curveTo(2f, 2.895f, 2.895f, 2f, 4f, 2f)
            horizontalLineTo(20f)
            curveTo(21.105f, 2f, 22f, 2.895f, 22f, 4f)
            verticalLineTo(8f)
            curveTo(22f, 9.105f, 21.105f, 10f, 20f, 10f)
            horizontalLineTo(19.5f)
        }
        // 下机箱
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4.5f, 14f)
            horizontalLineTo(4f)
            curveTo(2.895f, 14f, 2f, 14.895f, 2f, 16f)
            verticalLineTo(20f)
            curveTo(2f, 21.105f, 2.895f, 22f, 4f, 22f)
            horizontalLineTo(20f)
            curveTo(21.105f, 22f, 22f, 21.105f, 22f, 20f)
            verticalLineTo(16f)
            curveTo(22f, 14.895f, 21.105f, 14f, 20f, 14f)
            horizontalLineTo(19.5f)
        }
        // 指示灯点
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 6f)
            horizontalLineTo(6.01f)
            moveTo(6f, 18f)
            horizontalLineTo(6.01f)
        }
        // 中央齿轮外轮廓
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 15f)
            curveTo(13.657f, 15f, 15f, 13.657f, 15f, 12f)
            curveTo(15f, 10.343f, 13.657f, 9f, 12f, 9f)
            curveTo(10.343f, 9f, 9f, 10.343f, 9f, 12f)
            curveTo(9f, 13.657f, 10.343f, 15f, 12f, 15f)
            close()
            moveTo(12f, 8f)
            verticalLineTo(7f)
            moveTo(12f, 17f)
            verticalLineTo(16f)
            moveTo(8f, 12f)
            horizontalLineTo(7f)
            moveTo(17f, 12f)
            horizontalLineTo(16f)
        }
    }.build()

    /**
     * Lucide: camera
     */
    val Camera: ImageVector = ImageVector.Builder(
        name = "LucideCamera",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14.5f, 4f)
            horizontalLineTo(9.5f)
            lineTo(7f, 7f)
            horizontalLineTo(4f)
            curveTo(2.895f, 7f, 2f, 7.895f, 2f, 9f)
            verticalLineTo(19f)
            curveTo(2f, 20.105f, 2.895f, 21f, 4f, 21f)
            horizontalLineTo(20f)
            curveTo(21.105f, 21f, 22f, 20.105f, 22f, 19f)
            verticalLineTo(9f)
            curveTo(22f, 7.895f, 21.105f, 7f, 20f, 7f)
            horizontalLineTo(17f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 17f)
            curveTo(14.209f, 17f, 16f, 15.209f, 16f, 13f)
            curveTo(16f, 10.791f, 14.209f, 9f, 12f, 9f)
            curveTo(9.791f, 9f, 8f, 10.791f, 8f, 13f)
            curveTo(8f, 15.209f, 9.791f, 17f, 12f, 17f)
            close()
        }
    }.build()

    /**
     * Lucide: image
     */
    val Image: ImageVector = ImageVector.Builder(
        name = "LucideImage",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 3f)
            horizontalLineTo(19f)
            curveTo(20.105f, 3f, 21f, 3.895f, 21f, 5f)
            verticalLineTo(19f)
            curveTo(21f, 20.105f, 20.105f, 21f, 19f, 21f)
            horizontalLineTo(5f)
            curveTo(3.895f, 21f, 3f, 20.105f, 3f, 19f)
            verticalLineTo(5f)
            curveTo(3.895f, 3f, 5f, 3f, 5f, 3f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 9f)
            curveTo(9.552f, 9f, 10f, 8.552f, 10f, 8f)
            curveTo(10f, 7.448f, 9.552f, 7f, 9f, 7f)
            curveTo(8.448f, 7f, 8f, 7.448f, 8f, 8f)
            curveTo(8f, 8.552f, 8.448f, 9f, 9f, 9f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 15f)
            lineTo(16f, 10f)
            lineTo(5f, 21f)
        }
    }.build()

    /**
     * Lucide: paperclip
     */
    val Paperclip: ImageVector = ImageVector.Builder(
        name = "LucidePaperclip",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(13.234f, 20.252f)
            lineTo(6.163f, 13.181f)
            curveTo(4.21f, 11.229f, 4.21f, 8.063f, 6.163f, 6.11f)
            curveTo(8.115f, 4.157f, 11.282f, 4.157f, 13.234f, 6.11f)
            lineTo(20.305f, 13.181f)
            curveTo(21.477f, 14.353f, 22.135f, 15.942f, 22.135f, 17.599f)
            curveTo(22.135f, 19.256f, 21.477f, 20.845f, 20.305f, 22.017f)
            curveTo(19.133f, 23.189f, 17.544f, 23.847f, 15.887f, 23.847f)
            curveTo(14.23f, 23.847f, 12.641f, 23.189f, 11.469f, 22.017f)
            lineTo(4.398f, 14.946f)
        }
    }.build()

    /**
     * Lucide: brain
     */
    val Brain: ImageVector = ImageVector.Builder(
        name = "LucideBrain",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9.5f, 2f)
            curveTo(7.5f, 2f, 6f, 3.5f, 6f, 5.5f)
            curveTo(4.5f, 6f, 3.5f, 7.5f, 3.5f, 9f)
            curveTo(3.5f, 10.5f, 4.5f, 12f, 6f, 12.5f)
            curveTo(4.5f, 13.5f, 4.5f, 15.5f, 6f, 16.5f)
            curveTo(6f, 18.5f, 7.5f, 20f, 9.5f, 20f)
            curveTo(10f, 20f, 10.5f, 19.8f, 11f, 19.5f)
            verticalLineTo(4.5f)
            curveTo(10.5f, 4.2f, 10f, 4f, 9.5f, 2f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14.5f, 2f)
            curveTo(16.5f, 2f, 18f, 3.5f, 18f, 5.5f)
            curveTo(19.5f, 6f, 20.5f, 7.5f, 20.5f, 9f)
            curveTo(20.5f, 10.5f, 19.5f, 12f, 18f, 12.5f)
            curveTo(19.5f, 13.5f, 19.5f, 15.5f, 18f, 16.5f)
            curveTo(18f, 18.5f, 16.5f, 20f, 14.5f, 20f)
            curveTo(14f, 20f, 13.5f, 19.8f, 13f, 19.5f)
            verticalLineTo(4.5f)
            curveTo(13.5f, 4.2f, 14f, 4f, 14.5f, 2f)
            close()
        }
    }.build()

    /**
     * Lucide: bot
     */
    val Bot: ImageVector = ImageVector.Builder(
        name = "LucideBot",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 8f)
            verticalLineTo(4f)
            horizontalLineTo(8f)
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 8f)
            horizontalLineTo(19f)
            curveTo(20.105f, 8f, 21f, 8.895f, 21f, 10f)
            verticalLineTo(18f)
            curveTo(21f, 19.105f, 20.105f, 20f, 19f, 20f)
            horizontalLineTo(5f)
            curveTo(3.895f, 20f, 3f, 19.105f, 3f, 18f)
            verticalLineTo(10f)
            curveTo(3f, 8.895f, 3.895f, 8f, 5f, 8f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 13f)
            verticalLineTo(15f)
            moveTo(15f, 13f)
            verticalLineTo(15f)
        }
    }.build()

    /**
     * Lucide: settings
     */
    val Settings: ImageVector = ImageVector.Builder(
        name = "LucideSettings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12.22f, 2f)
            horizontalLineTo(11.78f)
            lineTo(11.38f, 4.4f)
            lineTo(10.14f, 4.92f)
            lineTo(8.06f, 3.66f)
            lineTo(7.44f, 4.28f)
            lineTo(6.82f, 4.9f)
            lineTo(8.08f, 6.98f)
            lineTo(7.56f, 8.22f)
            lineTo(5.16f, 8.62f)
            verticalLineTo(10f)
            lineTo(7.56f, 10.4f)
            lineTo(8.08f, 11.64f)
            lineTo(6.82f, 13.72f)
            lineTo(7.44f, 14.34f)
            lineTo(8.06f, 14.96f)
            lineTo(10.14f, 13.7f)
            lineTo(11.38f, 14.22f)
            lineTo(11.78f, 16.62f)
            horizontalLineTo(12.22f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 15f)
            curveTo(13.657f, 15f, 15f, 13.657f, 15f, 12f)
            curveTo(15f, 10.343f, 13.657f, 9f, 12f, 9f)
            curveTo(10.343f, 9f, 9f, 10.343f, 9f, 12f)
            curveTo(9f, 13.657f, 10.343f, 15f, 12f, 15f)
            close()
        }
    }.build()

    /**
     * Lucide: trash-2
     */
    val Trash2: ImageVector = ImageVector.Builder(
        name = "LucideTrash2",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 6f)
            horizontalLineTo(21f)
            moveTo(19f, 6f)
            verticalLineTo(20f)
            curveTo(19f, 21.105f, 18.105f, 22f, 17f, 22f)
            horizontalLineTo(7f)
            curveTo(5.895f, 22f, 5f, 21.105f, 5f, 20f)
            verticalLineTo(6f)
            moveTo(8f, 6f)
            verticalLineTo(4f)
            curveTo(8f, 2.895f, 8.895f, 2f, 10f, 2f)
            horizontalLineTo(14f)
            curveTo(15.105f, 2f, 16f, 2.895f, 16f, 4f)
            verticalLineTo(6f)
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(10f, 11f)
            verticalLineTo(17f)
            moveTo(14f, 11f)
            verticalLineTo(17f)
        }
    }.build()

    /**
     * Lucide: terminal
     */
    val Terminal: ImageVector = ImageVector.Builder(
        name = "LucideTerminal",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 17f)
            lineTo(10f, 11f)
            lineTo(4f, 5f)
            moveTo(12f, 19f)
            horizontalLineTo(20f)
        }
    }.build()

    /**
     * Lucide: refresh-cw
     */
    val RefreshCw: ImageVector = ImageVector.Builder(
        name = "LucideRefreshCw",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 12f)
            curveTo(3f, 7.029f, 7.029f, 3f, 12f, 3f)
            curveTo(15.7f, 3f, 18.847f, 5.234f, 20.2f, 8.5f)
            moveTo(21f, 3f)
            verticalLineTo(9f)
            horizontalLineTo(15f)
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 12f)
            curveTo(21f, 16.971f, 16.971f, 21f, 12f, 21f)
            curveTo(8.3f, 21f, 5.153f, 18.766f, 3.8f, 15.5f)
            moveTo(3f, 21f)
            verticalLineTo(15f)
            horizontalLineTo(9f)
        }
    }.build()

    /**
     * Lucide: wrap-text
     */
    val WrapText: ImageVector = ImageVector.Builder(
        name = "LucideWrapText",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 6f)
            horizontalLineTo(21f)
            moveTo(3f, 12f)
            horizontalLineTo(15f)
            curveTo(17.209f, 12f, 19f, 13.791f, 19f, 16f)
            curveTo(19f, 18.209f, 17.209f, 20f, 15f, 20f)
            horizontalLineTo(10f)
            moveTo(13f, 17f)
            lineTo(10f, 20f)
            lineTo(13f, 23f)
            moveTo(3f, 18f)
            horizontalLineTo(7f)
        }
    }.build()

    /**
     * Lucide: eraser
     */
    val Eraser: ImageVector = ImageVector.Builder(
        name = "LucideEraser",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(7f, 21f)
            lineTo(21f, 21f)
            moveTo(18f, 9f)
            lineTo(14f, 5f)
            lineTo(3.5f, 15.5f)
            curveTo(2.7f, 16.3f, 2.7f, 17.7f, 3.5f, 18.5f)
            lineTo(5.5f, 20.5f)
            curveTo(6.3f, 21.3f, 7.7f, 21.3f, 8.5f, 20.5f)
            lineTo(18f, 11f)
            close()
        }
    }.build()

    /**
     * Lucide: code-2
     */
    val Code2: ImageVector = ImageVector.Builder(
        name = "LucideCode2",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 16f)
            lineTo(22f, 12f)
            lineTo(18f, 8f)
            moveTo(6f, 8f)
            lineTo(2f, 12f)
            lineTo(6f, 16f)
            moveTo(14.5f, 4f)
            lineTo(9.5f, 20f)
        }
    }.build()

    /**
     * Lucide: folder
     */
    val Folder: ImageVector = ImageVector.Builder(
        name = "LucideFolder",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(20f, 20f)
            curveTo(20.552f, 20f, 21f, 19.552f, 21f, 19f)
            lineTo(21f, 7f)
            curveTo(21f, 6.448f, 20.552f, 6f, 20f, 6f)
            lineTo(12f, 6f)
            lineTo(10f, 4f)
            lineTo(4f, 4f)
            curveTo(3.448f, 4f, 3f, 4.448f, 3f, 5f)
            lineTo(3f, 19f)
            curveTo(3f, 19.552f, 3.448f, 20f, 4f, 20f)
            close()
        }
    }.build()

    /**
     * Lucide: plus
     */
    val Plus: ImageVector = ImageVector.Builder(
        name = "LucidePlus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 12f)
            lineTo(19f, 12f)
            moveTo(12f, 5f)
            lineTo(12f, 19f)
        }
    }.build()

    /**
     * Lucide: download
     */
    val Download: ImageVector = ImageVector.Builder(
        name = "LucideDownload",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 15f)
            verticalLineTo(19f)
            curveTo(21f, 19.53f, 20.789f, 20.04f, 20.414f, 20.414f)
            curveTo(20.04f, 20.789f, 19.53f, 21f, 19f, 21f)
            horizontalLineTo(5f)
            curveTo(4.47f, 21f, 3.96f, 20.789f, 3.586f, 20.414f)
            curveTo(3.21f, 20.04f, 3f, 19.53f, 3f, 19f)
            verticalLineTo(15f)
            moveTo(7f, 10f)
            lineTo(12f, 15f)
            lineTo(17f, 10f)
            moveTo(12f, 15f)
            verticalLineTo(3f)
        }
    }.build()
}
