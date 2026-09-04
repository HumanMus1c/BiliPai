package com.android.purebilibili.core.ui.transition

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoCardNativeSnapshotPolicyTest {
    @Test
    fun unrecordedNativeLayerFallsBackToReconstructedChrome() {
        assertFalse(isNativeVideoCardLayerDrawable(widthPx = 0, heightPx = 0))
        assertFalse(isNativeVideoCardLayerDrawable(widthPx = 320, heightPx = 1))
        assertTrue(isNativeVideoCardLayerDrawable(widthPx = 320, heightPx = 240))
    }

    @Test
    fun clickKeepsTheListCardUntilTheFlyingOverlayCoversIt() {
        assertFalse(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = true,
                phase = VideoCardTransitionBackgroundPhase.OPENING,
                depthProgress = 0f,
                isReturnGestureInProgress = false,
            ),
        )
        assertFalse(
            isVideoCardFlyingOverlayCoveringSource(
                phase = VideoCardTransitionBackgroundPhase.OPENING,
                depthProgress = 0f,
                isReturnGestureInProgress = false,
            ),
        )
    }

    @Test
    fun stationarySourceCardIsEmptyWhileTheFlyingCardOwnsTheSlot() {
        assertTrue(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = true,
                phase = VideoCardTransitionBackgroundPhase.OPENING,
                depthProgress = 0.2f,
                isReturnGestureInProgress = false,
            ),
        )
        assertTrue(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = true,
                phase = VideoCardTransitionBackgroundPhase.HELD,
                depthProgress = 1f,
                isReturnGestureInProgress = false,
            ),
        )
        assertTrue(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = true,
                phase = VideoCardTransitionBackgroundPhase.HELD,
                depthProgress = 0.4f,
                isReturnGestureInProgress = true,
            ),
        )
        assertTrue(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = true,
                phase = VideoCardTransitionBackgroundPhase.RETURNING,
                depthProgress = 0.5f,
                isReturnGestureInProgress = false,
            ),
        )
    }

    @Test
    fun stationarySourceCardReturnsAfterLandAndNeverHidesUnrelatedCards() {
        assertFalse(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = true,
                phase = VideoCardTransitionBackgroundPhase.IDLE,
                depthProgress = 0f,
                isReturnGestureInProgress = false,
            ),
        )
        assertFalse(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = true,
                phase = VideoCardTransitionBackgroundPhase.RETURNING,
                depthProgress = 0f,
                isReturnGestureInProgress = false,
            ),
        )
        assertFalse(
            shouldHideStationarySourceCard(
                isSharedMorphSourceCard = false,
                phase = VideoCardTransitionBackgroundPhase.OPENING,
                depthProgress = 0.2f,
                isReturnGestureInProgress = false,
            ),
        )
    }

    @Test
    fun clickFreezeIsReadByTheAlreadyMountedDrawModifier() {
        val snapshotSource = sourceFile(
            "app/src/main/java/com/android/purebilibili/core/ui/transition/VideoCardNativeSnapshot.kt",
            "src/main/java/com/android/purebilibili/core/ui/transition/VideoCardNativeSnapshot.kt",
        )
        val homeCardSource = sourceFile(
            "app/src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt",
            "src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt",
        )

        assertTrue(snapshotSource.contains("freezeProvider: () -> Boolean"))
        assertTrue(snapshotSource.contains("if (!freezeProvider())"))
        assertFalse(snapshotSource.contains("if (!freeze)"))
        assertTrue(homeCardSource.contains("freezeNativeCardLayer.value = true"))
        assertTrue(
            homeCardSource.contains("freezeProvider = { freezeNativeCardLayer.value }"),
        )
    }

    private fun sourceFile(primary: String, fallback: String): String =
        listOf(File(primary), File(fallback)).first { it.exists() }.readText()
}
