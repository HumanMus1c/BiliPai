package com.android.purebilibili.feature.video.screen

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceLayout
import kotlin.math.roundToInt

/** Click-time source geometry used only to align the real detail media with the source cover. */
internal data class VideoDetailReturnSourceCardLayout(
    val sourceScale: Float,
    val cardWidthPx: Float,
    val cardHeightPx: Float,
    val coverHeightPx: Float,
    val coverWidthPx: Float,
    val coverOffsetXPx: Float = 0f,
    val coverOffsetYPx: Float = 0f,
    val infoWidthPx: Float,
    val infoHeightPx: Float,
    val cardAnchorXInViewportPx: Float,
    val cardAnchorYInViewportPx: Float,
    val infoAnchorXInViewportPx: Float,
    val infoAnchorYInViewportPx: Float,
    val layout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
) {
    val canRender: Boolean
        get() = sourceScale > 0f &&
            cardWidthPx > 1f &&
            cardHeightPx > 1f &&
            infoWidthPx > 1f &&
            infoHeightPx > 1f &&
            layout != VideoCardSourceLayout.COVER_ONLY

    @Deprecated("Use infoWidthPx", ReplaceWith("infoWidthPx"))
    val sourceWidthPx: Float get() = infoWidthPx

    @Deprecated("Use infoHeightPx", ReplaceWith("infoHeightPx"))
    val sourceInfoHeightPx: Float get() = infoHeightPx

    @Deprecated("Use infoAnchorYInViewportPx", ReplaceWith("infoAnchorYInViewportPx"))
    val anchorYInViewportPx: Float get() = infoAnchorYInViewportPx

    @Deprecated("Use infoAnchorXInViewportPx", ReplaceWith("infoAnchorXInViewportPx"))
    val anchorXInViewportPx: Float get() = infoAnchorXInViewportPx
}

private fun emptyReturnSourceLayout(
    layout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
) = VideoDetailReturnSourceCardLayout(
    sourceScale = 0f,
    cardWidthPx = 0f,
    cardHeightPx = 0f,
    coverHeightPx = 0f,
    coverWidthPx = 0f,
    infoWidthPx = 0f,
    infoHeightPx = 0f,
    cardAnchorXInViewportPx = 0f,
    cardAnchorYInViewportPx = 0f,
    infoAnchorXInViewportPx = 0f,
    infoAnchorYInViewportPx = 0f,
    layout = layout,
)

/** Resolves the measured source cover inside the full-width detail entry. */
internal fun resolveVideoDetailReturnSourceCardLayout(
    viewportWidthPx: Float,
    sourceBounds: Rect?,
    sourceCoverBounds: Rect?,
    sourceLayout: VideoCardSourceLayout? = null,
): VideoDetailReturnSourceCardLayout {
    val viewportWidth = viewportWidthPx.coerceAtLeast(1f)
    val bounds = sourceBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return emptyReturnSourceLayout()
    val coverBounds = sourceCoverBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return emptyReturnSourceLayout()
    val layout = sourceLayout ?: resolveVideoCardSourceLayout(bounds, coverBounds)
    val sourceScale = (bounds.width / viewportWidth).coerceIn(0.01f, 1f)

    return when (layout) {
        VideoCardSourceLayout.STACKED -> {
            val horizontalTolerance = bounds.width * 0.1f
            val isFullWidthCover = coverBounds.left <= bounds.left + horizontalTolerance &&
                coverBounds.right >= bounds.right - horizontalTolerance
            val isVerticallyInsideCard = coverBounds.top >= bounds.top - 1f &&
                coverBounds.bottom in (bounds.top + 1f)..(bounds.bottom + 1f)
            if (!isFullWidthCover || !isVerticallyInsideCard) {
                return emptyReturnSourceLayout(layout)
            }
            val coverHeight = coverBounds.height.coerceAtLeast(0f)
            val coverOffsetY = (coverBounds.top - bounds.top).coerceAtLeast(0f)
            val infoHeight = (bounds.bottom - coverBounds.bottom).coerceAtLeast(0f)
            if (infoHeight <= 1f || coverHeight <= 1f) {
                return emptyReturnSourceLayout(layout)
            }
            VideoDetailReturnSourceCardLayout(
                sourceScale = sourceScale,
                cardWidthPx = bounds.width,
                cardHeightPx = bounds.height,
                coverHeightPx = coverHeight,
                coverWidthPx = coverBounds.width.coerceAtMost(bounds.width),
                coverOffsetXPx = (coverBounds.left - bounds.left).coerceAtLeast(0f),
                coverOffsetYPx = coverOffsetY,
                infoWidthPx = bounds.width,
                infoHeightPx = infoHeight,
                cardAnchorXInViewportPx = 0f,
                cardAnchorYInViewportPx = 0f,
                infoAnchorXInViewportPx = 0f,
                infoAnchorYInViewportPx = (coverOffsetY + coverHeight) / sourceScale,
                layout = layout,
            )
        }

        VideoCardSourceLayout.SIDE_BY_SIDE -> {
            val coverOnLeft = coverBounds.center.x <= bounds.center.x
            val coverNarrower = coverBounds.width < bounds.width * 0.85f
            val coverWidth: Float
            val coverHeight: Float
            val coverOffsetX: Float
            val coverOffsetY: Float
            val infoWidth: Float
            if (coverOnLeft && coverNarrower) {
                coverWidth = coverBounds.width.coerceAtLeast(1f)
                coverHeight = coverBounds.height
                    .coerceAtLeast(1f)
                    .coerceAtMost(bounds.height)
                coverOffsetX = (coverBounds.left - bounds.left).coerceAtLeast(0f)
                coverOffsetY = (coverBounds.top - bounds.top).coerceAtLeast(0f)
                infoWidth = (bounds.right - coverBounds.right).coerceAtLeast(0f)
            } else {
                coverWidth = bounds.width * 0.38f
                coverHeight = bounds.height * 0.85f
                coverOffsetX = 0f
                coverOffsetY = (bounds.height - coverHeight) / 2f
                infoWidth = bounds.width - coverWidth
            }
            val infoHeight = bounds.height.coerceAtLeast(0f)
            if (infoWidth <= 1f || infoHeight <= 1f || coverWidth <= 1f) {
                return emptyReturnSourceLayout(layout)
            }
            VideoDetailReturnSourceCardLayout(
                sourceScale = sourceScale,
                cardWidthPx = bounds.width,
                cardHeightPx = bounds.height,
                coverHeightPx = coverHeight,
                coverWidthPx = coverWidth,
                coverOffsetXPx = coverOffsetX,
                coverOffsetYPx = coverOffsetY,
                infoWidthPx = infoWidth,
                infoHeightPx = infoHeight,
                cardAnchorXInViewportPx = 0f,
                cardAnchorYInViewportPx = 0f,
                infoAnchorXInViewportPx = (coverOffsetX + coverWidth) / sourceScale,
                infoAnchorYInViewportPx = 0f,
                layout = layout,
            )
        }

        VideoCardSourceLayout.COVER_ONLY -> emptyReturnSourceLayout(layout)
    }
}

internal fun resolveVideoDetailReturnCoverHeightInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float = if (layout.canRender) layout.coverHeightPx / layout.sourceScale else 0f

internal fun resolveVideoDetailReturnCoverWidthInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float = if (layout.canRender) layout.coverWidthPx / layout.sourceScale else 0f

internal fun resolveVideoDetailReturnCoverOffsetXInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float = if (layout.canRender) layout.coverOffsetXPx / layout.sourceScale else 0f

internal fun resolveVideoDetailReturnCoverOffsetYInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float = if (layout.canRender) layout.coverOffsetYPx / layout.sourceScale else 0f

internal data class VideoDetailReturnMediaLayoutFrame(
    val offsetXPx: Int,
    val offsetYPx: Int,
    val widthPx: Int,
    val heightPx: Int,
)

internal fun resolveVideoDetailReturnMediaLayoutFrame(
    containerWidthPx: Int,
    containerHeightPx: Int,
    landingLayout: VideoDetailReturnSourceCardLayout?,
    handoffProgress: Float,
): VideoDetailReturnMediaLayoutFrame {
    val safeContainerWidth = containerWidthPx.coerceAtLeast(1)
    val safeContainerHeight = containerHeightPx.coerceAtLeast(1)
    val landing = landingLayout?.takeIf { it.canRender }
    val progress = if (landing == null) 0f else handoffProgress.coerceIn(0f, 1f)
    fun interpolate(start: Float, end: Float): Int =
        (start + (end - start) * progress).roundToInt()

    val targetWidth = landing
        ?.let(::resolveVideoDetailReturnCoverWidthInEntryPx)
        ?.takeIf { it > 1f }
        ?: safeContainerWidth.toFloat()
    val targetHeight = landing
        ?.let(::resolveVideoDetailReturnCoverHeightInEntryPx)
        ?.takeIf { it > 1f }
        ?: safeContainerHeight.toFloat()
    val targetOffsetX = landing?.let(::resolveVideoDetailReturnCoverOffsetXInEntryPx) ?: 0f
    val targetOffsetY = landing?.let(::resolveVideoDetailReturnCoverOffsetYInEntryPx) ?: 0f

    return VideoDetailReturnMediaLayoutFrame(
        offsetXPx = interpolate(0f, targetOffsetX),
        offsetYPx = interpolate(0f, targetOffsetY),
        widthPx = interpolate(safeContainerWidth.toFloat(), targetWidth).coerceAtLeast(1),
        heightPx = interpolate(safeContainerHeight.toFloat(), targetHeight).coerceAtLeast(1),
    )
}

/** Remeasures only the detail media to the real source cover; it never redraws card chrome. */
internal fun Modifier.videoDetailReturnMediaLayout(
    landingLayout: VideoDetailReturnSourceCardLayout?,
    handoffProgressProvider: () -> Float,
): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedWidth || !constraints.hasBoundedHeight) {
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    } else {
        val frame = resolveVideoDetailReturnMediaLayoutFrame(
            containerWidthPx = constraints.maxWidth,
            containerHeightPx = constraints.maxHeight,
            landingLayout = landingLayout,
            handoffProgress = handoffProgressProvider(),
        )
        val placeable = measurable.measure(
            Constraints.fixed(width = frame.widthPx, height = frame.heightPx),
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(frame.offsetXPx, frame.offsetYPx)
        }
    }
}
