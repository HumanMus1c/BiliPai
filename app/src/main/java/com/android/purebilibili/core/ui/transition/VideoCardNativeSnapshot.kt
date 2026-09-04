package com.android.purebilibili.core.ui.transition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.android.purebilibili.core.util.CardPositionManager

internal const val VIDEO_CARD_FLYING_OVERLAY_COVER_DEPTH = 0.001f

/**
 * Click pre-arms OPENING before NavDisplay mounts the destination. Hide the list slot
 * only once that overlay is actually covering the source bounds, otherwise the click
 * flashes an empty card-shaped hole.
 */
internal fun isVideoCardFlyingOverlayCoveringSource(
    phase: VideoCardTransitionBackgroundPhase,
    depthProgress: Float,
    isReturnGestureInProgress: Boolean,
): Boolean {
    if (isReturnGestureInProgress) return true
    return when (phase) {
        VideoCardTransitionBackgroundPhase.OPENING,
        VideoCardTransitionBackgroundPhase.RETURNING,
        -> depthProgress > VIDEO_CARD_FLYING_OVERLAY_COVER_DEPTH
        VideoCardTransitionBackgroundPhase.HELD -> true
        VideoCardTransitionBackgroundPhase.IDLE -> false
    }
}

/**
 * The flying overlay owns the clicked card. The list slot must be empty while that
 * overlay covers the source, otherwise a frozen duplicate sits under the morph.
 */
internal fun shouldHideStationarySourceCard(
    isSharedMorphSourceCard: Boolean,
    phase: VideoCardTransitionBackgroundPhase,
    depthProgress: Float,
    isReturnGestureInProgress: Boolean,
): Boolean {
    if (!isSharedMorphSourceCard) return false
    return isVideoCardFlyingOverlayCoveringSource(
        phase = phase,
        depthProgress = depthProgress,
        isReturnGestureInProgress = isReturnGestureInProgress,
    )
}

internal fun isRecordedNativeCardSource(bvid: String): Boolean {
    val clicked = CardPositionManager.lastClickedVideoSourceKey ?: return false
    val id = bvid.trim()
    if (id.isEmpty()) return false
    return clicked == id || clicked.endsWith(":$id")
}

internal fun isNativeVideoCardLayerDrawable(widthPx: Int, heightPx: Int): Boolean =
    widthPx > 1 && heightPx > 1

/**
 * Records the stationary list card into a graphics layer so a click can freeze native pixels
 * instead of reconstructing title/spacing on the flying detail entry.
 *
 * [GraphicsLayer.toImageBitmap] is suspend and cannot run from a click callback. Keep the
 * recorded layer and draw it with [androidx.compose.ui.graphics.layer.drawLayer].
 * While the flying overlay covers this card, skip drawing at the list coordinates.
 */
@Composable
internal fun Modifier.recordNativeVideoCardLayer(
    layer: GraphicsLayer,
    freezeProvider: () -> Boolean,
    bvid: String = "",
): Modifier {
    val bgState = LocalVideoCardTransitionBackgroundState.current
    return drawWithContent {
        // Read the latch in draw. Waiting for recomposition after a click can otherwise
        // overwrite the frozen first-click card after OPENING has hidden its info band.
        if (!freezeProvider()) {
            layer.record {
                this@drawWithContent.drawContent()
            }
        }
        val hide = shouldHideStationarySourceCard(
            isSharedMorphSourceCard = isRecordedNativeCardSource(bvid),
            phase = bgState.phaseProvider(),
            depthProgress = bgState.progressProvider(),
            isReturnGestureInProgress = bgState.isReturnGestureInProgressProvider() ||
                bgState.isGestureRestoreInProgressProvider(),
        )
        if (!hide) {
            drawContent()
        }
    }
}

internal fun captureNativeVideoCardImage(
    layer: GraphicsLayer,
) {
    CardPositionManager.recordNativeCardLayer(
        layer.takeIf {
            isNativeVideoCardLayerDrawable(it.size.width, it.size.height)
        },
    )
}

internal fun captureNativeCoverOverlayLayer(
    layer: GraphicsLayer,
) {
    CardPositionManager.recordNativeCoverOverlayLayer(
        layer.takeIf {
            isNativeVideoCardLayerDrawable(it.size.width, it.size.height)
        },
    )
}

@Composable
internal fun rememberNativeVideoCardLayer() = rememberGraphicsLayer()

internal class NativeVideoCardSnapshotController(
    val modifier: Modifier,
    val coverOverlayModifier: Modifier,
    val capture: () -> Unit,
)

@Composable
internal fun rememberNativeVideoCardSnapshotController(key: Any): NativeVideoCardSnapshotController {
    val layer = rememberNativeVideoCardLayer()
    val coverOverlayLayer = rememberNativeVideoCardLayer()
    val freezeState = remember(key) { mutableStateOf(false) }
    val bvid = (key as? String).orEmpty()
    return NativeVideoCardSnapshotController(
        modifier = Modifier.recordNativeVideoCardLayer(
            layer = layer,
            freezeProvider = { freezeState.value },
            bvid = bvid,
        ),
        coverOverlayModifier = Modifier.recordNativeVideoCardLayer(
            layer = coverOverlayLayer,
            freezeProvider = { freezeState.value },
            bvid = bvid,
        ),
        capture = {
            freezeState.value = true
            captureNativeVideoCardImage(layer)
            captureNativeCoverOverlayLayer(coverOverlayLayer)
        },
    )
}
