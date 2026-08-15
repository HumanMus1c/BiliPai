package com.android.purebilibili.feature.home.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.android.purebilibili.core.ui.rememberAppSemanticVisualPolicy
import com.android.purebilibili.core.ui.resolveMatchedLiquidIndicatorGeometry
import com.android.purebilibili.feature.home.components.liquid.InnerShadow
import com.android.purebilibili.feature.home.components.liquid.innerShadow
import com.android.purebilibili.feature.home.components.liquid.lens
import com.android.purebilibili.feature.home.components.liquid.rememberCombinedBackdrop
import com.android.purebilibili.feature.home.components.liquid.vibrancy
import com.android.purebilibili.feature.home.components.miuix.DampedDragAnimation
import com.android.purebilibili.feature.home.components.miuix.InteractiveHighlight
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

/**
 * Same three layers and local [DampedDragAnimation] as [FloatingBottomBar].
 *
 *   Box
 *   ├─ Base Row (labels)     // dropShadow + drawBackdrop(vibrancy+blur)
 *   ├─ Export Row (hidden)   // alpha=0 + layerBackdrop(tabsBackdrop)
 *   └─ Moving indicator Box  // combinedBackdrop + lens + innerShadow
 *                            // gestureModifier + dampedDragAnimation.modifier
 *
 * Indicator translation is always [DampedDragAnimation.value]. External pager
 * position is written into that animation while the pager is scrolling; it
 * never overrides the graphicsLayer the way a display-position overlay would.
 */
@Composable
internal fun BottomBarFloatingSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    itemWidth: Dp?,
    height: Dp,
    indicatorHeight: Dp,
    labelFontSize: TextUnit,
    containerHorizontalPadding: Dp,
    containerVerticalPadding: Dp,
    liquidGlassEffectsEnabled: Boolean,
    dragSelectionEnabled: Boolean,
    forceLiquidChrome: Boolean,
    miuixBackdrop: Backdrop?,
    containerColorOverride: Color?,
    selectedTextColorOverride: Color?,
    unselectedTextColorOverride: Color?,
    indicatorPositionProvider: (() -> Float)?,
    onIndicatorPositionChanged: ((Float) -> Unit)?,
    isScrollInProgressProvider: () -> Boolean = { false },
) {
    val context = LocalContext.current
    val visualPolicy = rememberAppSemanticVisualPolicy()
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val nativeGlassEnabled = forceLiquidChrome || homeSettings.androidNativeLiquidGlassEnabled
    val liquidGlassEnabled = resolveSegmentedControlLiquidGlassEnabled(
        storedLiquidGlassEnabled = homeSettings.isBottomBarLiquidGlassEnabled,
        liquidGlassEffectsEnabled = liquidGlassEffectsEnabled,
        supportsIndependentLiquidGlass = visualPolicy.supportsIndependentLiquidGlass,
        androidNativeLiquidGlassEnabled = nativeGlassEnabled,
    )
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
    val itemCount = items.size
    val maxTabIndex = (itemCount - 1).coerceAtLeast(0)
    val safeSelectedIndex = selectedIndex.coerceIn(0, maxTabIndex)
    val onSelectedLatest by rememberUpdatedState(onSelected)
    val isScrollInProgressLatest by rememberUpdatedState(isScrollInProgressProvider)
    val indicatorPositionLatest by rememberUpdatedState(indicatorPositionProvider)
    val pillShape = remember { resolveSharedBottomBarCapsuleShape() }
    val isDarkTheme = isSystemInDarkTheme()
    val blurIntensity = currentUnifiedBlurIntensity()
    val tuning = resolveAndroidNativeBottomBarTuning(
        blurEnabled = liquidGlassEnabled,
        darkTheme = isDarkTheme,
    )
    val containerColor = containerColorOverride ?: resolveAndroidNativeFloatingBottomBarContainerColor(
        surfaceColor = AppSurfaceTokens.cardContainer(),
        tuning = tuning,
        glassEnabled = liquidGlassEnabled,
        blurEnabled = liquidGlassEnabled,
        blurIntensity = blurIntensity,
        liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset,
    )
    val themeColor = MaterialTheme.colorScheme.primary
    val selectedTextColor = selectedTextColorOverride ?: themeColor
    val unselectedTextColor = unselectedTextColorOverride
        ?: resolveLiquidSegmentedControlUnselectedTextColor(
            onSurface = MaterialTheme.colorScheme.onSurface,
            enabled = enabled,
        )
    val exportTintColor = resolveAndroidNativeExportTintColor(
        themeColor = themeColor,
        darkTheme = isDarkTheme,
    )
    val exportMonochromeColor = resolveSharedLiquidExportMonochromeColor(darkTheme = isDarkTheme)

    val localPageBackdrop = rememberLayerBackdrop()
    val tabsBackdrop = rememberLayerBackdrop()
    val pageBackdrop = miuixBackdrop ?: localPageBackdrop
    val combinedBackdrop = rememberCombinedBackdrop(pageBackdrop, tabsBackdrop)

    var tabWidthPx by remember { mutableFloatStateOf(0f) }
    var totalWidthPx by remember { mutableFloatStateOf(0f) }

    val offsetAnimation = remember { Animatable(0f) }
    val rubberBandPx = with(density) { 4.dp.toPx() }
    val panelOffset by remember(rubberBandPx) {
        derivedStateOf {
            if (totalWidthPx == 0f) {
                0f
            } else {
                val fraction = (offsetAnimation.value / totalWidthPx).fastCoerceIn(-1f, 1f)
                rubberBandPx * fraction.sign * EaseOut.transform(abs(fraction))
            }
        }
    }

    var currentIndex by remember { mutableIntStateOf(safeSelectedIndex) }

    class DampedDragAnimationHolder {
        var instance: DampedDragAnimation? = null
    }

    val holder = remember { DampedDragAnimationHolder() }

    val dampedDragAnimation = remember(animationScope, itemCount, density, isLtr) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = safeSelectedIndex.toFloat(),
            valueRange = 0f..maxTabIndex.toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = FloatingBottomBarPressedScale,
            canDrag = { offset ->
                val animation = holder.instance ?: return@DampedDragAnimation true
                if (tabWidthPx == 0f) return@DampedDragAnimation false

                val indicatorX = animation.value * tabWidthPx
                val padding = with(density) { containerHorizontalPadding.toPx() }
                val globalTouchX = if (isLtr) {
                    padding + indicatorX + offset.x
                } else {
                    totalWidthPx - padding - tabWidthPx - indicatorX + offset.x
                }
                globalTouchX in 0f..totalWidthPx
            },
            onDragStarted = {},
            onDragStopped = {
                val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, maxTabIndex)
                currentIndex = targetIndex
                animateToValue(targetIndex.toFloat())
                animationScope.launch {
                    offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                }
            },
            onDrag = { _, dragAmount ->
                if (tabWidthPx > 0f) {
                    updateValue(
                        (targetValue + dragAmount.x / tabWidthPx * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, maxTabIndex.toFloat())
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            },
        ).also { holder.instance = it }
    }

    LaunchedEffect(safeSelectedIndex) {
        currentIndex = safeSelectedIndex
    }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { currentIndex }
            .drop(1)
            .collectLatest { index ->
                dampedDragAnimation.animateToValue(index.toFloat())
                if (enabled && index in items.indices) {
                    onSelectedLatest(index)
                }
            }
    }
    LaunchedEffect(dampedDragAnimation, maxTabIndex) {
        snapshotFlow {
            val external = indicatorPositionLatest?.invoke()
            val scrolling = isScrollInProgressLatest()
            Triple(external, scrolling, dampedDragAnimation.isDragging)
        }.collectLatest { (external, scrolling, dragging) ->
            if (dragging || !scrolling || external == null) return@collectLatest
            dampedDragAnimation.snapTo(external.coerceIn(0f, maxTabIndex.toFloat()))
        }
    }

    val interactiveHighlight =
        if (liquidGlassEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            remember(animationScope, tabWidthPx) {
                InteractiveHighlight(
                    animationScope = animationScope,
                    position = { size, _ ->
                        Offset(
                            if (isLtr) {
                                (dampedDragAnimation.value + 0.5f) * tabWidthPx + panelOffset
                            } else {
                                size.width - (dampedDragAnimation.value + 0.5f) * tabWidthPx +
                                    panelOffset
                            },
                            size.height / 2f,
                        )
                    },
                )
            }
        } else {
            null
        }

    val baseHighlight = rememberGravityRotatedHighlight(extraDegrees = -45f)
    val pillHighlight = rememberGravityRotatedHighlight(extraDegrees = 90f)

    Box(
        modifier = modifier.then(
            if (itemWidth != null) {
                Modifier.width(itemWidth * itemCount + containerHorizontalPadding * 2)
            } else {
                Modifier.fillMaxWidth()
            }
        )
            .height(height)
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (liquidGlassEnabled && miuixBackdrop == null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .layerBackdrop(localPageBackdrop)
                    .background(AppSurfaceTokens.background())
                    .clearAndSetSemantics {}
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    totalWidthPx = coords.size.width.toFloat()
                    val contentWidthPx =
                        totalWidthPx - with(density) { (containerHorizontalPadding * 2).toPx() }
                    tabWidthPx = (contentWidthPx / itemCount.coerceAtLeast(1)).coerceAtLeast(0f)
                }
                .graphicsLayer { translationX = panelOffset }
                .dropShadow(
                    shape = pillShape,
                    shadow = Shadow(
                        radius = 10.dp,
                        color = Color.Black,
                        alpha = if (isDarkTheme) 0.2f else 0.1f,
                    ),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .then(
                    if (liquidGlassEnabled) {
                        Modifier.drawBackdrop(
                            backdrop = pageBackdrop,
                            shape = { pillShape },
                            effects = {
                                vibrancy()
                                blur(4.dp.toPx(), 4.dp.toPx())
                                // A full-width shell lens folds horizontal content edges into
                                // the center of compact tab docks (the visible black seam). Keep
                                // refraction on the moving indicator, as the home bottom bar does.
                            },
                            highlight = { baseHighlight.copy(alpha = 0.75f) },
                            layerBlock = {
                                val width = size.width.coerceAtLeast(1f)
                                val s = lerp(
                                    1f,
                                    1f + 16.dp.toPx() / width,
                                    dampedDragAnimation.pressProgress,
                                )
                                scaleX = s
                                scaleY = s
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier.background(containerColor, pillShape)
                    }
                )
                .then(
                    if (liquidGlassEnabled && interactiveHighlight != null) {
                        interactiveHighlight.modifier
                    } else {
                        Modifier
                    }
                )
                .height(height)
                .padding(
                    horizontal = containerHorizontalPadding,
                    vertical = containerVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomBarLiquidSegmentedLabels(
                items = items,
                selectedIndex = safeSelectedIndex,
                indicatorPosition = dampedDragAnimation.value,
                motionProgress = dampedDragAnimation.pressProgress,
                selectionEmphasis = 1f,
                selectedTextColor = selectedTextColor,
                unselectedTextColor = unselectedTextColor,
                enabled = enabled,
                labelFontSize = labelFontSize,
                indicatorCorner = indicatorHeight / 2,
                onSelected = { index ->
                    if (!enabled || index !in items.indices) return@BottomBarLiquidSegmentedLabels
                    currentIndex = index
                },
                interactive = true,
                applyItemScale = true,
                forceUnselectedColor = liquidGlassEnabled,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        val resolvedIndicatorHeight = if (tabWidthPx <= 0f) {
            indicatorHeight
        } else {
            resolveSegmentedControlIndicatorHeightDp(
                slotWidthDp = with(density) { tabWidthPx.toDp().value },
                indicatorHeightDp = indicatorHeight.value,
            ).dp
        }
        val indicatorGeometry = resolveMatchedLiquidIndicatorGeometry(
            dockHeightDp = height.value,
            indicatorHeightDp = resolvedIndicatorHeight.value,
        )
        SideEffect {
            dampedDragAnimation.pressedScale = indicatorGeometry.pressedScale
        }

        if (liquidGlassEnabled) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabsBackdrop)
                    .graphicsLayer { translationX = panelOffset }
                    .drawBackdrop(
                        backdrop = pageBackdrop,
                        shape = { pillShape },
                        effects = {
                            vibrancy()
                            blur(4.dp.toPx(), 4.dp.toPx())
                            // The export row is sampled by the moving indicator. Refracting this
                            // whole row would bake the same center seam into that sampled layer.
                        },
                        onDrawSurface = { drawRect(containerColor) },
                    )
                    .then(interactiveHighlight?.modifier ?: Modifier)
                    .height(resolvedIndicatorHeight)
                    .padding(horizontal = containerHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomBarLiquidSegmentedLabels(
                    items = items,
                    selectedIndex = safeSelectedIndex,
                    indicatorPosition = dampedDragAnimation.value,
                    motionProgress = dampedDragAnimation.pressProgress,
                    selectionEmphasis = 1f,
                    selectedTextColor = exportMonochromeColor,
                    unselectedTextColor = exportMonochromeColor,
                    enabled = enabled,
                    labelFontSize = labelFontSize,
                    indicatorCorner = indicatorHeight / 2,
                    onSelected = {},
                    interactive = false,
                    applyItemScale = true,
                    forceUnselectedColor = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer(colorFilter = ColorFilter.tint(exportTintColor)),
                )
            }
        }

        if (tabWidthPx > 0f && itemCount > 0) {
            val tabWidthDp = with(density) { tabWidthPx.toDp() }
            if (liquidGlassEnabled) {
                Box(
                    Modifier
                        .padding(horizontal = containerHorizontalPadding)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) {
                                progressOffset + panelOffset
                            } else {
                                -progressOffset + panelOffset
                            }
                        }
                        .then(interactiveHighlight?.gestureModifier ?: Modifier)
                        .then(
                            if (enabled && itemCount > 1 && dragSelectionEnabled) {
                                dampedDragAnimation.modifier
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            enabled = enabled,
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                            onClick = {
                                currentIndex = safeSelectedIndex
                                onSelected(safeSelectedIndex)
                            },
                        )
                        .clearAndSetSemantics {}
                        .drawBackdrop(
                            backdrop = combinedBackdrop,
                            shape = { pillShape },
                            effects = {
                                val progress = dampedDragAnimation.pressProgress
                                lens(
                                    refractionHeight = 10.dp.toPx() * progress,
                                    refractionAmount = 14.dp.toPx() * progress,
                                    depthEffect = true,
                                    chromaticAberration = 0.5f,
                                )
                            },
                            highlight = {
                                pillHighlight.copy(alpha = dampedDragAnimation.pressProgress)
                            },
                            layerBlock = {
                                scaleX = dampedDragAnimation.scaleX
                                scaleY = dampedDragAnimation.scaleY
                                val velocity = dampedDragAnimation.velocity / 10f
                                scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
                                val progress = dampedDragAnimation.pressProgress
                                drawRect(
                                    color = if (!isDarkTheme) {
                                        Color.Black.copy(alpha = 0.1f)
                                    } else {
                                        Color.White.copy(alpha = 0.1f)
                                    },
                                    alpha = 1f - progress,
                                )
                                drawRect(Color.Black.copy(alpha = 0.03f * progress))
                            },
                        )
                        .innerShadow(shape = pillShape) {
                            InnerShadow(
                                radius = 8.dp * dampedDragAnimation.pressProgress,
                                color = Color.Black.copy(alpha = 0.15f),
                                alpha = dampedDragAnimation.pressProgress,
                            )
                        }
                        .height(resolvedIndicatorHeight)
                        .width(tabWidthDp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(horizontal = containerHorizontalPadding)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) {
                                progressOffset + panelOffset
                            } else {
                                -progressOffset + panelOffset
                            }
                        }
                        .then(
                            if (enabled && itemCount > 1 && dragSelectionEnabled) {
                                dampedDragAnimation.modifier
                            } else {
                                Modifier
                            }
                        )
                        .height(resolvedIndicatorHeight)
                        .width(tabWidthDp)
                        .background(
                            color = selectedTextColor.copy(alpha = 0.15f),
                            shape = pillShape,
                        )
                )
            }
        }

        SideEffect {
            onIndicatorPositionChanged?.invoke(dampedDragAnimation.value)
        }
    }
}
