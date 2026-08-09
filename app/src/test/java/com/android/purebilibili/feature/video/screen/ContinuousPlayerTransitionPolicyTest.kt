package com.android.purebilibili.feature.video.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContinuousPlayerTransitionPolicyTest {

    @Test
    fun enteringFullscreenRequestsLandscapeOnlyAfterExpansionFinishes() {
        val expanding = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.Inline,
            event = ContinuousPlayerTransitionEvent.Toggle,
        )
        assertEquals(ContinuousPlayerTransitionPhase.Expanding, expanding.phase)
        assertEquals(ContinuousPlayerOrientationRequest.None, expanding.orientationRequest)

        val awaitingLandscape = reduceContinuousPlayerTransition(
            phase = expanding.phase,
            event = ContinuousPlayerTransitionEvent.ExpansionFinished,
        )
        assertEquals(
            ContinuousPlayerTransitionPhase.AwaitingLandscape,
            awaitingLandscape.phase,
        )
        assertEquals(
            ContinuousPlayerOrientationRequest.Landscape,
            awaitingLandscape.orientationRequest,
        )
    }

    @Test
    fun portraitObservationDoesNotCancelButtonEnterBeforeLandscapeRequestCompletes() {
        assertTrue(
            shouldKeepContinuousPlayerEnterPhaseWhilePortrait(
                phase = ContinuousPlayerTransitionPhase.Expanding,
                isLandscape = false,
            )
        )
        assertTrue(
            shouldKeepContinuousPlayerEnterPhaseWhilePortrait(
                phase = ContinuousPlayerTransitionPhase.AwaitingLandscape,
                isLandscape = false,
            )
        )

        assertFalse(
            shouldKeepContinuousPlayerEnterPhaseWhilePortrait(
                phase = ContinuousPlayerTransitionPhase.Fullscreen,
                isLandscape = false,
            )
        )
        assertFalse(
            shouldKeepContinuousPlayerEnterPhaseWhilePortrait(
                phase = ContinuousPlayerTransitionPhase.Expanding,
                isLandscape = true,
            )
        )
    }

    @Test
    fun systemLandscapeCompletesFullscreenWithoutRestartingAnimation() {
        val result = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.AwaitingLandscape,
            event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = true),
        )

        assertEquals(ContinuousPlayerTransitionPhase.Fullscreen, result.phase)
        assertEquals(ContinuousPlayerOrientationRequest.None, result.orientationRequest)
    }

    @Test
    fun exitingFullscreenWaitsForPortraitBeforeCollapsing() {
        val awaitingPortrait = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.Fullscreen,
            event = ContinuousPlayerTransitionEvent.Toggle,
        )
        assertEquals(
            ContinuousPlayerTransitionPhase.AwaitingPortrait,
            awaitingPortrait.phase,
        )
        assertEquals(
            ContinuousPlayerOrientationRequest.Portrait,
            awaitingPortrait.orientationRequest,
        )

        val collapsing = reduceContinuousPlayerTransition(
            phase = awaitingPortrait.phase,
            event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = false),
        )
        assertEquals(ContinuousPlayerTransitionPhase.Collapsing, collapsing.phase)
    }

    @Test
    fun togglingDuringExpansionReversesFromCurrentVisualState() {
        val result = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.Expanding,
            event = ContinuousPlayerTransitionEvent.Toggle,
        )

        assertEquals(ContinuousPlayerTransitionPhase.Collapsing, result.phase)
        assertEquals(ContinuousPlayerOrientationRequest.None, result.orientationRequest)
    }

    @Test
    fun systemLandscapeFromMidExpansionSettlesFullscreen() {
        val result = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.Expanding,
            event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = true),
        )
        assertEquals(ContinuousPlayerTransitionPhase.Fullscreen, result.phase)
    }

    @Test
    fun systemPortraitFromFullscreenOrStuckPhasesStartsCollapse() {
        assertEquals(
            ContinuousPlayerTransitionPhase.Collapsing,
            reduceContinuousPlayerTransition(
                phase = ContinuousPlayerTransitionPhase.Fullscreen,
                event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = false),
            ).phase,
        )
        assertEquals(
            ContinuousPlayerTransitionPhase.Collapsing,
            reduceContinuousPlayerTransition(
                phase = ContinuousPlayerTransitionPhase.AwaitingLandscape,
                event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = false),
            ).phase,
        )
        assertEquals(
            ContinuousPlayerTransitionPhase.Collapsing,
            reduceContinuousPlayerTransition(
                phase = ContinuousPlayerTransitionPhase.Expanding,
                event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = false),
            ).phase,
        )
    }
}
