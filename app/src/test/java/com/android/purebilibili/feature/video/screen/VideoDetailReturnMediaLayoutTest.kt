package com.android.purebilibili.feature.video.screen

import androidx.compose.ui.geometry.Rect
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoDetailReturnMediaLayoutTest {
    @Test
    fun stackedCardUsesMeasuredCoverGeometry() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(100f, 200f, 500f, 700f),
            sourceCoverBounds = Rect(100f, 200f, 500f, 440f),
            sourceLayout = VideoCardSourceLayout.STACKED,
        )

        assertTrue(layout.canRender)
        assertEquals(240f, layout.coverHeightPx)
        assertEquals(260f, layout.infoHeightPx)
    }

    @Test
    fun horizontalCardUsesMeasuredFullCardHeight() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(40f, 100f, 840f, 360f),
            sourceCoverBounds = Rect(40f, 100f, 320f, 300f),
            sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
        )

        assertTrue(layout.canRender)
        assertEquals(260f, layout.infoHeightPx)
        assertEquals(280f, layout.coverWidthPx)
    }

    @Test
    fun invalidBoundsDoNotProduceReturnGeometry() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = null,
            sourceCoverBounds = null,
        )

        assertFalse(layout.canRender)
    }

    @Test
    fun mediaFrameInterpolatesToMeasuredCover() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(0f, 0f, 500f, 300f),
            sourceCoverBounds = Rect(0f, 0f, 200f, 180f),
            sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
        )
        val frame = resolveVideoDetailReturnMediaLayoutFrame(
            containerWidthPx = 1000,
            containerHeightPx = 600,
            landingLayout = layout,
            handoffProgress = 1f,
        )

        assertEquals(400, frame.widthPx)
        assertEquals(360, frame.heightPx)
    }
}
