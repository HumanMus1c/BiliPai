package com.android.purebilibili.feature.home.components

import java.io.File
import com.android.purebilibili.core.theme.UiPreset
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BottomBarLiquidSegmentedControlStructureTest {

    @Test
    fun `liquid segmented labels keep bottom bar foreground opacity`() {
        val onSurface = Color(0xFFF1F1F1)

        assertEquals(
            onSurface,
            resolveLiquidSegmentedControlUnselectedTextColor(
                onSurface = onSurface,
                enabled = true
            )
        )
        assertEquals(
            onSurface.copy(alpha = 0.42f),
            resolveLiquidSegmentedControlUnselectedTextColor(
                onSurface = onSurface,
                enabled = false
            )
        )
    }

    @Test
    fun `segmented labels reuse bottom bar glass content colors while moving`() {
        val unselected = Color(0xFF666666)
        val selected = Color(0xFFFF6699)

        val colors = resolveLiquidGlassSelectionContentColors(
            unselectedColor = unselected,
            selectedColor = selected,
            themeWeight = 1f,
            glassEnabled = true,
            indicatorProgress = 0.8f,
            indicatorBackdropEnabled = true
        )

        assertEquals(unselected, colors.visibleColor)
        assertEquals(unselected, colors.exportColor)
    }

    @Test
    fun `segmented indicator keeps slot width so content remains centered`() {
        val width = resolveSegmentedControlIndicatorWidthDp(
            slotWidthDp = 60f,
            indicatorHeightDp = 56f,
            itemCount = 5
        )

        assertEquals(60f, width)
    }

    @Test
    fun `segmented capture expands past full drag scale lens and panel offset`() {
        assertEquals(
            92f,
            resolveBottomBarCaptureSafeInsetDp(
                indicatorWidthDp = 224f,
                refractionHeightDp = 24f,
                refractionAmountDp = 24f,
                panelOffsetDp = 4f
            ),
            0.001f
        )
        assertEquals(
            24f,
            resolveBottomBarCaptureSafeInsetDp(
                indicatorWidthDp = 0f,
                refractionHeightDp = 24f,
                refractionAmountDp = 24f,
                panelOffsetDp = 0f
            ),
            0.001f
        )
    }

    @Test
    fun `segmented indicator reduces height for cramped slots to stay capsule shaped`() {
        assertEquals(
            37.5f,
            resolveSegmentedControlIndicatorHeightDp(
                slotWidthDp = 60f,
                indicatorHeightDp = 56f,
            )
        )
    }

    @Test
    fun `segmented indicator keeps full height for already wide home slots`() {
        assertEquals(
            56f,
            resolveSegmentedControlIndicatorHeightDp(
                slotWidthDp = 128f,
                indicatorHeightDp = 56f,
            )
        )
    }

    @Test
    fun `segmented indicator offset follows slot position without clamping dead zone`() {
        assertEquals(
            4f,
            resolveSegmentedControlIndicatorOffsetDp(
                position = 0f,
                slotWidthDp = 60f,
                contentPaddingDp = 4f,
            )
        )
        assertEquals(
            34f,
            resolveSegmentedControlIndicatorOffsetDp(
                position = 0.5f,
                slotWidthDp = 60f,
                contentPaddingDp = 4f,
            )
        )
        assertEquals(
            244f,
            resolveSegmentedControlIndicatorOffsetDp(
                position = 4f,
                slotWidthDp = 60f,
                contentPaddingDp = 4f,
            )
        )
    }

    @Test
    fun `segmented control only follows continuous drag when touch starts on indicator`() {
        assertTrue(
            shouldFollowSegmentedControlIndicatorDrag(
                pointerX = 132f,
                indicatorPosition = 2f,
                itemWidthPx = 64f
            )
        )
        assertFalse(
            shouldFollowSegmentedControlIndicatorDrag(
                pointerX = 80f,
                indicatorPosition = 2f,
                itemWidthPx = 64f
            )
        )
        assertFalse(
            shouldFollowSegmentedControlIndicatorDrag(
                pointerX = 196.1f,
                indicatorPosition = 2f,
                itemWidthPx = 64f
            )
        )
    }

    @Test
    fun `segmented control sweep release resolves label without requiring indicator follow`() {
        assertEquals(
            0,
            resolveSegmentedControlSweepSelectionIndex(
                pointerX = -12f,
                itemWidthPx = 64f,
                itemCount = 4
            )
        )
        assertEquals(
            1,
            resolveSegmentedControlSweepSelectionIndex(
                pointerX = 82f,
                itemWidthPx = 64f,
                itemCount = 4
            )
        )
        assertEquals(
            3,
            resolveSegmentedControlSweepSelectionIndex(
                pointerX = 260f,
                itemWidthPx = 64f,
                itemCount = 4
            )
        )
    }

    @Test
    fun `segmented indicator can follow external realtime page position`() {
        assertEquals(
            1.35f,
            resolveSegmentedControlIndicatorPosition(
                internalPosition = 1f,
                externalPosition = 1.35f,
                itemCount = 4
            )
        )
        assertEquals(
            0f,
            resolveSegmentedControlIndicatorPosition(
                internalPosition = 1f,
                externalPosition = -0.2f,
                itemCount = 4
            )
        )
        assertEquals(
            3f,
            resolveSegmentedControlIndicatorPosition(
                internalPosition = 1f,
                externalPosition = 4.2f,
                itemCount = 4
            )
        )
    }

    @Test
    fun `segmented indicator only samples hidden tab backdrop while sliding without external backdrop`() {
        assertFalse(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0f,
                hasExternalBackdrop = false
            )
        )
        assertTrue(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0.01f,
                hasExternalBackdrop = false
            )
        )
        assertTrue(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0f,
                hasExternalBackdrop = true
            )
        )
        assertFalse(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = false,
                motionProgress = 1f,
                hasExternalBackdrop = true
            )
        )
    }

    @Test
    fun `export capture backdrop requires an external page layer`() {
        assertTrue(
            shouldDrawSegmentedControlExportCaptureBackdrop(
                liquidGlassEnabled = true,
                hasExternalBackdrop = true
            )
        )
        assertFalse(
            shouldDrawSegmentedControlExportCaptureBackdrop(
                liquidGlassEnabled = true,
                hasExternalBackdrop = false
            )
        )
        assertFalse(
            shouldDrawSegmentedControlExportCaptureBackdrop(
                liquidGlassEnabled = false,
                hasExternalBackdrop = true
            )
        )
    }

    @Test
    fun `global glass overrides inline segmented control preference`() {
        assertEquals(
            SegmentedControlChromeStyle.LIQUID_PILL,
            resolveSegmentedControlChromeStyle(
                prefersNativeChrome = true,
                androidNativeLiquidGlassEnabled = true,
                preferInlineContentStyle = true
            )
        )
    }

    @Test
    fun `android native chrome segmented control keeps liquid pill when global glass is enabled`() {
        assertEquals(
            SegmentedControlChromeStyle.LIQUID_PILL,
            resolveSegmentedControlChromeStyle(
                prefersNativeChrome = true,
                androidNativeLiquidGlassEnabled = true,
                preferInlineContentStyle = false
            )
        )
    }

    @Test
    fun `global segmented control delegates liquid chrome to bottom bar matched implementation`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt"
        )
        val sharedChrome = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarMatchedLiquidChrome.kt"
        )

        assertTrue(source.contains("BottomBarMotionProfile.ANDROID_NATIVE_FLOATING"))
        assertFalse(source.contains("BottomBarMotionProfile.IOS_FLOATING"))
        assertTrue(source.contains("rememberBottomBarMatchedLiquidChromeState("))
        assertTrue(source.contains("BottomBarMatchedLiquidDock("))
        assertTrue(source.contains("BottomBarMatchedLiquidIndicator("))
        assertTrue(source.contains("drawShellLens = false"))
        assertTrue(source.contains("resolveSharedLiquidIndicatorPanelOffsetPx("))
        assertTrue(source.contains("horizontalDragGesture("))
        assertTrue(source.contains("BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP = 58"))
        assertTrue(source.contains("BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP = 56"))
        assertTrue(source.contains("val localPageMiuixBackdrop = rememberMiuixLayerBackdrop()"))
        assertTrue(source.contains(".miuixLayerBackdrop(localPageMiuixBackdrop)"))
        assertTrue(source.contains("bottomBarMatchedCaptureOverflow(captureSafeInset)"))
        assertTrue(source.contains(".background(AppSurfaceTokens.background())"))
        val hiddenExport = source
            .substringAfter("// 2) Hidden export capture")
            .substringBefore("// 3) Capsule on top")
        assertFalse(hiddenExport.contains("bottomBarMatchedCaptureOverflow("))
        assertTrue(source.contains("rememberMiuixCombinedBackdrop("))
        assertTrue(source.contains("shouldDrawSegmentedControlExportCaptureBackdrop("))
        assertTrue(source.contains("resolveBottomBarBackdropPresetCaptureLens("))
        assertTrue(source.contains("resolveBottomBarBackdropPresetIndicatorLens("))
        assertTrue(source.contains("forceUnselectedColor = useGlassColorPath"))
        assertTrue(source.contains("ColorFilter.tint(exportTintColor)"))
        assertFalse(source.contains("val indicatorScale = lerp(1f, 78f / 56f, motionProgress)"))
        assertFalse(source.contains("velocity = dragState.velocity / 10f"))
        assertTrue(source.contains("liquidGlassEffectsEnabled: Boolean = true"))
        assertTrue(source.contains("dragSelectionEnabled: Boolean = true"))
        assertTrue(source.contains("getHomeSettings("))
        assertTrue(source.contains("visualPolicy.supportsIndependentLiquidGlass"))
        assertTrue(source.contains("resolveSegmentedControlChromeStyle("))
        assertTrue(source.contains("AndroidNativeUnderlinedSegmentedControl("))
        assertTrue(source.contains("SegmentedControlChromeStyle.ANDROID_NATIVE_UNDERLINE"))
        assertTrue(source.contains("onIndicatorPositionChanged?.invoke(indicatorPosition)"))
        assertTrue(source.contains("indicatorPositionProvider: (() -> Float)? = null"))
        assertTrue(source.contains("resolveSegmentedControlIndicatorPosition("))
        assertTrue(source.contains("externalPosition = if (dragState.isDragging) null else indicatorPositionProvider?.invoke()"))
        assertTrue(source.contains("val underlineOffsetX = (segmentWidth * indicatorPosition) + ((segmentWidth - underlineWidth) / 2)"))
        assertTrue(source.contains("if (enabled && itemCount > 1 && dragSelectionEnabled)"))
        assertTrue(source.contains("onPressChanged = dragState::setPressed"))
        assertTrue(source.contains("resolveSegmentedControlIndicatorWidthDp("))
        assertTrue(source.contains("resolveSegmentedControlIndicatorHeightDp("))
        assertTrue(source.contains("resolveSegmentedControlIndicatorOffsetDp("))
        assertTrue(source.contains("val indicatorShape = resolveSharedBottomBarCapsuleShape()"))
        assertTrue(source.contains("val containerShape = indicatorShape"))
        assertTrue(source.contains("indicatorWidth = indicatorWidth"))
        assertTrue(source.contains("indicatorHeight = resolvedIndicatorHeight"))

        assertTrue(sharedChrome.contains("holdPressUntilReleaseTargetSettles = true"))
        assertTrue(sharedChrome.contains("resolveBottomBarMaterialScrollAnimationDurationMillis(isScrolling)"))
        assertTrue(sharedChrome.contains("KernelSuMiuixBottomBarIndicatorLayer("))
        assertTrue(sharedChrome.contains("BottomBarLiquidOrientation.VERTICAL"))
        assertTrue(sharedChrome.contains("swapMotionAxes = orientation == BottomBarLiquidOrientation.VERTICAL"))
        assertTrue(source.contains("contentBackdrop = combinedMiuixBackdrop"))
        assertTrue(source.contains("legacyContentBackdrop = tabsBackdrop"))
    }

    @Test
    fun `dynamic top tabs reuse shared liquid chrome with pager and scroll state`() {
        val dynamicScreen = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt"
        )
        val dynamicTopBar = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicTopBar.kt"
        )

        assertTrue(dynamicTopBar.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(dynamicTopBar.contains("indicatorPositionProvider = indicatorPositionProvider"))
        assertTrue(dynamicTopBar.contains("isScrollInProgressProvider = isScrollInProgressProvider"))
        assertFalse(dynamicTopBar.contains("DynamicCompactTabRow("))
        assertTrue(dynamicScreen.contains("BottomBarMatchedDockVisibility("))
        assertTrue(dynamicScreen.contains("edge = BottomBarMatchedDockEdge.TOP"))
        assertTrue(dynamicScreen.contains("activeListState?.isScrollInProgress == true"))
        assertTrue(dynamicScreen.contains("pagerState.isScrollInProgress"))
    }

    @Test
    fun `common list and video tabs pass page backdrop into segmented control`() {
        val commonList = loadSource("app/src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        val iosSegmented = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/AppSegmentedComponents.kt")

        val videoContent = loadSource("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt")
        val commentSortBar = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/CommentSortFilterBar.kt"
        )
        val commentSheetHost = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/VideoCommentSheetHost.kt"
        )

        assertTrue(commonList.contains("val commonListChromeBackdrop = rememberLayerBackdrop()"))
        assertTrue(commonList.contains(".layerBackdrop(commonListChromeBackdrop)"))
        assertTrue(commonList.contains("backdrop = commonListChromeBackdrop"))
        assertTrue(videoContent.contains("val videoContentChromeBackdrop = rememberLayerBackdrop()"))
        assertTrue(videoContent.contains("chromeBackdrop = videoContentChromeBackdrop"))
        assertTrue(videoContent.contains("backdrop = videoContentChromeBackdrop"))
        assertTrue(videoContent.contains("Column(modifier = modifier.fillMaxSize())"))
        assertTrue(commentSortBar.contains("backdrop = backdrop"))
        assertTrue(commentSheetHost.contains("val commentChromeBackdrop = rememberLayerBackdrop()"))
        assertTrue(commentSheetHost.contains(".layerBackdrop(commentChromeBackdrop)"))
        assertTrue(iosSegmented.contains("backdrop: Backdrop? = null"))
        assertTrue(iosSegmented.contains("backdrop = backdrop"))
    }

    @Test
    fun `segmented control does not attach drag gesture when drag selection is disabled`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt"
        )

        assertTrue(
            source.contains("if (enabled && itemCount > 1 && dragSelectionEnabled)"),
            "Scrollable contribution tabs disable drag selection, so the liquid indicator must not attach a competing horizontal drag gesture"
        )
    }

    @Test
    fun `global video dynamic and live segmented surfaces share android native fallback`() {
        val paths = listOf(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/CommentSortFilterBar.kt",
            "app/src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt",
            "app/src/main/java/com/android/purebilibili/feature/live/LiveListScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/live/LiveAreaScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/live/LivePlayerScreen.kt"
        )

        paths.forEach { path ->
            assertTrue(
                loadSource(path).contains("BottomBarLiquidSegmentedControl("),
                "$path should keep using BottomBarLiquidSegmentedControl so the global Android native fallback applies"
            )
        }
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
