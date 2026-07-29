package com.android.purebilibili.core.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.UiPreset

/** Smoothing factor that approximates Apple's continuous corner curvature. */
const val IOS_CONTINUOUS_CORNER_SMOOTHING = 0.55f

fun shouldUseIosContinuousRounding(uiPreset: UiPreset): Boolean = uiPreset == UiPreset.IOS

fun IosContinuousRoundedCornerShape(
    cornerRadius: Dp,
    smoothing: Float = IOS_CONTINUOUS_CORNER_SMOOTHING
): Shape = IosContinuousRoundedCornerShape(
    topStart = cornerRadius,
    topEnd = cornerRadius,
    bottomEnd = cornerRadius,
    bottomStart = cornerRadius,
    smoothing = smoothing
)

fun IosContinuousRoundedCornerShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
    smoothing: Float = IOS_CONTINUOUS_CORNER_SMOOTHING
): Shape = IosContinuousRoundedCornerShapeImpl(
    topStart = topStart,
    topEnd = topEnd,
    bottomEnd = bottomEnd,
    bottomStart = bottomStart,
    smoothing = smoothing
)

internal class IosContinuousRoundedCornerShapeImpl(
    private val topStart: Dp,
    private val topEnd: Dp,
    private val bottomEnd: Dp,
    private val bottomStart: Dp,
    private val smoothing: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radii = with(density) {
            if (layoutDirection == LayoutDirection.Rtl) {
                CornerRadiiPx(
                    topStart = topEnd.toPx(),
                    topEnd = topStart.toPx(),
                    bottomEnd = bottomStart.toPx(),
                    bottomStart = bottomEnd.toPx()
                )
            } else {
                CornerRadiiPx(
                    topStart = topStart.toPx(),
                    topEnd = topEnd.toPx(),
                    bottomEnd = bottomEnd.toPx(),
                    bottomStart = bottomStart.toPx()
                )
            }
        }
        val path = Path().apply {
            addContinuousRoundRect(
                width = size.width,
                height = size.height,
                radii = radii,
                smoothing = smoothing
            )
        }
        return Outline.Generic(path)
    }
}

internal data class CornerRadiiPx(
    val topStart: Float,
    val topEnd: Float,
    val bottomEnd: Float,
    val bottomStart: Float
)

internal fun Path.addContinuousRoundRect(
    width: Float,
    height: Float,
    radii: CornerRadiiPx,
    smoothing: Float
) {
    val maxRadius = minOf(width, height) / 2f
    val topStart = radii.topStart.coerceIn(0f, maxRadius)
    val topEnd = radii.topEnd.coerceIn(0f, maxRadius)
    val bottomEnd = radii.bottomEnd.coerceIn(0f, maxRadius)
    val bottomStart = radii.bottomStart.coerceIn(0f, maxRadius)
    val smooth = smoothing.coerceIn(0f, 1f)
    val controlInset = 1f - smooth * 0.22f

    reset()
    moveTo(0f, topStart)
    if (topStart > 0f) {
        cubicTo(
            0f,
            topStart * controlInset,
            topStart * controlInset,
            0f,
            topStart,
            0f
        )
    }
    lineTo(width - topEnd, 0f)
    if (topEnd > 0f) {
        cubicTo(
            width - topEnd * controlInset,
            0f,
            width,
            topEnd * controlInset,
            width,
            topEnd
        )
    }
    lineTo(width, height - bottomEnd)
    if (bottomEnd > 0f) {
        cubicTo(
            width,
            height - bottomEnd * controlInset,
            width - bottomEnd * controlInset,
            height,
            width - bottomEnd,
            height
        )
    }
    lineTo(bottomStart, height)
    if (bottomStart > 0f) {
        cubicTo(
            bottomStart * controlInset,
            height,
            0f,
            height - bottomStart * controlInset,
            0f,
            height - bottomStart
        )
    }
    close()
}