package com.android.purebilibili.core.ui

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
import com.android.purebilibili.feature.home.components.BiliPaiImmersiveTopBar
import com.android.purebilibili.feature.home.components.shouldUseBiliPaiProgressiveTopBlur
import com.android.purebilibili.core.ui.blur.rememberChromeBackdropSource

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
    val progressive = shouldUseBiliPaiProgressiveTopBlur(
        enabled = config.progressiveTopBlurEnabled && !config.headerBlurEnabled && topBar != null,
        hasBackdrop = true,
    ) && !isLowBlurBudgetForced()
    val source = if (progressive && blurContentReady) rememberChromeBackdropSource() else null
    val backdrop = source?.takeIf { it.isReady }?.backdrop
    val blurActive = progressive && backdrop != null
    AppScaffold(
        modifier = modifier,
        topBar = {
            if (topBar != null) {
                BiliPaiImmersiveTopBar(
                    backdrop = backdrop,
                    enabled = blurActive,
                    modifier = Modifier.background(
                        if (blurActive) Color.Transparent else globalWallpaperAwareChromeColor(containerColor)
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
                .globalWallpaperAwareBackground(containerColor),
        ) {
            content(padding)
        }
    }
}
