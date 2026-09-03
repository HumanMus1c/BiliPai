package com.android.purebilibili.core.ui.transition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.android.purebilibili.core.util.CardPositionManager

/**
 * Records the stationary list card into a graphics layer so a click can freeze native pixels
 * instead of reconstructing title/spacing on the flying detail entry.
 *
 * [GraphicsLayer.toImageBitmap] is suspend and cannot run from a click callback. Keep the
 * recorded layer and draw it with [androidx.compose.ui.graphics.layer.drawLayer].
 */
@Composable
internal fun Modifier.recordNativeVideoCardLayer(
    layer: GraphicsLayer,
    freeze: Boolean,
): Modifier = drawWithContent {
    if (!freeze) {
        layer.record {
            this@drawWithContent.drawContent()
        }
    }
    drawContent()
}

internal fun captureNativeVideoCardImage(
    layer: GraphicsLayer,
) {
    CardPositionManager.recordNativeCardLayer(layer)
}

internal fun captureNativeCoverOverlayLayer(
    layer: GraphicsLayer,
) {
    CardPositionManager.recordNativeCoverOverlayLayer(layer)
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
    return NativeVideoCardSnapshotController(
        modifier = Modifier.recordNativeVideoCardLayer(layer, freezeState.value),
        coverOverlayModifier = Modifier.recordNativeVideoCardLayer(
            coverOverlayLayer,
            freezeState.value,
        ),
        capture = {
            freezeState.value = true
            captureNativeVideoCardImage(layer)
            captureNativeCoverOverlayLayer(coverOverlayLayer)
        },
    )
}
