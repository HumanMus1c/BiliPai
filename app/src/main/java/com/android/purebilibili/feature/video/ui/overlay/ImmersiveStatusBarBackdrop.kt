package com.android.purebilibili.feature.video.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.blur.BlurSurfaceType
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.ui.blur.unifiedBlur
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect

internal const val VIDEO_STATUS_BAR_AMBIENT_CAPTURE_INTERVAL_MS = 66L
internal const val VIDEO_STATUS_BAR_AMBIENT_SAMPLE_WIDTH_PX = 96
internal const val VIDEO_STATUS_BAR_AMBIENT_SAMPLE_HEIGHT_PX = 54

internal fun resolveVideoStatusBarAmbientHazeStyle(): HazeBlurStyle = HazeBlurStyle(
    backgroundColor = Color.Black,
    colorEffects = emptyList(),
    blurRadius = 24.dp,
    noiseFactor = 0f,
    fallbackColorEffect = HazeColorEffect.tint(Color.Black),
)

/**
 * Keeps the system status icons visible over an opaque, live ambient strip sampled from playback.
 * Black is retained as the first-frame and capture-failure fallback.
 */
@Composable
internal fun ImmersiveStatusBarBackdrop(
    ambientFrame: State<ImageBitmap?>?,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    if (height.value <= 0f) return
    val hazeState = rememberRecoverableHazeState()
    val currentAmbientFrame = ambientFrame?.value
    val colorFaithfulHazeStyle = remember { resolveVideoStatusBarAmbientHazeStyle() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.Black),
    ) {
        if (currentAmbientFrame != null) {
            Image(
                bitmap = currentAmbientFrame,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSourceCompat(hazeState),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .unifiedBlur(
                        hazeState = hazeState,
                        surfaceType = BlurSurfaceType.HEADER,
                        blurStyleOverride = colorFaithfulHazeStyle,
                    )
                    .background(Color.Black.copy(alpha = 0.34f)),
            )
        }
    }
}
