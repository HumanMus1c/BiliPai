package com.android.purebilibili.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.Person
import kotlin.math.PI
import kotlin.math.sin

internal fun resolveMascotBounceWave(phase: Float): Float {
    val clamped = phase.coerceIn(0f, 1f)
    return sin((clamped * (2f * PI)).toFloat())
}

internal fun resolveMascotDotAlpha(phase: Float, index: Int): Float {
    val safeIndex = index.coerceIn(0, 2)
    val offsetPhase = phase.coerceIn(0f, 1f) - safeIndex * 0.17f
    val wave = (sin((offsetPhase * (2f * PI)).toFloat()) + 1f) / 2f
    return (0.18f + 0.82f * wave).coerceIn(0.18f, 1f)
}

@Composable
internal fun IosCutePersonLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
) {
    val transition = rememberInfiniteTransition(label = "cute-loading")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1120, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cute-loading-phase",
    )
    val density = LocalDensity.current
    val wave = resolveMascotBounceWave(phase)
    val iconSize = (strokeWidth.value * 9f).coerceIn(14f, 30f).dp
    val dotSize = (iconSize.value * 0.2f).coerceIn(2f, 5f).dp
    val translationY = with(density) { (-wave * 2.8f).dp.toPx() }

    Box(
        modifier = modifier.sizeIn(
            minWidth = iconSize + 8.dp,
            minHeight = iconSize + 8.dp,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(dotSize * 0.8f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .alpha(resolveMascotDotAlpha(phase = phase, index = index))
                        .background(color.copy(alpha = 0.92f), CircleShape),
                )
            }
        }

        Icon(
            imageVector = CupertinoIcons.Filled.Person,
            contentDescription = "加载中",
            tint = color,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    rotationZ = wave * 8f
                    this.translationY = translationY
                },
        )
    }
}
