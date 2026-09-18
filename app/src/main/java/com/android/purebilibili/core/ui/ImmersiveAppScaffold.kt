package com.android.purebilibili.core.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.performance.isLowBlurBudgetForced
import com.android.purebilibili.core.ui.blur.BlurSurfaceType
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import com.android.purebilibili.core.ui.blur.recoverableBlurEnabled
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.core.ui.blur.shouldAllowRenderEffectBackedHazeEffect
import com.android.purebilibili.feature.home.components.BiliPaiImmersiveTopBar
import com.android.purebilibili.feature.home.components.shouldUseBiliPaiProgressiveTopBlur
import com.android.purebilibili.core.ui.blur.rememberChromeBackdropSource
import com.android.purebilibili.core.ui.resolveTopChromeRenderMode
import com.android.purebilibili.core.ui.TopChromeRenderMode

/** List pages keep their viewport full height and apply scaffold insets as scroll content padding. */
@Composable
internal fun ImmersiveAppScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentWindowInsets: WindowInsets = WindowInsets.navigationBars,
    // Keep false until any outgoing skeleton transition has left composition.
    blurContentReady: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    val config = LocalAppThemeConfig.current
    val lowBlurBudget = isLowBlurBudgetForced()
    val headerRequested = config.headerBlurEnabled && topBar != null
    val progressiveRequested = config.progressiveTopBlurEnabled && !headerRequested && topBar != null
    val hazeState = if (
        headerRequested &&
        !lowBlurBudget &&
        shouldAllowRenderEffectBackedHazeEffect(Build.VERSION.SDK_INT)
    ) {
        rememberRecoverableHazeState(initialBlurEnabled = true)
    } else {
        null
    }
    val hazeReady = hazeState != null && blurContentReady && recoverableBlurEnabled(hazeState)
    val progressive = shouldUseBiliPaiProgressiveTopBlur(
        enabled = progressiveRequested,
        hasBackdrop = true,
    ) && !lowBlurBudget
    // Keep recording while skeleton/loading content is shown. When the real content becomes
    // eligible, the already-warm backdrop can be published in the same composition instead of
    // making chrome briefly fall back while a new source records its first frame.
    val source = if (progressive) rememberChromeBackdropSource() else null
    val backdrop = source?.takeIf { blurContentReady && it.isReady }?.backdrop
    val renderMode = resolveTopChromeRenderMode(
        headerBlurRequested = headerRequested,
        progressiveBlurRequested = progressiveRequested,
        hazeAvailable = hazeReady,
        progressiveAvailable = backdrop != null,
    )
    val hazeActive = renderMode == TopChromeRenderMode.HAZE
    val progressiveActive = renderMode == TopChromeRenderMode.PROGRESSIVE
    val blurActive = hazeActive || progressiveActive
    AppScaffold(
        modifier = modifier,
        topBar = {
            if (topBar != null) {
                BiliPaiImmersiveTopBar(
                    backdrop = backdrop.takeIf { progressiveActive },
                    enabled = progressiveActive,
                    headerBlurActive = hazeActive,
                    extendBelowBounds = false,
                    modifier = Modifier.then(
                        if (progressiveActive) {
                            Modifier.background(Color.Transparent)
                        } else if (hazeActive && hazeState != null) {
                            Modifier
                                .unifiedBlur(
                                    hazeState = hazeState,
                                    surfaceType = BlurSurfaceType.HEADER,
                                )
                                .background(
                                    globalWallpaperAwareChromeColor(containerColor)
                                        .copy(alpha = AppSurfaceTokens.FrostedScrimAlpha)
                                )
                        } else {
                            Modifier.background(globalWallpaperAwareChromeColor(containerColor))
                        }
                    ),
                    content = topBar,
                )
            }
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentWindowInsets = contentWindowInsets,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(source?.modifier ?: Modifier)
                .then(if (hazeState != null) Modifier.hazeSourceCompat(hazeState) else Modifier)
                .globalWallpaperAwareBackground(containerColor),
        ) {
            content(padding)
        }
    }
}
