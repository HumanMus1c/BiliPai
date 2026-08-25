package com.android.purebilibili.core.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import com.android.purebilibili.core.util.AppWindowAdaptiveInfo
import com.android.purebilibili.core.util.LocalAppWindowAdaptiveInfo

internal fun resolveOccludingHingeInputBounds(
    adaptiveInfo: AppWindowAdaptiveInfo,
    containerWidthPx: Int,
    containerHeightPx: Int,
): IntRect? {
    if (!adaptiveInfo.foldingFeature.isOccluding) return null
    val hingeBounds = adaptiveInfo.foldingFeature.hingeBounds ?: return null
    val left = hingeBounds.left.coerceIn(0, containerWidthPx)
    val top = hingeBounds.top.coerceIn(0, containerHeightPx)
    val right = hingeBounds.right.coerceIn(0, containerWidthPx)
    val bottom = hingeBounds.bottom.coerceIn(0, containerHeightPx)
    if (right <= left || bottom <= top) return null
    return IntRect(left = left, top = top, right = right, bottom = bottom)
}

/**
 * Final input-safety net for a physically occluding fold.
 *
 * Feature layouts should still place content on either side of the hinge. This transparent layer
 * only guarantees that a forgotten or transiently moving control cannot receive input through the
 * occluded bounds while posture or window size is changing.
 */
@Composable
internal fun HingeOcclusionInputShield(
    modifier: Modifier = Modifier,
    adaptiveInfo: AppWindowAdaptiveInfo = LocalAppWindowAdaptiveInfo.current,
) {
    if (!adaptiveInfo.foldingFeature.isOccluding || adaptiveInfo.foldingFeature.hingeBounds == null) {
        return
    }

    Layout(
        modifier = modifier,
        content = {
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { change ->
                                change.consume()
                            }
                        }
                    }
                },
            )
        },
    ) { measurables, constraints ->
        val containerWidth = constraints.maxWidth
        val containerHeight = constraints.maxHeight
        val exclusionBounds = resolveOccludingHingeInputBounds(
            adaptiveInfo = adaptiveInfo,
            containerWidthPx = containerWidth,
            containerHeightPx = containerHeight,
        )
        val shieldWidth = exclusionBounds?.width ?: 0
        val shieldHeight = exclusionBounds?.height ?: 0
        val shield = measurables.single().measure(
            Constraints.fixed(width = shieldWidth, height = shieldHeight),
        )

        layout(containerWidth, containerHeight) {
            exclusionBounds?.let { bounds ->
                shield.place(x = bounds.left, y = bounds.top)
            }
        }
    }
}
