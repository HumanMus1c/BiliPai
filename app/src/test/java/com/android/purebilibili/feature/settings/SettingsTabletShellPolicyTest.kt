package com.android.purebilibili.feature.settings

import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppAdaptiveSceneLayout
import com.android.purebilibili.core.ui.AppSplitLayoutState
import com.android.purebilibili.core.ui.AppSplitPane
import com.android.purebilibili.core.ui.resolveAppAdaptiveSceneLayout
import com.android.purebilibili.core.util.AppWindowAdaptiveInfo
import com.android.purebilibili.core.util.WindowHeightSizeClass
import com.android.purebilibili.core.util.WindowSizeClass
import com.android.purebilibili.core.util.WindowWidthSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsTabletShellPolicyTest {
    @Test
    fun mediumAndExpandedWindowsResolveExpectedPaneCounts() {
        assertEquals(
            AppAdaptiveSceneLayout.OptionalTwoPane,
            resolveAppAdaptiveSceneLayout(adaptiveInfo(WindowWidthSizeClass.Medium)),
        )
        assertEquals(
            AppAdaptiveSceneLayout.TwoPane,
            resolveAppAdaptiveSceneLayout(adaptiveInfo(WindowWidthSizeClass.Expanded)),
        )
        assertEquals(
            AppAdaptiveSceneLayout.ThreePane,
            resolveAppAdaptiveSceneLayout(adaptiveInfo(WindowWidthSizeClass.ExtraLarge)),
        )
    }

    @Test
    fun paneNavigatorKeepsDetailSelectionAndPredictableBackHistory() {
        val state = AppSplitLayoutState(listOf(AppSplitPane.Primary))

        state.navigateTo(AppSplitPane.Secondary)
        assertEquals(AppSplitPane.Secondary, state.currentPane)
        assertTrue(state.navigateBack())
        assertEquals(AppSplitPane.Primary, state.currentPane)
    }

    private fun adaptiveInfo(widthClass: WindowWidthSizeClass) = AppWindowAdaptiveInfo(
        windowSizeClass = WindowSizeClass(
            widthSizeClass = widthClass,
            heightSizeClass = WindowHeightSizeClass.Medium,
            widthDp = 1000.dp,
            heightDp = 700.dp,
        ),
    )
}
