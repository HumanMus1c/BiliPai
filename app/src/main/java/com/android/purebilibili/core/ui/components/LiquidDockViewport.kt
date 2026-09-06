package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

/**
 * Frame the viewport, not the scrolling rail. Only trim the scrolling axis:
 * a capsule clip also cuts off the indicator's vertical press bloom.
 * Place before horizontalScroll (or on its parent) so the frame never scrolls.
 */
@Composable
internal fun Modifier.liquidDockViewport(): Modifier = this
    .drawWithContent {
        // horizontalScroll already allows cross-axis shadow overflow. Do not
        // reintroduce a shell-height clip outside it; leave room for bloom/lens.
        val overflow = 30.dp.toPx()
        clipRect(left = 0f, top = -overflow, right = size.width, bottom = size.height + overflow) {
            this@drawWithContent.drawContent()
        }
    }
    .border(
        width = 0.75.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
        shape = CircleShape,
    )
