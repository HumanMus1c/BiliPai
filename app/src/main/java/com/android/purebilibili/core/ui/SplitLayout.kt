package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.android.purebilibili.core.ui.components.AppAdaptiveSplitLayout
import com.android.purebilibili.core.ui.components.AppSplitAxis
import com.android.purebilibili.core.util.AppFoldPosture
import com.android.purebilibili.core.util.AppHingeOrientation
import com.android.purebilibili.core.util.LocalAppWindowAdaptiveInfo
import com.android.purebilibili.core.util.LocalWindowSizeClass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

enum class AppAdaptiveSceneLayout {
    SinglePane,
    OptionalTwoPane,
    TwoPane,
    ThreePane,
    Book,
    Tabletop,
}

/**
 * Logical pane identity used by [AppSplitLayoutState].
 *
 * The identity is deliberately independent from the physical split axis, so a pane keeps the
 * same saved state while a device moves between single-pane, book and tabletop layouts.
 */
enum class AppSplitPane {
    Primary,
    Secondary,
    Tertiary,
}

/**
 * Small list/detail-style navigator for [AppSplitLayout].
 *
 * In a multi-pane layout every available pane is visible. In a single-pane layout
 * [currentPane] selects the visible destination and [navigateBack] returns through the pane
 * history. This mirrors the important ListDetailPaneScaffold behavior without coupling the app
 * shell to Material adaptive navigation: changing window size does not reset pane selection or
 * the saveable state owned by each pane.
 */
@Stable
class AppSplitLayoutState internal constructor(
    initialHistory: List<AppSplitPane>,
) {
    private var history by mutableStateOf(normalizeHistory(initialHistory))

    val currentPane: AppSplitPane
        get() = history.last()

    fun navigateTo(pane: AppSplitPane) {
        if (pane == currentPane) return
        history = history + pane
    }

    fun navigateBack(): Boolean {
        if (history.size <= 1) return false
        history = history.dropLast(1)
        return true
    }

    internal fun ensureAvailable(tertiaryAvailable: Boolean) {
        if (!tertiaryAvailable && AppSplitPane.Tertiary in history) {
            history = normalizeHistory(history.filterNot { it == AppSplitPane.Tertiary })
        }
    }

    internal fun snapshot(): List<String> = history.map(AppSplitPane::name)

    companion object {
        private fun normalizeHistory(history: List<AppSplitPane>): List<AppSplitPane> =
            history.ifEmpty { listOf(AppSplitPane.Primary) }

        val Saver: Saver<AppSplitLayoutState, List<String>> = Saver(
            save = { state -> state.snapshot() },
            restore = { names ->
                AppSplitLayoutState(
                    names.mapNotNull { name ->
                        runCatching { AppSplitPane.valueOf(name) }.getOrNull()
                    },
                )
            },
        )
    }
}

@Composable
fun rememberAppSplitLayoutState(
    initialPane: AppSplitPane = AppSplitPane.Primary,
): AppSplitLayoutState = rememberSaveable(saver = AppSplitLayoutState.Saver) {
    AppSplitLayoutState(listOf(initialPane))
}

fun resolveAppAdaptiveSceneLayout(
    adaptiveInfo: com.android.purebilibili.core.util.AppWindowAdaptiveInfo,
): AppAdaptiveSceneLayout {
    if (adaptiveInfo.windowSizeClass.heightSizeClass ==
        com.android.purebilibili.core.util.WindowHeightSizeClass.Compact
    ) {
        return AppAdaptiveSceneLayout.SinglePane
    }
    return when (adaptiveInfo.posture) {
        AppFoldPosture.Book -> AppAdaptiveSceneLayout.Book
        AppFoldPosture.Tabletop -> AppAdaptiveSceneLayout.Tabletop
        AppFoldPosture.None,
        AppFoldPosture.Flat -> when (adaptiveInfo.windowSizeClass.widthSizeClass) {
            com.android.purebilibili.core.util.WindowWidthSizeClass.Compact ->
                AppAdaptiveSceneLayout.SinglePane
            com.android.purebilibili.core.util.WindowWidthSizeClass.Medium ->
                AppAdaptiveSceneLayout.OptionalTwoPane
            com.android.purebilibili.core.util.WindowWidthSizeClass.Expanded,
            com.android.purebilibili.core.util.WindowWidthSizeClass.Large ->
                AppAdaptiveSceneLayout.TwoPane
            com.android.purebilibili.core.util.WindowWidthSizeClass.ExtraLarge ->
                AppAdaptiveSceneLayout.ThreePane
        }
    }
}

@Composable
fun AppSplitLayout(
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit,
    tertiaryContent: (@Composable () -> Unit)? = null,
    primaryRatio: Float = 0.65f,
    state: AppSplitLayoutState = rememberAppSplitLayoutState(),
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val adaptiveInfo = LocalAppWindowAdaptiveInfo.current
    val density = LocalDensity.current
    val foldingFeature = adaptiveInfo.foldingFeature
    val hingeBounds = foldingFeature.hingeBounds
    val splitAxis = when (foldingFeature.hingeOrientation) {
        AppHingeOrientation.Horizontal -> AppSplitAxis.Vertical
        AppHingeOrientation.Vertical,
        AppHingeOrientation.None -> AppSplitAxis.Horizontal
    }
    val hingeAwareRatio = if (adaptiveInfo.shouldAvoidHinge && hingeBounds != null) {
        with(density) {
            when (splitAxis) {
                AppSplitAxis.Horizontal ->
                    hingeBounds.left / windowSizeClass.widthDp.toPx()
                AppSplitAxis.Vertical ->
                    hingeBounds.top / windowSizeClass.heightDp.toPx()
            }
        }.coerceIn(0.2f, 0.8f)
    } else {
        primaryRatio
    }
    val dividerSize = if (adaptiveInfo.shouldAvoidHinge && hingeBounds != null) {
        with(density) {
            when (splitAxis) {
                AppSplitAxis.Horizontal -> hingeBounds.width.toDp()
                AppSplitAxis.Vertical -> hingeBounds.height.toDp()
            }
        }.coerceAtLeast(1.dp)
    } else {
        1.dp
    }
    LaunchedEffect(state, tertiaryContent != null) {
        state.ensureAvailable(tertiaryAvailable = tertiaryContent != null)
    }
    val paneStateHolder = rememberSaveableStateHolder()
    val savedPrimaryContent: @Composable () -> Unit = {
        paneStateHolder.SaveableStateProvider(AppSplitPane.Primary.name, primaryContent)
    }
    val savedSecondaryContent: @Composable () -> Unit = {
        paneStateHolder.SaveableStateProvider(AppSplitPane.Secondary.name, secondaryContent)
    }
    val savedTertiaryContent: (@Composable () -> Unit)? = tertiaryContent?.let { content ->
        {
            paneStateHolder.SaveableStateProvider(AppSplitPane.Tertiary.name, content)
        }
    }
    val sceneLayout = resolveAppAdaptiveSceneLayout(adaptiveInfo)
    val useSplitLayout = windowSizeClass.shouldUseSplitLayout || adaptiveInfo.shouldAvoidHinge
    if (!useSplitLayout || sceneLayout == AppAdaptiveSceneLayout.SinglePane) {
        androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
            when (state.currentPane) {
                AppSplitPane.Primary -> savedPrimaryContent()
                AppSplitPane.Secondary -> savedSecondaryContent()
                AppSplitPane.Tertiary -> savedTertiaryContent?.invoke() ?: savedSecondaryContent()
            }
        }
        return
    }
    if (
        savedTertiaryContent != null &&
        sceneLayout == AppAdaptiveSceneLayout.ThreePane
    ) {
        AppAdaptiveSplitLayout(
            useSplitLayout = true,
            primaryContent = savedPrimaryContent,
            secondaryContent = {
                AppAdaptiveSplitLayout(
                    useSplitLayout = true,
                    primaryContent = savedSecondaryContent,
                    secondaryContent = savedTertiaryContent,
                    primaryRatio = 0.5f,
                    modifier = Modifier,
                )
            },
            primaryRatio = primaryRatio.coerceAtMost(0.4f),
            modifier = modifier,
        )
        return
    }
    AppAdaptiveSplitLayout(
        useSplitLayout = true,
        primaryContent = savedPrimaryContent,
        secondaryContent = savedSecondaryContent,
        primaryRatio = hingeAwareRatio,
        splitAxis = splitAxis,
        dividerSize = dividerSize,
        modifier = modifier,
    )
}
