package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    if (
        tertiaryContent != null &&
        resolveAppAdaptiveSceneLayout(adaptiveInfo) == AppAdaptiveSceneLayout.ThreePane
    ) {
        AppAdaptiveSplitLayout(
            useSplitLayout = true,
            primaryContent = primaryContent,
            secondaryContent = {
                AppAdaptiveSplitLayout(
                    useSplitLayout = true,
                    primaryContent = secondaryContent,
                    secondaryContent = tertiaryContent,
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
        useSplitLayout = windowSizeClass.shouldUseSplitLayout || adaptiveInfo.shouldAvoidHinge,
        primaryContent = primaryContent,
        secondaryContent = secondaryContent,
        primaryRatio = hingeAwareRatio,
        splitAxis = splitAxis,
        dividerSize = dividerSize,
        modifier = modifier,
    )
}
