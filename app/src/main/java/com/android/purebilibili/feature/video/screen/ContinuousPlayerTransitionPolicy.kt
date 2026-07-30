package com.android.purebilibili.feature.video.screen

internal enum class ContinuousPlayerTransitionPhase {
    Inline,
    Expanding,
    AwaitingLandscape,
    Fullscreen,
    AwaitingPortrait,
    Collapsing,
}
internal enum class ContinuousPlayerOrientationRequest {
    None,
    Landscape,
    Portrait,
}

internal sealed interface ContinuousPlayerTransitionEvent {
    data object Toggle : ContinuousPlayerTransitionEvent
    data object ExpansionFinished : ContinuousPlayerTransitionEvent
    data object CollapseFinished : ContinuousPlayerTransitionEvent
    data class OrientationChanged(val isLandscape: Boolean) : ContinuousPlayerTransitionEvent
}

internal data class ContinuousPlayerTransitionDecision(
    val phase: ContinuousPlayerTransitionPhase,
    val orientationRequest: ContinuousPlayerOrientationRequest =
        ContinuousPlayerOrientationRequest.None,
)

internal fun reduceContinuousPlayerTransition(
    phase: ContinuousPlayerTransitionPhase,
    event: ContinuousPlayerTransitionEvent,
): ContinuousPlayerTransitionDecision {
    return when (event) {
        ContinuousPlayerTransitionEvent.Toggle -> when (phase) {
            ContinuousPlayerTransitionPhase.Inline,
            ContinuousPlayerTransitionPhase.Collapsing -> ContinuousPlayerTransitionDecision(
                phase = ContinuousPlayerTransitionPhase.Expanding,
            )

            ContinuousPlayerTransitionPhase.Expanding -> ContinuousPlayerTransitionDecision(
                phase = ContinuousPlayerTransitionPhase.Collapsing,
            )

            ContinuousPlayerTransitionPhase.AwaitingLandscape,
            ContinuousPlayerTransitionPhase.Fullscreen -> ContinuousPlayerTransitionDecision(
                phase = ContinuousPlayerTransitionPhase.AwaitingPortrait,
                orientationRequest = ContinuousPlayerOrientationRequest.Portrait,
            )

            ContinuousPlayerTransitionPhase.AwaitingPortrait -> ContinuousPlayerTransitionDecision(
                phase = ContinuousPlayerTransitionPhase.AwaitingLandscape,
                orientationRequest = ContinuousPlayerOrientationRequest.Landscape,
            )
        }

        ContinuousPlayerTransitionEvent.ExpansionFinished -> {
            if (phase == ContinuousPlayerTransitionPhase.Expanding) {
                ContinuousPlayerTransitionDecision(
                    phase = ContinuousPlayerTransitionPhase.AwaitingLandscape,
                    orientationRequest = ContinuousPlayerOrientationRequest.Landscape,
                )
            } else {
                ContinuousPlayerTransitionDecision(phase)
            }
        }

        ContinuousPlayerTransitionEvent.CollapseFinished -> {
            if (phase == ContinuousPlayerTransitionPhase.Collapsing) {
                ContinuousPlayerTransitionDecision(ContinuousPlayerTransitionPhase.Inline)
            } else {
                ContinuousPlayerTransitionDecision(phase)
            }
        }

        is ContinuousPlayerTransitionEvent.OrientationChanged -> when {
            event.isLandscape &&
                phase == ContinuousPlayerTransitionPhase.AwaitingLandscape ->
                ContinuousPlayerTransitionDecision(ContinuousPlayerTransitionPhase.Fullscreen)

            !event.isLandscape &&
                phase == ContinuousPlayerTransitionPhase.AwaitingPortrait ->
                ContinuousPlayerTransitionDecision(ContinuousPlayerTransitionPhase.Collapsing)

            else -> ContinuousPlayerTransitionDecision(phase)
        }
    }
}
