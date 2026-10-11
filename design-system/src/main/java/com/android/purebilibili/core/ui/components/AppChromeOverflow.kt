package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

private class ChromeOverflowEntry(val layer: GraphicsLayer) {
    var coordinates by mutableStateOf<LayoutCoordinates?>(null)
}

private val LocalChromeOverflowEntries = staticCompositionLocalOf<MutableList<ChromeOverflowEntry>?> { null }

/**
 * Native app bars crop their slots to their fixed height. Replay just their control display lists
 * at the same coordinates in an unclipped sibling draw pass. Layout, input and semantics remain
 * in the native slots; titles and collapsing containers still use the native renderer.
 * This is a live display list, not a bitmap or a second composition of the controls.
 */
@Composable
internal fun AppChromeOverflowHost(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val entries = remember { mutableStateListOf<ChromeOverflowEntry>() }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates = it }
            .drawWithContent {
                drawContent()
                val host = coordinates
                if (host != null && host.isAttached) {
                    entries.forEach { entry ->
                        val source = entry.coordinates
                        if (source != null && source.isAttached && source.size.height > 0) {
                            val offset = host.localPositionOf(source)
                            // A fully collapsed/offscreen native slot must not leave a floating glyph.
                            val centerY = offset.y + source.size.height / 2f
                            if (centerY >= 0f && centerY <= size.height) {
                                translate(offset.x, offset.y) { drawLayer(entry.layer) }
                            }
                        }
                    }
                }
            },
    ) {
        CompositionLocalProvider(LocalChromeOverflowEntries provides entries, content = content)
    }
}

@Composable
internal fun AppChromeOverflowSlot(content: @Composable () -> Unit) {
    val entries = LocalChromeOverflowEntries.current
    if (entries == null) {
        content()
        return
    }
    val layer = rememberGraphicsLayer()
    val entry = remember(layer) { ChromeOverflowEntry(layer) }
    DisposableEffect(entries, entry) {
        entries.add(entry)
        onDispose { entries.remove(entry) }
    }
    Box(
        modifier = Modifier
            .onGloballyPositioned { entry.coordinates = it }
            .drawWithContent {
                // Do not draw the layer here: the native bar would crop it and duplicate the replay.
                layer.record { this@drawWithContent.drawContent() }
            },
    ) {
        content()
    }
}
