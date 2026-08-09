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

internal fun shouldKeepContinuousPlayerEnterPhaseWhilePortrait(
    phase: ContinuousPlayerTransitionPhase,
    isLandscape: Boolean,
): Boolean {
    return !isLandscape &&
        (
            phase == ContinuousPlayerTransitionPhase.Expanding ||
                phase == ContinuousPlayerTransitionPhase.AwaitingLandscape
            )
}

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

            // 系统旋转进横屏（非按钮路径）时，任意非全屏相位都应落到 Fullscreen，
            // 避免卡在 Expanding/AwaitingLandscape 导致回竖屏后进度仍为 1。
            event.isLandscape &&
                phase != ContinuousPlayerTransitionPhase.Fullscreen &&
                phase != ContinuousPlayerTransitionPhase.AwaitingPortrait ->
                ContinuousPlayerTransitionDecision(ContinuousPlayerTransitionPhase.Fullscreen)

            !event.isLandscape &&
                phase == ContinuousPlayerTransitionPhase.AwaitingPortrait ->
                ContinuousPlayerTransitionDecision(ContinuousPlayerTransitionPhase.Collapsing)

            // 系统旋转回竖屏：Fullscreen / 半途 Expanding / 卡在 AwaitingLandscape
            // 都应收起为 Collapsing，避免竖屏仍按全屏高度铺满。
            !event.isLandscape &&
                (
                    phase == ContinuousPlayerTransitionPhase.Fullscreen ||
                        phase == ContinuousPlayerTransitionPhase.AwaitingLandscape ||
                        phase == ContinuousPlayerTransitionPhase.Expanding
                    ) ->
                ContinuousPlayerTransitionDecision(ContinuousPlayerTransitionPhase.Collapsing)

            else -> ContinuousPlayerTransitionDecision(phase)
        }
    }
}
