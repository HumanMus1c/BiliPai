package com.android.purebilibili.feature.video.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RelatedVideoGridPerformancePolicyTest {

    @Test
    fun sharedTransition_isMountedOnlyAfterRelatedCardClick() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/ui/components/RelatedVideoItem.kt"
        ).readText()
        val gridRowSource = source
            .substringAfter("fun RelatedVideoGridRow(")
            .substringBefore("val pendingVideo = actionVideo")

        assertTrue(gridRowSource.contains("transitionEnabled = false"))
        assertTrue(gridRowSource.contains("sharedTransitionEnabled = transitionEnabled"))
        assertFalse(gridRowSource.contains("isListScrolling"))
        assertTrue(source.contains("forceSharedTransitionForClick = true"))
        assertTrue(source.contains("withFrameNanos { }"))
    }

    @Test
    fun relatedCard_defersBoundsCalculationAndKeepsCoverRequestStableWhileScrolling() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/ui/components/RelatedVideoItem.kt"
        ).readText()

        assertTrue(source.contains("var value: LayoutCoordinates? = null"))
        assertTrue(source.contains("?.boundsInRoot()"))
        assertTrue(source.contains("cardCoordinatesRef.value = coordinates"))
        assertFalse(source.contains("cardBoundsRef.value = coordinates.boundsInRoot()"))
        assertTrue(source.contains("val coverRequest = remember(video.pic)"))
    }

    @Test
    fun navigation_waitsForSharedBoundsWhenTapEndsAScroll() {
        assertTrue(
            shouldDeferRelatedVideoNavigationForSharedTransition(
                sharedTransitionEnabled = true,
                cardTransitionEnabled = false,
            ),
        )
        assertFalse(
            shouldDeferRelatedVideoNavigationForSharedTransition(
                sharedTransitionEnabled = true,
                cardTransitionEnabled = true,
            ),
        )
        assertFalse(
            shouldDeferRelatedVideoNavigationForSharedTransition(
                sharedTransitionEnabled = false,
                cardTransitionEnabled = false,
            ),
        )
    }
}
