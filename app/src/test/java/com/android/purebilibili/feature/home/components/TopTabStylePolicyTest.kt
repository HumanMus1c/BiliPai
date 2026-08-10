package com.android.purebilibili.feature.home.components

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.theme.resolveUiStyle
import com.android.purebilibili.core.ui.AppTopTabPresentation
import com.android.purebilibili.core.ui.resolveAppTopChromePolicy
import java.io.File
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopTabStylePolicyTest {

    @Test
    fun `floating plus liquid uses liquid glass`() {
        val state = resolveTopTabStyle(
            isBottomBarFloating = true,
            isBottomBarBlurEnabled = true,
            isLiquidGlassEnabled = true
        )

        assertEquals(true, state.floating)
        assertEquals(TopTabMaterialMode.LIQUID_GLASS, state.materialMode)
    }

    @Test
    fun `floating without liquid but blur enabled uses blur`() {
        val state = resolveTopTabStyle(
            isBottomBarFloating = true,
            isBottomBarBlurEnabled = true,
            isLiquidGlassEnabled = false
        )

        assertEquals(true, state.floating)
        assertEquals(TopTabMaterialMode.BLUR, state.materialMode)
    }

    @Test
    fun `floating without blur and liquid uses plain`() {
        val state = resolveTopTabStyle(
            isBottomBarFloating = true,
            isBottomBarBlurEnabled = false,
            isLiquidGlassEnabled = false
        )

        assertEquals(true, state.floating)
        assertEquals(TopTabMaterialMode.PLAIN, state.materialMode)
    }

    @Test
    fun `docked with blur uses blur`() {
        val state = resolveTopTabStyle(
            isBottomBarFloating = false,
            isBottomBarBlurEnabled = true,
            isLiquidGlassEnabled = false
        )

        assertEquals(false, state.floating)
        assertEquals(TopTabMaterialMode.BLUR, state.materialMode)
    }

    @Test
    fun `docked with liquid uses liquid glass when blur enabled`() {
        val state = resolveTopTabStyle(
            isBottomBarFloating = false,
            isBottomBarBlurEnabled = true,
            isLiquidGlassEnabled = true
        )

        assertEquals(false, state.floating)
        assertEquals(TopTabMaterialMode.LIQUID_GLASS, state.materialMode)
    }

    @Test
    fun `docked with liquid uses liquid glass without blur`() {
        val state = resolveTopTabStyle(
            isBottomBarFloating = false,
            isBottomBarBlurEnabled = false,
            isLiquidGlassEnabled = true
        )

        assertEquals(false, state.floating)
        assertEquals(TopTabMaterialMode.LIQUID_GLASS, state.materialMode)
    }

    @Test
    fun `reduced interaction budget keeps liquid glass home header tab material mode`() {
        assertEquals(
            TopTabMaterialMode.LIQUID_GLASS,
            resolveEffectiveHomeHeaderTabMaterialMode(
                materialMode = TopTabMaterialMode.LIQUID_GLASS,
                interactionBudget = HomeInteractionMotionBudget.REDUCED
            )
        )
        assertEquals(
            TopTabMaterialMode.BLUR,
            resolveEffectiveHomeHeaderTabMaterialMode(
                materialMode = TopTabMaterialMode.BLUR,
                interactionBudget = HomeInteractionMotionBudget.REDUCED
            )
        )
    }

    @Test
    fun `top tab liquid glass follows requested state regardless of interaction budget`() {
        assertTrue(
            resolveEffectiveTopTabLiquidGlassEnabled(
                isLiquidGlassEnabled = true,
                interactionBudget = HomeInteractionMotionBudget.FULL
            )
        )
        assertTrue(
            resolveEffectiveTopTabLiquidGlassEnabled(
                isLiquidGlassEnabled = true,
                interactionBudget = HomeInteractionMotionBudget.REDUCED
            )
        )
        assertFalse(
            resolveEffectiveTopTabLiquidGlassEnabled(
                isLiquidGlassEnabled = false,
                interactionBudget = HomeInteractionMotionBudget.FULL
            )
        )
    }

    @Test
    fun `home top tab material mode only keeps blur or plain`() {
        assertEquals(TopTabMaterialMode.BLUR, resolveHomeTopTabMaterialMode(headerBlurEnabled = true))
        assertEquals(TopTabMaterialMode.PLAIN, resolveHomeTopTabMaterialMode(headerBlurEnabled = false))
    }

    @Test
    fun `home top tab presentation routes by preset and native variant`() {
        // 2B 迁移：iOS 输入经迁移表并入 MIUIX，落到 TONAL_CAPSULE→MATERIAL_UNDERLINE 首页呈现。
        assertEquals(
            AppTopTabPresentation.MATERIAL_UNDERLINE,
            topStyle(UiPreset.IOS, AndroidNativeVariant.MATERIAL3).presentation
        )
        assertEquals(
            AppTopTabPresentation.MATERIAL_UNDERLINE,
            topStyle(UiPreset.MD3, AndroidNativeVariant.MATERIAL3).presentation
        )
        assertEquals(
            AppTopTabPresentation.MATERIAL_UNDERLINE,
            topStyle(UiPreset.MD3, AndroidNativeVariant.MIUIX).presentation
        )
        assertEquals(
            AppTopTabPresentation.MATERIAL_UNDERLINE,
            topStyle(UiPreset.MD3, AndroidNativeVariant.MIUIX, labelMode = 0).presentation
        )
    }

    @Test
    fun `home top preset style keeps migrated ios aligned with miuix text tabs`() {
        val ios = topStyle(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        val material3 = topStyle(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        val miuix = topStyle(UiPreset.MD3, AndroidNativeVariant.MIUIX)

        assertEquals(ios.searchBarHeight, material3.searchBarHeight)
        assertEquals(material3.searchBarHeight, miuix.searchBarHeight)
        assertNotEquals(material3.unifiedPanelCornerRadius, miuix.unifiedPanelCornerRadius)
        // 2B 迁移：iOS 输入并入 MIUIX，与 miuix 呈现一致。
        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, ios.presentation)
        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, material3.presentation)
        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, miuix.presentation)
        assertEquals(TopTabIndicatorStyle.MATERIAL, ios.indicatorStyle)
        assertEquals(TopTabIndicatorStyle.MATERIAL, material3.indicatorStyle)
        assertEquals(TopTabIndicatorStyle.MATERIAL, miuix.indicatorStyle)
    }

    @Test
    fun `miuix icon modes use the shared compact top tab geometry`() {
        val iconAndText = topStyle(UiPreset.MD3, AndroidNativeVariant.MIUIX, labelMode = 0)

        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, iconAndText.presentation)
        assertEquals(36.dp, iconAndText.tabRowHeightDocked)
        assertEquals(40.dp, iconAndText.tabRowHeightFloating)
        assertEquals(30.dp, iconAndText.md3VisualSpec.selectedCapsuleHeight)
        assertEquals(44.dp, iconAndText.actionButtonSizeDocked)
    }

    @Test
    fun `miuix top panel reserves extra content gap below category tabs`() {
        val ios = topStyle(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        val material3 = topStyle(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        val miuix = topStyle(UiPreset.MD3, AndroidNativeVariant.MIUIX)

        // 2B 迁移：iOS 输入并入 MIUIX 预留 12dp 内容底部间隙。
        assertEquals(12.dp, ios.reservedContentBottomGap)
        assertEquals(5.dp, material3.reservedContentBottomGap)
        assertEquals(12.dp, miuix.reservedContentBottomGap)
        assertEquals(
            12.dp,
            resolveHomeTopReservedContentBottomGap(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
        // Feed air under dock is owned by tabsToContent (list padding), not panel bottom gap.
        assertEquals(6.dp, ios.tabsToContentSpacing)
        assertEquals(6.dp, material3.tabsToContentSpacing)
        assertEquals(6.dp, miuix.tabsToContentSpacing)
    }

    @Test
    fun `home top settings button converges to unified edge control height`() {
        assertEquals(
            36.dp, // 两主题统一：与头像、搜索胶囊同高
            resolveHomeTopSettingsButtonSize(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertEquals(
            36.dp,
            resolveHomeTopSettingsButtonSize(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertEquals(
            36.dp,
            resolveHomeTopSettingsButtonSize(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
        assertEquals(
            18.dp,
            resolveHomeTopSettingsIconSize(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertEquals(
            18.dp,
            resolveHomeTopSettingsIconSize(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertEquals(
            18.dp,
            resolveHomeTopSettingsIconSize(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
    }


    @Test
    fun `clicking selected top tab scrolls to top while other tabs select`() {
        assertEquals(
            TopTabClickAction.SCROLL_TO_TOP,
            resolveTopTabClickAction(index = 2, selectedIndex = 2)
        )
        assertEquals(
            TopTabClickAction.SELECT_TAB,
            resolveTopTabClickAction(index = 3, selectedIndex = 2)
        )
    }

    @Test
    fun `ios top tab tuning uses the compact shared top tab footprint`() {
        val tuning = resolveTopTabVisualTuning(AppTopTabPresentation.MOVING_CAPSULE)

        assertEquals(30f, tuning.nonFloatingIndicatorHeightDp, 0.001f)
        assertEquals(9f, tuning.nonFloatingIndicatorCornerDp, 0.001f)
        assertEquals(1.18f, tuning.nonFloatingIndicatorWidthRatio, 0.001f)
        assertEquals(84f, tuning.nonFloatingIndicatorMinWidthDp, 0.001f)
        assertEquals(0f, tuning.nonFloatingIndicatorHorizontalInsetDp, 0.001f)
        assertEquals(30f, tuning.floatingIndicatorHeightDp, 0.001f)
        assertEquals(15f, tuning.tabTextSizeSp, 0.001f)
        assertEquals(20f, tuning.tabTextLineHeightSp, 0.001f)
        assertEquals(30f, tuning.tabContentMinHeightDp, 0.001f)
        assertEquals(18f, tuning.tabIconWithTextSizeDp, 0.001f)
        assertEquals(18f, tuning.tabIconOnlySizeDp, 0.001f)
    }

    @Test
    fun `md3 top tab tuning uses the compact shared top tab shape`() {
        val tuning = resolveTopTabVisualTuning(AppTopTabPresentation.MATERIAL_UNDERLINE)

        assertEquals(30f, tuning.nonFloatingIndicatorHeightDp, 0.001f)
        assertEquals(9f, tuning.nonFloatingIndicatorCornerDp, 0.001f)
        assertEquals(30f, tuning.floatingIndicatorHeightDp, 0.001f)
        assertEquals(15f, tuning.tabTextSizeSp, 0.001f)
    }

    @Test
    fun `ios top tab keeps icon plus text scale stable inside large capsule`() {
        assertEquals(
            1f,
            resolveTopTabContentScale(
                selectionFraction = 1f,
                showIcon = true,
                showText = true,
                presentation = AppTopTabPresentation.MOVING_CAPSULE
            ),
            0.001f
        )
        assertEquals(
            1.03f,
            resolveTopTabContentScale(
                selectionFraction = 1f,
                showIcon = true,
                showText = false,
                presentation = AppTopTabPresentation.MOVING_CAPSULE
            ),
            0.001f
        )
    }

    @Test
    fun `ios lightweight top tab uses bottom bar capsule indicator shape`() {
        val source = sourceText("app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt")
        val itemBlock = source
            .substringAfter("private fun LightweightTopTabItem(")
            .substringBefore("Box(")

        assertTrue(itemBlock.contains("presentation == AppTopTabPresentation.MOVING_CAPSULE -> resolveSharedBottomBarCapsuleShape()"))
        assertFalse(itemBlock.contains("presentation == AppTopTabPresentation.MOVING_CAPSULE -> AppShapes.container(ContainerLevel.Pill)"))
    }

    @Test
    fun `top tab chrome centers wrapped dock`() {
        val source = sourceText(
            "app/src/main/java/com/android/purebilibili/feature/home/components/HomeTopTabChrome.kt"
        )

        assertTrue(source.contains("val dockAlignment = Alignment.Center"))
        assertTrue(source.contains(".align(dockAlignment)"))
        assertFalse(source.contains("val dockAlignment = Alignment.CenterStart"))
    }

    @Test
    fun `top tab indicator reuses bottom bar immediate drag and liquid rendering`() {
        val source = sourceText("app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt")
        val gestureBlock = source
            .substringAfter("private fun Modifier.topTabSelectedItemDrag(")
            .substringBefore("@Composable\nprivate fun")
        val iosIndicatorBlock = source
            .substringAfter("if (shouldUseMovingIosCapsule) {")
            .substringBefore("if (shouldUseMd3DockBackedCapsule)")
        val chromeSource = sourceText(
            "app/src/main/java/com/android/purebilibili/feature/home/components/HomeTopTabChrome.kt"
        )
        val bottomBarIndicatorBlock = sourceText(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt"
        ).substringAfter("internal fun BoxScope.KernelSuBottomBarIndicatorLayer(")
            .substringBefore("@Composable\nprivate fun BoxScope.KernelSuBottomBarInputLayer(")

        assertTrue(gestureBlock.contains("awaitHorizontalTouchSlopOrCancellation"))
        assertFalse(gestureBlock.contains("awaitLongPressOrCancellation"))
        assertTrue(iosIndicatorBlock.contains("BottomBarMatchedLiquidIndicator("))
        assertEquals(1, iosIndicatorBlock.split("BottomBarMatchedLiquidIndicator(").size - 1)
        assertTrue(iosIndicatorBlock.contains("glassEnabled = shouldUseLiquidGlassIndicator"))
        assertTrue(iosIndicatorBlock.contains("indicatorEffectsEnabled = shouldUseLiquidGlassIndicator"))
        assertFalse(iosIndicatorBlock.contains(".fillMaxHeight()"))
        assertFalse(source.contains("shouldForceDragLiquidGlassIndicator"))
        assertFalse(chromeSource.contains("Modifier.clip(tabShape)"))
        assertTrue(bottomBarIndicatorBlock.contains("indicatorIdleSurfaceColor"))
        assertTrue(bottomBarIndicatorBlock.contains("shellShape"))
    }

    @Test
    fun `liquid top tab glyphs switch below indicator while glass is moving`() {
        val source = sourceText("app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt")
        val visibleTabsBlock = source
            .substringAfter("LazyRow(\n                    state = listState,")
            .substringBefore("// Keep the indicator between its capture layer")
        val indicatorLayerBlock = source
            .substringAfter("// Keep the indicator between its capture layer")
            .substringBefore("} // shared panel-offset group")

        assertTrue(source.contains("val topTabVisibleContentZIndex = if (useTopTabGlassColorPath) 0f else 2f"))
        assertTrue(visibleTabsBlock.contains(".zIndex(topTabVisibleContentZIndex)"))
        assertTrue(indicatorLayerBlock.contains(".zIndex(1f)"))
    }

    @Test
    fun `dragging an offscreen top tab synchronizes its viewport`() {
        val source = sourceText("app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt")

        assertTrue(source.contains("val topTabDragTargetIndex = topTabDragPosition.roundToInt()"))
        assertTrue(source.contains("LaunchedEffect(topTabDragActive, topTabDragTargetIndex, isViewportSyncEnabled)"))
        assertTrue(source.contains("listState.scrollToItem(topTabDragTargetIndex)"))
    }

    @Test
    fun `ios lightweight top tab capsule uses gray white while content keeps theme primary`() {
        val colorScheme = lightColorScheme(primary = Color(0xFF2D6A4F))
        val capsuleColor = resolveIosTopTabCapsuleContainerColor(
            isDarkTheme = false,
            selectionFraction = 1f
        )
        val bottomIndicatorColor = resolveBottomBarMovingIndicatorSurfaceColor(isDarkTheme = false)

        assertEquals(bottomIndicatorColor.red, capsuleColor.red, 0.001f)
        assertEquals(bottomIndicatorColor.green, capsuleColor.green, 0.001f)
        assertEquals(bottomIndicatorColor.blue, capsuleColor.blue, 0.001f)
        assertEquals(0.28f, capsuleColor.alpha, 0.002f)
        assertEquals(
            colorScheme.primary,
            resolveIosTopTabSelectedContentColor(colorScheme)
        )
        assertFalse(capsuleColor == colorScheme.primary.copy(alpha = 0.10f))
    }

    @Test
    fun `ios top tab icon modes use readable glyph sizes`() {
        assertEquals(18f, resolveTopTabIconSizeDp(labelMode = 0), 0.001f)
        assertEquals(18f, resolveTopTabIconSizeDp(labelMode = 1), 0.001f)
        assertEquals(6f, resolveTopTabIconTextSpacingDp(labelMode = 0), 0.001f)
        // Must match compact chrome track (HomeTopPresetStyle 36/40) or labels clip to "...".
        assertEquals(36.dp, resolveIosTopTabRowHeight(isFloatingStyle = false))
        assertEquals(40.dp, resolveIosTopTabRowHeight(isFloatingStyle = true))
        assertEquals(44.dp, resolveIosTopTabActionButtonSize(isFloatingStyle = false))
        assertEquals(22.dp, resolveIosTopTabActionIconSize(isFloatingStyle = false))
    }

    @Test
    fun `all three top tab presentations keep compact chrome and content row heights aligned`() {
        listOf(
            topStyle(UiPreset.IOS, AndroidNativeVariant.MATERIAL3),
            topStyle(UiPreset.MD3, AndroidNativeVariant.MATERIAL3),
            topStyle(UiPreset.MD3, AndroidNativeVariant.MIUIX)
        ).forEach { style ->
            assertEquals(36.dp, style.tabRowHeightDocked)
            assertEquals(40.dp, style.tabRowHeightFloating)
        }

        assertEquals(36.dp, resolveIosTopTabRowHeight(isFloatingStyle = false))
        assertEquals(40.dp, resolveIosTopTabRowHeight(isFloatingStyle = true))
        assertEquals(
            36.dp,
            resolveMd3TopTabVisualSpec(
                isFloatingStyle = false,
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE
            ).rowHeight
        )
        assertEquals(
            40.dp,
            resolveMd3TopTabVisualSpec(
                isFloatingStyle = true,
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE
            ).rowHeight
        )
        assertEquals(
            36.dp,
            resolveMd3TopTabVisualSpec(
                isFloatingStyle = false,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            ).rowHeight
        )
    }

    @Test
    fun `md3 top tabs keep material typography spacing`() {
        val textSize = resolveTopTabLabelTextSizeSp(labelMode = 0)
        val lineHeight = resolveTopTabLabelLineHeightSp(labelMode = 0)

        assertEquals(15f, textSize, 0.001f)
        assertEquals(20f, lineHeight, 0.001f)
        assertTrue(lineHeight >= textSize)
    }

    @Test
    fun `md3 top tabs use compact rounded rectangle sizing`() {
        val spec = resolveMd3TopTabVisualSpec(isFloatingStyle = false)

        assertEquals(36.dp, spec.rowHeight)
        assertEquals(30.dp, spec.selectedCapsuleHeight)
        assertEquals(9.dp, spec.selectedCapsuleCornerRadius)
        assertEquals(18.dp, spec.iconSize)
        assertEquals(15.sp, spec.labelTextSize)
        assertEquals(20.sp, spec.labelLineHeight)
        assertEquals(0.dp, spec.iconLabelSpacing)
        assertEquals(10.dp, spec.itemHorizontalPadding)
        assertEquals(0.dp, spec.selectedCapsuleShadowElevation)
        assertEquals(0.dp, spec.selectedCapsuleTonalElevation)
    }

    @Test
    fun `md3 icon plus text top tabs reserve enough height`() {
        val spec = resolveMd3TopTabVisualSpec(
            isFloatingStyle = false,
            labelMode = 0
        )

        assertEquals(36.dp, spec.rowHeight)
        assertEquals(10.dp, spec.itemHorizontalPadding)
        assertEquals(6.dp, spec.iconLabelSpacing)
        assertEquals(18.dp, spec.iconSize)
        assertEquals(15.sp, spec.labelTextSize)
        assertTrue(spec.labelLineHeight >= spec.labelTextSize)
    }

    @Test
    fun `android native miuix top tabs should promote capsule selection styling`() {
        val spec = resolveMd3TopTabVisualSpec(
            isFloatingStyle = false,
            presentation = AppTopTabPresentation.TONAL_CAPSULE
        )

        assertEquals(36.dp, spec.rowHeight)
        assertEquals(30.dp, spec.selectedCapsuleHeight)
        assertEquals(9.dp, spec.selectedCapsuleCornerRadius)
        assertEquals(10.dp, spec.itemHorizontalPadding)
        assertEquals(0.dp, spec.iconLabelSpacing)
        assertEquals(15.sp, spec.labelTextSize)
    }


    @Test
    fun `android native miuix top tabs skip outer chrome surface`() {
        assertFalse(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.TONAL_CAPSULE,
                materialMode = TopTabMaterialMode.LIQUID_GLASS
            )
        )
        assertFalse(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.TONAL_CAPSULE,
                materialMode = TopTabMaterialMode.BLUR
            )
        )
        assertFalse(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.TONAL_CAPSULE,
                materialMode = TopTabMaterialMode.PLAIN
            )
        )
        assertTrue(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                materialMode = TopTabMaterialMode.LIQUID_GLASS
            )
        )
        // md3 下划线 tab 也统一绘制长胶囊背景（普通/模糊模式同样绘制），
        // 避免滚动时 tab 文字直接浮现在信息流上方。
        assertTrue(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                materialMode = TopTabMaterialMode.PLAIN
            )
        )
        assertTrue(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                materialMode = TopTabMaterialMode.BLUR
            )
        )
        assertTrue(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.MOVING_CAPSULE,
                materialMode = TopTabMaterialMode.LIQUID_GLASS
            )
        )
    }

    @Test
    fun `md3 selected top tab should reuse material primary emphasis`() {
        val colorScheme = lightColorScheme(
            surface = Color.White,
            primary = Color(0xFF2D6A4F),
            secondaryContainer = Color(0xFFDCEFD8),
            onSecondaryContainer = Color(0xFF1A1C18),
            onSurface = Color(0xFF1B1C1F),
            onSurfaceVariant = Color(0xFF6A5E61)
        )

        assertEquals(colorScheme.primary, resolveMd3TopTabSelectedContainerColor(colorScheme))
        assertEquals(colorScheme.primary, resolveMd3TopTabSelectedIconColor(colorScheme))
        assertEquals(colorScheme.primary, resolveMd3TopTabSelectedLabelColor(colorScheme))
        assertEquals(colorScheme.onSurfaceVariant, resolveMd3TopTabUnselectedIconColor(colorScheme))
        assertEquals(colorScheme.onSurfaceVariant, resolveMd3TopTabUnselectedLabelColor(colorScheme))
    }

    @Test
    fun `android native miuix top tabs should use miuix secondary container emphasis`() {
        val colorScheme = lightColorScheme(
            primary = Color(0xFF2D6A4F),
            surfaceContainerHigh = Color(0xFFF4ECE1),
            secondaryContainer = Color(0xFFDCEFD8),
            onSecondaryContainer = Color(0xFF1A1C18),
            onSurface = Color(0xFF1E1B16),
            onSurfaceVariant = Color(0xFF6A5E61)
        )

        assertEquals(
            colorScheme.secondaryContainer,
            resolveMd3TopTabSelectedContainerColor(
                colorScheme = colorScheme,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            )
        )
        assertEquals(
            colorScheme.onSecondaryContainer,
            resolveMd3TopTabSelectedIconColor(
                colorScheme = colorScheme,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            )
        )
        assertEquals(
            colorScheme.onSecondaryContainer,
            resolveMd3TopTabSelectedLabelColor(
                colorScheme = colorScheme,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            )
        )
    }


    @Test
    fun `md3 preset uses material tab indicator style`() {
        assertEquals(
            TopTabIndicatorStyle.MATERIAL,
            resolveTopTabIndicatorStyle(AppTopTabPresentation.MATERIAL_UNDERLINE)
        )
        assertEquals(
            TopTabIndicatorStyle.CAPSULE,
            resolveTopTabIndicatorStyle(AppTopTabPresentation.MOVING_CAPSULE)
        )
    }

    @Test
    fun `md3 top tabs always use material indicator after removing top liquid glass`() {
        assertTrue(
            shouldUseMd3TopTabMaterialIndicator(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                liquidGlassEnabled = true
            )
        )
        assertTrue(
            shouldUseMd3TopTabMaterialIndicator(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                liquidGlassEnabled = false
            )
        )
    }

    @Test
    fun `md3 top tabs keep outer dock in every material mode`() {
        // md3 下划线 tab 统一绘制长胶囊背景（BLUR/PLAIN/LIQUID_GLASS 均绘制）。
        assertTrue(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                materialMode = TopTabMaterialMode.BLUR
            )
        )
        assertTrue(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                materialMode = TopTabMaterialMode.PLAIN
            )
        )
        assertTrue(
            shouldDrawHomeTopTabOuterChromeSurface(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                materialMode = TopTabMaterialMode.LIQUID_GLASS
            )
        )
    }

    @Test
    fun `md3 top tabs use underline row semantics and tighter action shape`() {
        assertEquals(
            "UNDERLINE_FIXED",
            resolveMd3TopTabRowVariant().name
        )
        assertEquals(16.dp, resolveMd3TopTabActionButtonCorner(isFloatingStyle = true))
        assertEquals(12.dp, resolveMd3TopTabActionButtonCorner(isFloatingStyle = false))
        assertEquals(48.dp, resolveMd3TopTabActionButtonSize(isFloatingStyle = true))
        assertEquals(42.dp, resolveMd3TopTabActionButtonSize(isFloatingStyle = false))
        assertEquals(24.dp, resolveMd3TopTabActionIconSize(isFloatingStyle = true))
        assertEquals(22.dp, resolveMd3TopTabActionIconSize(isFloatingStyle = false))
        assertEquals(4.dp, resolveMd3TopTabActionContentBottomPadding())
        assertEquals(4f, resolveMd3TopTabVerticalLiftDp(), 0.001f)
        assertEquals(8.dp, resolveMd3TopTabIndicatorBottomPadding())
    }

    @Test
    fun `skin top tabs use compact readable underline layout`() {
        assertEquals(46.dp, resolveHomeSkinTopTabRowHeight())
        assertTrue(resolveHomeSkinTopTabRowHeight() >= 44.dp)
        assertTrue(resolveHomeSkinTopTabRowHeight() <= 48.dp)
        assertEquals(44.dp, resolveHomeSkinTopTabActionButtonSize())
        assertEquals(24.dp, resolveHomeSkinTopTabActionIconSize())
        assertEquals(4.dp, resolveHomeSkinTopTabIndicatorBottomPadding())
        assertEquals(32.dp, resolveTopTabSkinStickerIconSize(showText = true))
        assertEquals(36.dp, resolveTopTabSkinStickerIconSize(showText = false))
        assertEquals(32.dp, resolveTopTabSkinPartitionIconSize())
        assertEquals(28.dp, resolveTopTabSkinStickerIndicatorWidth())
        assertEquals(
            64.dp,
            resolveTopTabSkinStickerRowHeight(
                baseRowHeight = 56.dp,
                hasSkinStickerIcons = true,
                showIcon = true,
                showText = true
            )
        )
        assertEquals(
            52.dp,
            resolveTopTabSkinStickerRowHeight(
                baseRowHeight = 52.dp,
                hasSkinStickerIcons = false,
                showIcon = true,
                showText = true
            )
        )
        assertEquals(2.dp, resolveTopTabSkinStickerItemVerticalPadding(showText = true))
    }

    @Test
    fun `skin top tab colors stay readable on light and dark skin backgrounds`() {
        val darkBackgroundContent = resolveHomeSkinTopTabContentColor(Color(0xFF2E2A1E))
        val midDarkBackgroundContent = resolveHomeSkinTopTabContentColor(Color(0xFF778675))
        val lightBackgroundContent = resolveHomeSkinTopTabContentColor(Color(0xFFE4F6FF))

        assertEquals(Color.White.copy(alpha = 0.98f), darkBackgroundContent)
        assertEquals(Color.White.copy(alpha = 0.98f), midDarkBackgroundContent)
        assertEquals(Color(0xFF111820).copy(alpha = 0.96f), lightBackgroundContent)
        assertEquals(
            Color.White.copy(alpha = 0.84f),
            resolveHomeSkinTopTabUnselectedContentColor(darkBackgroundContent)
        )
        assertEquals(
            Color(0xFF111820).copy(alpha = 0.78f),
            resolveHomeSkinTopTabUnselectedContentColor(lightBackgroundContent)
        )
        assertEquals(darkBackgroundContent, resolveHomeSkinTopTabIndicatorColor(darkBackgroundContent))
        assertEquals(lightBackgroundContent, resolveHomeSkinTopTabIndicatorColor(lightBackgroundContent))
    }

    @Test
    fun `skin top tab image backed dark mode uses light content for all presets`() {
        val lightFallbackTint = Color(0xFFDFF5FF)
        val presets = listOf(
            UiPreset.IOS to AndroidNativeVariant.MATERIAL3,
            UiPreset.MD3 to AndroidNativeVariant.MATERIAL3,
            UiPreset.MD3 to AndroidNativeVariant.MIUIX
        )

        presets.forEach { (uiPreset, androidNativeVariant) ->
            topStyle(uiPreset, androidNativeVariant, labelMode = 0)
            val contentColor = resolveHomeSkinTopTabContentColor(
                topAtmosphereTint = lightFallbackTint,
                hasTopAtmosphereImage = true,
                darkTheme = true
            )

            assertEquals(
                "$uiPreset/$androidNativeVariant should stay readable over dark image-backed skin",
                Color.White.copy(alpha = 0.98f),
                contentColor
            )
        }
    }

    @Test
    fun `skin decoration keeps host top tab readability strategy`() {
        assertFalse(shouldUseHomeSkinPlainTopTabs(null))
        assertFalse(
            shouldUseHomeSkinPlainTopTabs(
                HomeUiSkinDecoration(
                    skinId = "test",
                    topAtmosphereTint = Color(0xFFE4F6FF),
                    searchCapsuleTint = Color.White
                )
            )
        )
    }

    @Test
    fun `skin top tabs render sticker image before host vector icon fallback`() {
        val source = sourceText("src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt")
        val rowCallSource = source
            .substringAfter("LightweightHomeTopTabs(")
            .substringBefore("private fun rememberTopTabPagerDragHeld(")
        val itemSource = source
            .substringAfter("private fun LightweightTopTabItem(")
            .substringBefore("@OptIn(ExperimentalMaterial3Api::class)")

        assertTrue(rowCallSource.contains("topTabSkinIconPaths = topTabSkinIconPaths"))
        assertTrue(rowCallSource.contains("partitionSkinIconPath = partitionSkinIconPath"))
        assertTrue(itemSource.contains("skinIconPath"))
        assertTrue(itemSource.contains("AsyncImage("))
        assertTrue(itemSource.contains("model = File(skinIconPath)"))
        assertTrue(itemSource.contains("resolveTopTabSkinStickerIconSize(showText = showText)"))
        assertTrue(rowCallSource.contains("resolveTopTabSkinPartitionIconSize()"))
        assertTrue(rowCallSource.contains("resolveTopTabSkinStickerRowHeight("))
        // 纯色 wash 胶囊仅限 skin 主题兜底；常规主题始终由移动胶囊负责。
        assertTrue(rowCallSource.contains("if (effectivePresentation == AppTopTabPresentation.MATERIAL_UNDERLINE && !hasSkinStickerIcons && skinPlainStyle)"))
        assertTrue(itemSource.contains("resolveTopTabSkinStickerItemVerticalPadding(showText = showText)"))
        assertTrue(itemSource.contains("resolveTopTabSkinStickerIndicatorWidth()"))
        assertTrue(itemSource.contains("alpha(selectionFraction)"))
        assertTrue(itemSource.indexOf("AsyncImage(") < itemSource.indexOf("TopTabBlendedIcon("))
        assertTrue(itemSource.contains("else {"))
        assertTrue(itemSource.contains("resolveTopTabCategoryIcon("))
        assertFalse(itemSource.contains("resolveMiuixPreferredTopTabCategoryIcon("))
        // Host still passes the shared icon family into LightweightHomeTopTabs.
        assertTrue(rowCallSource.contains("iconFamily = topTabIconFamily") || source.contains("iconFamily = topTabIconFamily"))
    }

    @Test
    fun `skin top tab stickers keep ios md3 and miuix on shared item indicator path`() {
        val source = sourceText("src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt")
        val categoryTabRowSource = source
            .substringAfter("fun CategoryTabRow(")
            .substringBefore("@Composable\nprivate fun rememberTopTabPagerDragHeld(")

        assertTrue(categoryTabRowSource.contains("val hasSkinStickerIcons = topTabSkinIconPaths.isNotEmpty() || !partitionSkinIconPath.isNullOrBlank()"))
        assertTrue(categoryTabRowSource.contains("topTabSkinIconPaths = topTabSkinIconPaths"))
        assertTrue(categoryTabRowSource.contains("partitionSkinIconPath = partitionSkinIconPath"))
    }

    @Test
    fun `android native miuix top tabs should slightly enlarge action button chrome`() {
        assertEquals(
            18.dp,
            resolveMd3TopTabActionButtonCorner(
                isFloatingStyle = true,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            )
        )
        assertEquals(
            14.dp,
            resolveMd3TopTabActionButtonCorner(
                isFloatingStyle = false,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            )
        )
        assertEquals(
            50.dp,
            resolveMd3TopTabActionButtonSize(
                isFloatingStyle = true,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            )
        )
        assertEquals(
            44.dp,
            resolveMd3TopTabActionButtonSize(
                isFloatingStyle = false,
                presentation = AppTopTabPresentation.TONAL_CAPSULE
            )
        )
    }

    private fun topStyle(
        uiPreset: UiPreset,
        androidNativeVariant: AndroidNativeVariant,
        labelMode: Int = 2,
    ): HomeTopPresetStyle = resolveHomeTopPresetStyle(
        // 兼容桥接：旧 pair 输入经迁移表落到两值风格。
        chromePolicy = resolveAppTopChromePolicy(resolveUiStyle(uiPreset, androidNativeVariant)),
        labelMode = labelMode,
    )

    private fun sourceText(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
