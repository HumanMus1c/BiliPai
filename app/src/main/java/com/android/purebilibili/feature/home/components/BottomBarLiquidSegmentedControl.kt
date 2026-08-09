package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.OpticalContrastPalette

import androidx.compose.animation.core.EaseOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.ui.rememberAppSemanticVisualPolicy
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.animation.horizontalDragGesture
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.android.purebilibili.core.ui.motion.BottomBarMotionProfile
import com.android.purebilibili.core.ui.motion.BottomBarMotionSpec
import com.android.purebilibili.core.ui.motion.resolveBottomBarMotionSpec
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.android.purebilibili.feature.home.components.liquid.lens as miuixLens
import com.android.purebilibili.feature.home.components.liquid.rememberCombinedBackdrop as rememberMiuixCombinedBackdrop
import com.android.purebilibili.feature.home.components.liquid.vibrancy as miuixVibrancy
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop
import top.yukonga.miuix.kmp.blur.blur as miuixBlur
import top.yukonga.miuix.kmp.blur.drawBackdrop as miuixDrawBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop as miuixLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop as rememberMiuixLayerBackdrop
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

internal fun resolveSegmentedControlLiquidGlassEnabled(
    storedLiquidGlassEnabled: Boolean,
    liquidGlassEffectsEnabled: Boolean,
    supportsIndependentLiquidGlass: Boolean,
    androidNativeLiquidGlassEnabled: Boolean
): Boolean {
    if (!liquidGlassEffectsEnabled) return false
    return androidNativeLiquidGlassEnabled ||
        (supportsIndependentLiquidGlass && storedLiquidGlassEnabled)
}

internal enum class SegmentedControlChromeStyle {
    LIQUID_PILL,
    ANDROID_NATIVE_UNDERLINE
}

internal const val BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP = 58
internal const val BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP = 56
private const val SEGMENTED_CONTROL_MIN_INDICATOR_ASPECT_RATIO = 1.6f

internal fun resolveSegmentedControlChromeStyle(
    prefersNativeChrome: Boolean,
    androidNativeLiquidGlassEnabled: Boolean,
    preferInlineContentStyle: Boolean = false
): SegmentedControlChromeStyle {
    return if (prefersNativeChrome && !androidNativeLiquidGlassEnabled) {
        SegmentedControlChromeStyle.ANDROID_NATIVE_UNDERLINE
    } else {
        SegmentedControlChromeStyle.LIQUID_PILL
    }
}

internal fun resolveLiquidSegmentedControlUnselectedTextColor(
    onSurface: Color,
    enabled: Boolean
): Color = if (enabled) onSurface else onSurface.copy(alpha = 0.42f)

internal fun resolveSegmentedControlIndicatorWidthDp(
    slotWidthDp: Float,
    indicatorHeightDp: Float,
    itemCount: Int
): Float {
    if (slotWidthDp <= 0f || indicatorHeightDp <= 0f || itemCount <= 0) return 0f
    return slotWidthDp
}

internal fun resolveSegmentedControlIndicatorHeightDp(
    slotWidthDp: Float,
    indicatorHeightDp: Float
): Float {
    if (slotWidthDp <= 0f || indicatorHeightDp <= 0f) return 0f
    return min(
        indicatorHeightDp,
        slotWidthDp / SEGMENTED_CONTROL_MIN_INDICATOR_ASPECT_RATIO
    )
}

internal fun resolveSegmentedControlIndicatorOffsetDp(
    position: Float,
    slotWidthDp: Float,
    contentPaddingDp: Float
): Float {
    return contentPaddingDp + (slotWidthDp * position)
}

internal fun shouldFollowSegmentedControlIndicatorDrag(
    pointerX: Float,
    indicatorPosition: Float,
    itemWidthPx: Float
): Boolean {
    if (itemWidthPx <= 0f) return false
    val startX = indicatorPosition * itemWidthPx
    val endX = startX + itemWidthPx
    return pointerX in startX..endX
}

internal fun resolveSegmentedControlSweepSelectionIndex(
    pointerX: Float,
    itemWidthPx: Float,
    itemCount: Int
): Int {
    if (itemWidthPx <= 0f || itemCount <= 0) return 0
    return (pointerX.coerceAtLeast(0f) / itemWidthPx)
        .toInt()
        .coerceIn(0, itemCount - 1)
}

internal fun resolveSegmentedControlIndicatorPosition(
    internalPosition: Float,
    externalPosition: Float?,
    itemCount: Int
): Float {
    if (itemCount <= 0) return 0f
    return (externalPosition ?: internalPosition)
        .coerceIn(0f, (itemCount - 1).toFloat())
}

internal fun shouldDrawSegmentedControlIndicatorBackdrop(
    liquidGlassEnabled: Boolean,
    motionProgress: Float,
    hasExternalBackdrop: Boolean
): Boolean {
    if (!liquidGlassEnabled) return false
    return hasExternalBackdrop || motionProgress > 0.001f
}

/**
 * Export capture may drawBackdrop only from an external page LayerBackdrop.
 * Sampling the same tabs LayerBackdrop being recorded on that node creates a
 * cyclic RenderNode graph and overflows HyperOS MiBackgroundBlurBlend.
 */
internal fun shouldDrawSegmentedControlExportCaptureBackdrop(
    liquidGlassEnabled: Boolean,
    hasExternalBackdrop: Boolean
): Boolean {
    return liquidGlassEnabled && hasExternalBackdrop
}

@Composable
internal fun BottomBarLiquidIndicatorSurface(
    modifier: Modifier = Modifier,
    shape: Shape = resolveSharedBottomBarCapsuleShape(),
    liquidGlassEnabled: Boolean,
    backdrop: Backdrop? = null,
    hasExternalBackdrop: Boolean = backdrop != null,
    indicatorLensSpec: BottomBarBackdropPresetLensSpec = resolveBottomBarBackdropPresetIndicatorLens(
        progress = if (liquidGlassEnabled) 1f else 0f
    ),
    indicatorHighlightAlpha: Float = resolveBottomBarLiquidGlassHighlightAlpha(
        motionProgress = if (liquidGlassEnabled) 1f else 0f
    ),
    indicatorGlowAlpha: Float = resolveBottomBarIndicatorGlowAlpha(
        glassEnabled = liquidGlassEnabled,
        pressProgress = 0f
    ),
    motionProgress: Float = 0f,
    idleSurfaceColor: Color = Color.Unspecified,
    layerBlock: GraphicsLayerScope.() -> Unit = {}
) {
    val resolvedIdleSurfaceColor = if (idleSurfaceColor == Color.Unspecified) {
        resolveAndroidNativeIdleIndicatorSurfaceColor(darkTheme = isSystemInDarkTheme())
    } else {
        idleSurfaceColor
    }
    Box(
        modifier = modifier.run {
            if (backdrop != null && shouldDrawSegmentedControlIndicatorBackdrop(
                    liquidGlassEnabled = liquidGlassEnabled,
                    motionProgress = motionProgress,
                    hasExternalBackdrop = hasExternalBackdrop
                )
            ) {
                drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        lens(
                            refractionHeight = indicatorLensSpec.refractionHeightDp.dp.toPx(),
                            refractionAmount = indicatorLensSpec.refractionAmountDp.dp.toPx(),
                            depthEffect = true,
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(alpha = maxOf(indicatorHighlightAlpha, indicatorGlowAlpha))
                    },
                    shadow = {
                        Shadow(alpha = indicatorGlowAlpha)
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = AppSpacingTokens.Small * indicatorGlowAlpha,
                            alpha = indicatorGlowAlpha
                        )
                    },
                    layerBlock = layerBlock,
                    onDrawSurface = {
                        drawRect(
                            color = resolvedIdleSurfaceColor,
                            alpha = 1f - motionProgress
                        )
                        drawRect(OpticalContrastPalette.Shadow.copy(alpha = 0.03f * motionProgress))
                    }
                )
            } else {
                background(resolvedIdleSurfaceColor, shape)
            }
        }
    )
}

internal fun resolveSegmentedControlMotionProgress(
    pressProgress: Float,
    refractionProgress: Float,
    tapPressRefractionEnabled: Boolean
): Float {
    val resolvedPressProgress = if (tapPressRefractionEnabled) pressProgress else 0f
    return maxOf(resolvedPressProgress, refractionProgress)
}

internal fun resolveSegmentedControlExternalPagerVelocityItemsPerSecond(
    currentPosition: Float,
    previousPosition: Float,
    elapsedNanos: Long,
): Float {
    if (elapsedNanos <= 0L) return 0f
    val elapsedSeconds = elapsedNanos / 1_000_000_000f
    if (elapsedSeconds <= 0f) return 0f
    return ((currentPosition - previousPosition) / elapsedSeconds)
        .coerceIn(-12f, 12f)
}

internal fun shouldStretchSegmentedControlExternalPagerIndicator(
    position: Float,
    externalPagerMotionActive: Boolean,
    positionEpsilon: Float = 0.015f,
): Boolean {
    if (!externalPagerMotionActive) return false
    return abs(position - position.roundToInt().toFloat()) > positionEpsilon
}

/**
 * Shared liquid segmented/top-tab indicator motion must match the home floating bottom bar.
 * Do not soften springs/offsets here — any divergence makes swipe stretch/settle feel wrong.
 */
internal fun resolveSegmentedControlMotionSpec(): BottomBarMotionSpec {
    return resolveBottomBarMotionSpec(profile = BottomBarMotionProfile.ANDROID_NATIVE_FLOATING)
}

/**
 * Same panel-offset formula as [KernelSuAlignedBottomBar]: fraction of full dock width,
 * capped at AppSpacingTokens.ExtraSmall, EaseOut mapped.
 */
internal fun resolveSharedLiquidIndicatorPanelOffsetPx(
    dragOffsetPx: Float,
    dockWidthPx: Float,
    maxOffsetPx: Float
): Float {
    if (dockWidthPx <= 0f) return 0f
    val fraction = (dragOffsetPx / dockWidthPx).coerceIn(-1f, 1f)
    return maxOffsetPx * fraction.sign * EaseOut.transform(abs(fraction))
}

/**
 * Lens/refraction progress for shared liquid indicators.
 * Bottom bar keeps a drag floor so slow swipes still show glass stretch instead of fading out.
 */
internal fun resolveSharedLiquidIndicatorLensProgress(
    pressProgress: Float,
    motionProgress: Float,
    isDragging: Boolean
): Float {
    val dragFloor = if (isDragging) 0.6f else 0f
    return maxOf(pressProgress, motionProgress, dragFloor).coerceIn(0f, 1f)
}

/**
 * When glass is active and the capsule is moving, visible labels stay neutral and the
 * selected color is carried by the export layer + tint (same as home bottom bar).
 */
internal fun resolveSharedLiquidIndicatorUseGlassColorPath(
    liquidGlassEnabled: Boolean,
    lensProgress: Float
): Boolean = liquidGlassEnabled && lensProgress > 0.001f

/** Capture lens strength: full 24dp while interacting, like KernelSu bottom bar capture. */
internal fun resolveSharedLiquidIndicatorCaptureLensProgress(
    lensProgress: Float,
    isDragging: Boolean
): Float {
    if (isDragging) return 1f
    return lensProgress.coerceIn(0f, 1f)
}

/**
 * Export-layer glyph color before [ColorFilter.tint].
 * Must stay near-white so SrcIn tint resolves to pure theme/primary color.
 */
internal fun resolveSharedLiquidExportMonochromeColor(
    darkTheme: Boolean
): Color = if (darkTheme) {
    OpticalContrastPalette.Highlight.copy(alpha = 0.96f)
} else {
    OpticalContrastPalette.Highlight
}

@Composable
fun BottomBarLiquidSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemWidth: Dp? = null,
    height: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP.dp,
    indicatorHeight: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP.dp,
    labelFontSize: TextUnit = TextUnit.Unspecified,
    containerHorizontalPadding: Dp = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
    containerVerticalPadding: Dp = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
    liquidGlassEffectsEnabled: Boolean = true,
    dragSelectionEnabled: Boolean = true,
    preferInlineContentStyle: Boolean = false,
    forceLiquidChrome: Boolean = false,
    backdrop: Backdrop? = null,
    miuixBackdrop: MiuixBackdrop? = null,
    tapPressRefractionEnabled: Boolean = true,
    containerColorOverride: Color? = null,
    selectedTextColorOverride: Color? = null,
    unselectedTextColorOverride: Color? = null,
    indicatorIdleSurfaceColorOverride: Color? = null,
    indicatorPositionProvider: (() -> Float)? = null,
    onIndicatorPositionChanged: ((Float) -> Unit)? = null,
    isScrollInProgressProvider: () -> Boolean = { false },
    externalPagerMotionEffectsEnabled: Boolean = false,
) {
    if (items.isEmpty()) return

    val effectiveLabelFontSize = if (labelFontSize.isSpecified) {
        labelFontSize
    } else {
        MaterialTheme.typography.labelMedium.fontSize
    }

    val context = LocalContext.current
    val visualPolicy = rememberAppSemanticVisualPolicy()
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings(),
            context = kotlin.coroutines.EmptyCoroutineContext
        )
    val effectiveAndroidNativeLiquidGlassEnabled =
        forceLiquidChrome || homeSettings.androidNativeLiquidGlassEnabled
    val chromeStyle = resolveSegmentedControlChromeStyle(
        prefersNativeChrome = visualPolicy.prefersNativeChrome,
        androidNativeLiquidGlassEnabled = effectiveAndroidNativeLiquidGlassEnabled,
        preferInlineContentStyle = preferInlineContentStyle
    )
    if (chromeStyle == SegmentedControlChromeStyle.ANDROID_NATIVE_UNDERLINE) {
        AndroidNativeUnderlinedSegmentedControl(
            items = items,
            selectedIndex = selectedIndex,
            onSelected = onSelected,
            modifier = modifier,
            enabled = enabled,
            itemWidth = itemWidth,
            height = height,
            labelFontSize = effectiveLabelFontSize,
            selectedTextColorOverride = selectedTextColorOverride,
            unselectedTextColorOverride = unselectedTextColorOverride,
            indicatorPositionProvider = indicatorPositionProvider,
            onIndicatorPositionChanged = onIndicatorPositionChanged
        )
        return
    }

    val liquidGlassEnabled = resolveSegmentedControlLiquidGlassEnabled(
        storedLiquidGlassEnabled = homeSettings.isBottomBarLiquidGlassEnabled,
        liquidGlassEffectsEnabled = liquidGlassEffectsEnabled,
        supportsIndependentLiquidGlass = visualPolicy.supportsIndependentLiquidGlass,
        androidNativeLiquidGlassEnabled = effectiveAndroidNativeLiquidGlassEnabled
    )
    val blurIntensity = currentUnifiedBlurIntensity()
    val density = LocalDensity.current
    val itemCount = items.size
    val safeSelectedIndex = selectedIndex.coerceIn(0, itemCount - 1)
    val motionSpec = remember { resolveSegmentedControlMotionSpec() }
    val clickPulseKey = remember { mutableIntStateOf(0) }
    val clickPulseTransform = rememberBottomBarClickPulseTransform(clickPulseKey.intValue)
    val matchedChromeState = rememberBottomBarMatchedLiquidChromeState(
        initialIndex = safeSelectedIndex,
        itemCount = itemCount,
        notifyIndexChangedOnReleaseStart = indicatorPositionProvider != null,
        isScrollInProgressProvider = isScrollInProgressProvider,
        onIndexChanged = { index ->
            if (enabled && index in items.indices) {
                onSelected(index)
            }
        }
    )
    val dragState = matchedChromeState.dragState
    val indicatorShape = resolveSharedBottomBarCapsuleShape()
    val containerShapeToken = AppShapes.container(ContainerLevel.Pill)
    val containerShape = indicatorShape
    val indicatorCorner = indicatorHeight / 2
    val isDarkTheme = isSystemInDarkTheme()
    val surfaceColor = AppSurfaceTokens.cardContainer()
    val androidNativeTuning = resolveAndroidNativeBottomBarTuning(
        blurEnabled = liquidGlassEnabled,
        darkTheme = isDarkTheme
    )
    val containerColor = containerColorOverride ?: resolveAndroidNativeFloatingBottomBarContainerColor(
        surfaceColor = surfaceColor,
        tuning = androidNativeTuning,
        glassEnabled = liquidGlassEnabled,
        blurEnabled = liquidGlassEnabled,
        blurIntensity = blurIntensity,
        liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset
    )
    val themeColor = MaterialTheme.colorScheme.primary
    val selectedTextColor = selectedTextColorOverride ?: themeColor
    val unselectedTextColor = unselectedTextColorOverride
        ?: resolveLiquidSegmentedControlUnselectedTextColor(
            onSurface = MaterialTheme.colorScheme.onSurface,
            enabled = enabled
        )
    // Bottom-bar path: export is monochrome so SrcIn tint becomes pure theme color under glass.
    val exportTintColor = resolveAndroidNativeExportTintColor(
        themeColor = themeColor,
        darkTheme = isDarkTheme
    )
    val exportMonochromeColor = resolveSharedLiquidExportMonochromeColor(darkTheme = isDarkTheme)
    fun selectFromTap(index: Int) {
        if (!enabled || index !in items.indices) return
        clickPulseKey.intValue += 1
        // Animate indicator with the same spring path as home bottom bar taps.
        dragState.updateIndex(index)
        onSelected(index)
    }
    LaunchedEffect(safeSelectedIndex) {
        dragState.updateIndex(safeSelectedIndex)
    }

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (itemWidth != null) {
                    Modifier.width((itemWidth.value * itemCount).dp + containerHorizontalPadding * 2)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .height(height)
    ) {
        val contentPadding = containerHorizontalPadding
        val contentVerticalInset = containerVerticalPadding
        val slotWidth = (maxWidth - (contentPadding * 2)) / itemCount
        val indicatorWidth = resolveSegmentedControlIndicatorWidthDp(
            slotWidthDp = slotWidth.value,
            indicatorHeightDp = indicatorHeight.value,
            itemCount = itemCount
        ).dp
        val resolvedIndicatorHeight = resolveSegmentedControlIndicatorHeightDp(
            slotWidthDp = slotWidth.value,
            indicatorHeightDp = indicatorHeight.value
        ).dp
        val externalIndicatorPosition = if (dragState.isDragging) {
            null
        } else {
            indicatorPositionProvider?.invoke()
        }
        val indicatorPosition = resolveSegmentedControlIndicatorPosition(
            internalPosition = dragState.value,
            externalPosition = externalIndicatorPosition,
            itemCount = itemCount
        )
        val indicatorOffset = resolveSegmentedControlIndicatorOffsetDp(
            position = indicatorPosition,
            slotWidthDp = slotWidth.value,
            contentPaddingDp = contentPadding.value
        ).dp
        val itemWidthPx = with(density) { slotWidth.toPx() }.coerceAtLeast(1f)
        val dockWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        // Match home bottom bar: drag anywhere on the dock, not only from the capsule.
        val dragModifier = if (enabled && itemCount > 1 && dragSelectionEnabled) {
            Modifier.horizontalDragGesture(
                dragState = dragState,
                itemWidthPx = itemWidthPx
            )
        } else {
            Modifier
        }
        val externalPagerMotionActive = externalPagerMotionEffectsEnabled &&
            externalIndicatorPosition != null &&
            isScrollInProgressProvider()
        val externalVelocityPositionTracker = remember {
            FloatArray(1) { indicatorPosition }
        }
        val externalVelocityTimeTracker = remember {
            LongArray(1) { System.nanoTime() }
        }
        val velocitySampleTimeNanos = System.nanoTime()
        val externalPagerVelocityItemsPerSecond = if (externalPagerMotionActive) {
            resolveSegmentedControlExternalPagerVelocityItemsPerSecond(
                currentPosition = indicatorPosition,
                previousPosition = externalVelocityPositionTracker[0],
                elapsedNanos = (velocitySampleTimeNanos - externalVelocityTimeTracker[0])
                    .coerceAtLeast(1L),
            )
        } else {
            0f
        }
        SideEffect {
            externalVelocityPositionTracker[0] = indicatorPosition
            externalVelocityTimeTracker[0] = velocitySampleTimeNanos
        }
        val motionVelocityItemsPerSecond = when {
            dragState.isDragging -> dragState.deformationVelocityItemsPerSecond
            externalPagerMotionActive -> externalPagerVelocityItemsPerSecond
            externalPagerMotionEffectsEnabled && indicatorPositionProvider != null -> 0f
            else -> dragState.deformationVelocityItemsPerSecond
        }
        val motionVelocityPxPerSecond = if (externalPagerMotionActive) {
            externalPagerVelocityItemsPerSecond * itemWidthPx
        } else {
            dragState.velocityPxPerSecond
        }
        val indicatorShouldStretch = dragState.isDragging ||
            shouldStretchSegmentedControlExternalPagerIndicator(
                position = indicatorPosition,
                externalPagerMotionActive = externalPagerMotionActive,
            )
        val indicatorIsInteracting = dragState.isDragging || externalPagerMotionActive
        SideEffect {
            onIndicatorPositionChanged?.invoke(indicatorPosition)
        }
        val pressMotionProgress by remember {
            derivedStateOf { dragState.pressProgress }
        }
        val refractionMotionProfile = resolveBottomBarEffectiveRefractionMotionProfile(
            preset = homeSettings.bottomBarLiquidGlassPreset,
            profile = resolveBottomBarRefractionMotionProfile(
                position = indicatorPosition,
                velocity = motionVelocityPxPerSecond,
                isDragging = indicatorIsInteracting,
                motionSpec = motionSpec
            )
        )
        val motionProgress = resolveSegmentedControlMotionProgress(
            pressProgress = pressMotionProgress,
            refractionProgress = refractionMotionProfile.progress,
            // Always keep refraction progress for swipe glass; press is still used for scale/lens floor.
            tapPressRefractionEnabled = true
        )
        val effectivePressProgress = if (tapPressRefractionEnabled) {
            pressMotionProgress
        } else {
            // Even when call sites disable "tap press refraction", drag still calls press()
            // in DampedDragAnimation — keep that press for scale/lens while dragging.
            if (dragState.isDragging) pressMotionProgress else 0f
        }
        val indicatorDragScaleProgress = rememberBottomBarIndicatorDragScaleProgress(
            isDragging = indicatorShouldStretch
        )
        // Match bottom bar: 88/56 drag-scale + velocity stretch (no compound scaleX/Y).
        val indicatorLayerScaleProgress = maxOf(indicatorDragScaleProgress, effectivePressProgress)
        val lensProgress = resolveSharedLiquidIndicatorLensProgress(
            pressProgress = effectivePressProgress,
            motionProgress = motionProgress,
            isDragging = indicatorShouldStretch
        )
        val useGlassColorPath = resolveSharedLiquidIndicatorUseGlassColorPath(
            liquidGlassEnabled = liquidGlassEnabled,
            lensProgress = lensProgress
        )
        val rawPanelOffsetPx by remember(density, dockWidthPx) {
            derivedStateOf {
                val maxOffsetPx = with(density) { AppSpacingTokens.ExtraSmall.toPx() }
                resolveSharedLiquidIndicatorPanelOffsetPx(
                    dragOffsetPx = dragState.dragOffset,
                    dockWidthPx = dockWidthPx,
                    maxOffsetPx = maxOffsetPx
                )
            }
        }
        val matchedPanelOffsetPx = if (externalPagerMotionActive) {
            refractionMotionProfile.indicatorPanelOffsetFraction.coerceIn(-1f, 1f) *
                with(density) { AppSpacingTokens.ExtraSmall.toPx() }
        } else {
            rawPanelOffsetPx
        }
        val presetPanelOffsets = remember(
            homeSettings.bottomBarLiquidGlassPreset,
            matchedPanelOffsetPx,
        ) {
            resolveBottomBarPresetPanelOffsets(
                preset = homeSettings.bottomBarLiquidGlassPreset,
                rawPanelOffsetPx = matchedPanelOffsetPx
            )
        }
        val panelOffsetPx = presetPanelOffsets.indicatorPanelOffsetPx
        val exportPanelOffsetPx = presetPanelOffsets.exportPanelOffsetPx
        val tabsBackdrop = rememberLayerBackdrop()
        val tabsMiuixBackdrop = rememberMiuixLayerBackdrop()
        val localPageMiuixBackdrop = rememberMiuixLayerBackdrop()
        val useBottomBarMatchedMiuix = effectiveAndroidNativeLiquidGlassEnabled
        val pageMiuixBackdrop = miuixBackdrop ?: localPageMiuixBackdrop
        val combinedMiuixBackdrop = rememberMiuixCombinedBackdrop(
            pageMiuixBackdrop,
            tabsMiuixBackdrop
        )
        // The local page source and export source are siblings. Neither source contains
        // the liquid target, so the combined path cannot recursively sample itself.
        val hasExternalBackdrop = backdrop != null
        val containerBackdrop = backdrop
        val captureLensProgress = resolveSharedLiquidIndicatorCaptureLensProgress(
            lensProgress = lensProgress,
            isDragging = indicatorShouldStretch
        )
        // Full 24dp capture lens while interacting — same constant strength as bottom bar capture.
        val captureLensSpec = resolveBottomBarBackdropPresetCaptureLens(
            progress = captureLensProgress
        )
        val captureSafeLensSpec = resolveBottomBarBackdropPresetCaptureLens(progress = 1f)
        val captureSafeInset = resolveBottomBarCaptureSafeInsetDp(
            indicatorWidthDp = indicatorWidth.value,
            refractionHeightDp = captureSafeLensSpec.refractionHeightDp,
            refractionAmountDp = captureSafeLensSpec.refractionAmountDp,
            panelOffsetDp = AppSpacingTokens.ExtraSmall.value
        ).dp
        // Indicator capsule lens follows swipe, not only finger-down press.
        val indicatorLensSpec = resolveBottomBarBackdropPresetIndicatorLens(
            progress = lensProgress
        )
        val captureHighlightAlpha = resolveBottomBarLiquidGlassHighlightAlpha(captureLensProgress)
        val indicatorIdleSurfaceColor = indicatorIdleSurfaceColorOverride
            ?: resolveBottomBarIdleIndicatorSurfaceColor(
                preset = homeSettings.bottomBarLiquidGlassPreset,
                darkTheme = isDarkTheme
            )
        val foregroundAboveIndicator = shouldRenderBottomBarForegroundAboveIndicator(
            homeSettings.bottomBarLiquidGlassPreset
        )

        if (useBottomBarMatchedMiuix && miuixBackdrop == null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .bottomBarMatchedCaptureOverflow(captureSafeInset)
                    .alpha(0f)
                    .miuixLayerBackdrop(localPageMiuixBackdrop)
                    .background(AppSurfaceTokens.background())
                    .clearAndSetSemantics {}
            )
        }

        BottomBarMatchedLiquidDock(
            backdrop = if (useBottomBarMatchedMiuix) pageMiuixBackdrop else null,
            legacyBackdrop = backdrop,
            containerColor = containerColor,
            shape = containerShape,
            blurEnabled = liquidGlassEnabled,
            glassEnabled = liquidGlassEnabled,
            // Inline segmented controls often sit directly above horizontal separators.
            // Refracting the entire shell pulls those edges into the vertical center and
            // makes them look like dashed strokes between labels. The moving indicator
            // keeps its own lens, so the interactive liquid-glass response is preserved.
            drawShellLens = false,
            blurRadius = androidNativeTuning.shellBlurRadiusDp.dp,
            modifier = Modifier.matchParentSize(),
            liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset,
            isScrollInProgressProvider = isScrollInProgressProvider
        ) {}

        // 1) Visible labels BEHIND the capsule (bottom-bar z-order).
        //    While sliding they stay neutral; theme color is revealed only through glass.
        BottomBarLiquidSegmentedLabels(
            items = items,
            selectedIndex = safeSelectedIndex,
            indicatorPosition = indicatorPosition,
            motionProgress = motionProgress,
            selectionEmphasis = refractionMotionProfile.visibleSelectionEmphasis,
            selectedTextColor = selectedTextColor,
            unselectedTextColor = unselectedTextColor,
            enabled = enabled,
            labelFontSize = effectiveLabelFontSize,
            indicatorCorner = indicatorCorner,
            onSelected = onSelected,
            interactive = false,
            applyItemScale = true,
            forceUnselectedColor = useGlassColorPath,
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = contentPadding, vertical = contentVerticalInset)
                .zIndex(if (foregroundAboveIndicator) 1f else 0f)
                .graphicsLayer { translationX = panelOffsetPx }
        )

        // 2) Hidden export capture: monochrome glyphs, theme tint on content only (not backdrop).
        Box(
            modifier = Modifier
                .matchParentSize()
                .clearAndSetSemantics {}
                .alpha(0f)
                .run {
                    if (useBottomBarMatchedMiuix) {
                        this.miuixLayerBackdrop(tabsMiuixBackdrop)
                            .graphicsLayer { translationX = exportPanelOffsetPx }
                            .run {
                                if (
                                    shouldDrawSegmentedControlExportCaptureBackdrop(
                                        liquidGlassEnabled = liquidGlassEnabled,
                                        hasExternalBackdrop = true
                                    )
                                ) {
                                    miuixDrawBackdrop(
                                        backdrop = pageMiuixBackdrop,
                                        shape = { containerShape },
                                        effects = {
                                            miuixVibrancy()
                                            miuixBlur(AppSpacingTokens.ExtraSmall.toPx(), AppSpacingTokens.ExtraSmall.toPx())
                                            if (captureLensProgress > 0.001f) {
                                                miuixLens(
                                                    refractionHeight = captureLensSpec.refractionHeightDp.dp.toPx(),
                                                    refractionAmount = captureLensSpec.refractionAmountDp.dp.toPx(),
                                                    depthEffect = true,
                                                    chromaticAberration = 0.5f
                                                )
                                            }
                                        },
                                        onDrawSurface = { drawRect(containerColor) }
                                    )
                                } else {
                                    this
                                }
                            }
                    } else {
                        this.layerBackdrop(tabsBackdrop)
                            .graphicsLayer { translationX = exportPanelOffsetPx }
                            .run {
                                if (
                                    shouldDrawSegmentedControlExportCaptureBackdrop(
                                        liquidGlassEnabled = liquidGlassEnabled,
                                        hasExternalBackdrop = hasExternalBackdrop
                                    ) && containerBackdrop != null
                                ) {
                                    drawBackdrop(
                                        backdrop = containerBackdrop,
                                        shape = { containerShape },
                                        effects = {
                                            vibrancy()
                                            blur(androidNativeTuning.shellBlurRadiusDp.dp.toPx())
                                            if (captureLensProgress > 0.001f) {
                                                lens(
                                                    refractionHeight = captureLensSpec.refractionHeightDp.dp.toPx(),
                                                    refractionAmount = captureLensSpec.refractionAmountDp.dp.toPx(),
                                                    depthEffect = true,
                                                    chromaticAberration = true
                                                )
                                            }
                                        },
                                        highlight = {
                                            Highlight.Default.copy(alpha = captureHighlightAlpha)
                                        },
                                        onDrawSurface = { drawRect(containerColor) }
                                    )
                                } else {
                                    this
                                }
                            }
                    }
                }
        ) {
            BottomBarLiquidSegmentedLabels(
                items = items,
                selectedIndex = safeSelectedIndex,
                indicatorPosition = indicatorPosition,
                motionProgress = motionProgress,
                selectionEmphasis = refractionMotionProfile.exportSelectionEmphasis,
                // Match bottom bar export: neutral glyphs then SrcIn-tint to primary.
                selectedTextColor = exportMonochromeColor,
                unselectedTextColor = exportMonochromeColor,
                enabled = enabled,
                labelFontSize = effectiveLabelFontSize,
                indicatorCorner = indicatorCorner,
                onSelected = onSelected,
                interactive = false,
                applyItemScale = true,
                forceUnselectedColor = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = contentPadding, vertical = contentVerticalInset)
                    .graphicsLayer(colorFilter = ColorFilter.tint(exportTintColor))
            )
        }

        // 3) Capsule on top — samples export theme glyphs through glass.
        if (useBottomBarMatchedMiuix) {
            BottomBarMatchedLiquidIndicator(
                visible = true,
                dockContentAlpha = 1f,
                indicatorTranslationXPx = with(density) { indicatorOffset.toPx() },
                indicatorPanelOffsetPx = panelOffsetPx,
                indicatorWidth = indicatorWidth,
                indicatorHeight = resolvedIndicatorHeight,
                shellShape = indicatorShape,
                liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset,
                contentBackdrop = combinedMiuixBackdrop,
                backdrop = pageMiuixBackdrop,
                indicatorLensSpec = indicatorLensSpec,
                effectivePressProgress = lensProgress,
                indicatorIdleSurfaceColor = indicatorIdleSurfaceColor,
                glassEnabled = liquidGlassEnabled,
                motionProgress = motionProgress,
                velocityItemsPerSecond = motionVelocityItemsPerSecond,
                isDragging = indicatorShouldStretch,
                indicatorLayerScaleProgress = indicatorLayerScaleProgress,
                bottomBarMotionSpec = motionSpec,
                isDarkTheme = isDarkTheme
            )
        } else {
            BottomBarMatchedLiquidIndicator(
                visible = true,
                dockContentAlpha = 1f,
                indicatorTranslationXPx = with(density) { indicatorOffset.toPx() },
                indicatorPanelOffsetPx = panelOffsetPx,
                indicatorWidth = indicatorWidth,
                indicatorHeight = resolvedIndicatorHeight,
                shellShape = indicatorShape,
                liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset,
                contentBackdrop = null,
                backdrop = null,
                legacyContentBackdrop = tabsBackdrop,
                legacyBackdrop = backdrop,
                indicatorLensSpec = indicatorLensSpec,
                effectivePressProgress = lensProgress,
                indicatorIdleSurfaceColor = indicatorIdleSurfaceColor,
                glassEnabled = liquidGlassEnabled,
                motionProgress = motionProgress,
                velocityItemsPerSecond = motionVelocityItemsPerSecond,
                isDragging = indicatorShouldStretch,
                indicatorLayerScaleProgress = indicatorLayerScaleProgress,
                bottomBarMotionSpec = motionSpec,
                isDarkTheme = isDarkTheme,
                indicatorSettleReboundTransform = clickPulseTransform
            )
        }

        // 4) Invisible hit / drag layer above everything.
        BottomBarLiquidSegmentedLabels(
            items = items,
            selectedIndex = safeSelectedIndex,
            indicatorPosition = indicatorPosition,
            motionProgress = motionProgress,
            selectionEmphasis = refractionMotionProfile.visibleSelectionEmphasis,
            selectedTextColor = selectedTextColor,
            unselectedTextColor = unselectedTextColor,
            enabled = enabled,
            labelFontSize = effectiveLabelFontSize,
            indicatorCorner = indicatorCorner,
            onSelected = ::selectFromTap,
            interactive = true,
            onPressChanged = dragState::setPressed,
            applyItemScale = false,
            forceUnselectedColor = false,
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = contentPadding, vertical = contentVerticalInset)
                .alpha(0f)
                .graphicsLayer { translationX = panelOffsetPx }
                .then(dragModifier)
        )
    }
}

@Composable
internal fun AndroidNativeUnderlinedSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemWidth: Dp? = null,
    height: Dp,
    labelFontSize: TextUnit,
    selectedTextColorOverride: Color? = null,
    unselectedTextColorOverride: Color? = null,
    indicatorPositionProvider: (() -> Float)? = null,
    onIndicatorPositionChanged: ((Float) -> Unit)? = null
) {
    val itemCount = items.size
    val safeSelectedIndex = selectedIndex.coerceIn(0, itemCount - 1)
    val selectedTextColor = selectedTextColorOverride ?: MaterialTheme.colorScheme.primary
    val unselectedTextColor = unselectedTextColorOverride
        ?: MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.78f else 0.42f)
    val underlineShape = CircleShape
    val indicatorPosition = resolveSegmentedControlIndicatorPosition(
        internalPosition = safeSelectedIndex.toFloat(),
        externalPosition = indicatorPositionProvider?.invoke(),
        itemCount = itemCount
    )

    SideEffect {
        onIndicatorPositionChanged?.invoke(indicatorPosition)
    }

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (itemWidth != null) {
                    Modifier.width(itemWidth * itemCount)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .height(height)
    ) {
        val segmentWidth = maxWidth / itemCount
        val underlineWidth = (segmentWidth * 0.42f)
            .coerceAtLeast(AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall)
            .coerceAtMost(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Small)
        val underlineOffsetX = (segmentWidth * indicatorPosition) + ((segmentWidth - underlineWidth) / 2)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, label ->
                val selected = index == safeSelectedIndex
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clickable(enabled = enabled) { onSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = label,
                        color = if (selected) selectedTextColor else unselectedTextColor,
                        fontSize = labelFontSize,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = underlineOffsetX)
                .width(underlineWidth)
                .height(AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2)
                .clip(underlineShape)
                .background(selectedTextColor)
        )
    }
}

@Composable
private fun BottomBarLiquidSegmentedLabels(
    items: List<String>,
    selectedIndex: Int,
    indicatorPosition: Float,
    motionProgress: Float,
    selectionEmphasis: Float,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    enabled: Boolean,
    labelFontSize: TextUnit,
    indicatorCorner: Dp,
    onSelected: (Int) -> Unit,
    interactive: Boolean,
    onPressChanged: ((Boolean) -> Unit)? = null,
    applyItemScale: Boolean = true,
    forceUnselectedColor: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, label ->
            val interactionSource = remember { MutableInteractionSource() }
            if (interactive && onPressChanged != null) {
                val pressed by interactionSource.collectIsPressedAsState()
                LaunchedEffect(pressed) {
                    onPressChanged(pressed)
                }
            }
            val visual = resolveBottomBarItemMotionVisual(
                itemIndex = index,
                indicatorPosition = indicatorPosition,
                currentSelectedIndex = selectedIndex,
                motionProgress = motionProgress,
                selectionEmphasis = selectionEmphasis
            )
            val contentColors = resolveLiquidGlassSelectionContentColors(
                unselectedColor = unselectedTextColor,
                selectedColor = selectedTextColor,
                themeWeight = visual.themeWeight,
                glassEnabled = forceUnselectedColor,
                indicatorProgress = motionProgress,
                indicatorBackdropEnabled = true
            )
            val textColor = if (!enabled) {
                unselectedTextColor.copy(alpha = 0.44f)
            } else {
                contentColors.visibleColor
            }
            val labelScale = if (applyItemScale) visual.scale else 1f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(indicatorCorner))
                    .then(
                        if (interactive) {
                            Modifier.clickable(
                                enabled = enabled,
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onSelected(index)
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = label,
                    color = textColor,
                    fontSize = labelFontSize,
                    fontWeight = if (visual.themeWeight > 0.5f && !forceUnselectedColor) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        scaleX = labelScale
                        scaleY = labelScale
                    }
                )
            }
        }
    }
}
