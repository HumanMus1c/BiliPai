package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import com.android.purebilibili.core.theme.LocalCornerRadiusScale
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalDynamicColorActive
import com.android.purebilibili.core.theme.LocalSettingsLiquidGlassEnabled
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import com.android.purebilibili.core.ui.resolveCompactCapsuleChromeSpec
import com.android.purebilibili.core.theme.iOSCornerRadius
import com.android.purebilibili.core.ui.LocalAppThemeConfig
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.adaptiveSquircleBackground
import io.github.alexzhirkevich.cupertino.CupertinoSwitch
import io.github.alexzhirkevich.cupertino.CupertinoSwitchDefaults
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.*
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import com.android.purebilibili.core.ui.AppSurfaceTokens
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.preference.ArrowPreference as MiuixArrowPreference
import top.yukonga.miuix.kmp.preference.SliderPreference as MiuixSliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference as MiuixSwitchPreference
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.max

private object NoOpHapticFeedback : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}

// ═══════════════════════════════════════════════════
//  Common iOS List Components (Reused across Settings, Profile, etc.)
// ═══════════════════════════════════════════════════

data class AdaptiveListComponentVisualSpec(
    val sectionStartPaddingDp: Int,
    val groupCornerRadiusDp: Int,
    val groupTonalElevationDp: Int,
    val iconCornerRadiusDp: Int,
    val iconContainerSizeDp: Int,
    val iconGlyphSizeDp: Int,
    val iconBackgroundAlpha: Float,
    val gridCornerRadiusDp: Int,
    val searchBarCornerRadiusDp: Int,
    val searchBarHeightDp: Int,
    val dividerThicknessDp: Float,
    val dividerStartIndentDp: Int
)

enum class AppPreferenceIconTreatment {
    TONAL,
    FILLED,
}

val LocalAppPreferenceIconTreatment = staticCompositionLocalOf {
    AppPreferenceIconTreatment.TONAL
}

val LocalAppPreferenceGroupPresentation = staticCompositionLocalOf {
    AppPreferenceGroupPresentation.CARD
}

internal data class AdaptiveSwitchVisualSpec(
    val usePlatformDefaults: Boolean,
    val checkedThumbColor: Color,
    val checkedTrackColor: Color,
    val uncheckedThumbColor: Color,
    val uncheckedTrackColor: Color,
    val uncheckedBorderColor: Color
)

data class AdaptiveListRowVisualSpec(
    val insideHorizontalPaddingDp: Int,
    val insideVerticalPaddingDp: Int,
    val trailingIconSizeDp: Int,
    val trailingSpacingDp: Int,
    val minTouchTargetHeightDp: Int
)

/**
 * Semantic list capabilities consumed by feature screens without exposing the active UI style.
 */
data class AdaptiveListVisualCapabilities(
    val componentSpec: AdaptiveListComponentVisualSpec,
    val rowSpec: AdaptiveListRowVisualSpec,
    val useComponentDefaultGroupShape: Boolean,
    val showExplicitActionChevron: Boolean,
) {
    fun adaptGroupShape(candidate: Shape): Shape? =
        if (useComponentDefaultGroupShape) null else candidate
}

internal fun resolveAdaptiveListComponentVisualSpec(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): AdaptiveListComponentVisualSpec {
    val chromeTokens = resolveAndroidNativeChromeTokens(uiPreset, androidNativeVariant)
    val compactChrome = resolveCompactCapsuleChromeSpec(uiPreset, androidNativeVariant)
    return if (uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX) {
        AdaptiveListComponentVisualSpec(
            sectionStartPaddingDp = chromeTokens.denseHorizontalSpacingDp,
            groupCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
            groupTonalElevationDp = chromeTokens.tonalSurfaceElevationDp,
            iconCornerRadiusDp = 10,
            iconContainerSizeDp = 38,
            iconGlyphSizeDp = 20,
            iconBackgroundAlpha = chromeTokens.selectedContainerAlpha,
            gridCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
            searchBarCornerRadiusDp = compactChrome.primaryCornerRadiusDp,
            searchBarHeightDp = compactChrome.primaryHeightDp,
            dividerThicknessDp = 0f,
            dividerStartIndentDp = chromeTokens.denseHorizontalSpacingDp
        )
    } else if (uiPreset == UiPreset.MD3) {
        AdaptiveListComponentVisualSpec(
            sectionStartPaddingDp = chromeTokens.denseHorizontalSpacingDp,
            groupCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
            groupTonalElevationDp = chromeTokens.tonalSurfaceElevationDp,
            iconCornerRadiusDp = 12,
            iconContainerSizeDp = 40,
            iconGlyphSizeDp = 22,
            iconBackgroundAlpha = chromeTokens.selectedContainerAlpha,
            gridCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
            searchBarCornerRadiusDp = compactChrome.primaryCornerRadiusDp,
            searchBarHeightDp = compactChrome.primaryHeightDp,
            dividerThicknessDp = 0f,
            dividerStartIndentDp = chromeTokens.denseHorizontalSpacingDp
        )
    } else {
        AdaptiveListComponentVisualSpec(
            sectionStartPaddingDp = 32,
            groupCornerRadiusDp = 20,
            groupTonalElevationDp = 1,
            iconCornerRadiusDp = 10,
            iconContainerSizeDp = 36,
            iconGlyphSizeDp = 20,
            iconBackgroundAlpha = 0.12f,
            gridCornerRadiusDp = 20,
            searchBarCornerRadiusDp = 10,
            searchBarHeightDp = 40,
            dividerThicknessDp = 0.5f,
            dividerStartIndentDp = 66
        )
    }
}

internal fun resolveAdaptiveListRowVisualSpec(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): AdaptiveListRowVisualSpec {
    val chromeTokens = resolveAndroidNativeChromeTokens(uiPreset, androidNativeVariant)
    return if (uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX) {
        AdaptiveListRowVisualSpec(
            insideHorizontalPaddingDp = 16,
            insideVerticalPaddingDp = 14,
            trailingIconSizeDp = 14,
            trailingSpacingDp = 6,
            minTouchTargetHeightDp = chromeTokens.rowMinTouchTargetDp
        )
    } else if (uiPreset == UiPreset.MD3) {
        AdaptiveListRowVisualSpec(
            insideHorizontalPaddingDp = 18,
            insideVerticalPaddingDp = 16,
            trailingIconSizeDp = 16,
            trailingSpacingDp = 8,
            minTouchTargetHeightDp = chromeTokens.rowMinTouchTargetDp
        )
    } else {
        AdaptiveListRowVisualSpec(
            insideHorizontalPaddingDp = 16,
            insideVerticalPaddingDp = 14,
            trailingIconSizeDp = 20,
            trailingSpacingDp = 6,
            minTouchTargetHeightDp = chromeTokens.rowMinTouchTargetDp
        )
    }
}

internal fun resolveAdaptiveListVisualCapabilities(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3,
): AdaptiveListVisualCapabilities = AdaptiveListVisualCapabilities(
    componentSpec = resolveAdaptiveListComponentVisualSpec(uiPreset, androidNativeVariant),
    rowSpec = resolveAdaptiveListRowVisualSpec(uiPreset, androidNativeVariant),
    useComponentDefaultGroupShape = uiPreset == UiPreset.IOS,
    showExplicitActionChevron = !shouldUseNativeMiuixSearchBar(uiPreset, androidNativeVariant),
)

@Composable
fun rememberAdaptiveListVisualCapabilities(): AdaptiveListVisualCapabilities {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    return remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListVisualCapabilities(uiPreset, androidNativeVariant)
    }
}

internal fun resolveAdaptiveGroupContainerColor(
    uiPreset: UiPreset,
    colorScheme: ColorScheme,
    fallbackColor: Color,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3,
    globalWallpaperVisible: Boolean = false
): Color {
    val resolvedColor = if (uiPreset == UiPreset.MD3) {
        when {
            androidNativeVariant == AndroidNativeVariant.MIUIX -> colorScheme.surfaceContainer
            else -> colorScheme.surfaceContainerLow
        }
    } else {
        fallbackColor
    }
    return resolveGlobalWallpaperListContainerColor(
        containerColor = resolvedColor,
        colorScheme = colorScheme,
        globalWallpaperVisible = globalWallpaperVisible,
        targetAlpha = 0.62f
    )
}

internal fun resolveAdaptiveSearchBarContainerColor(
    uiPreset: UiPreset,
    colorScheme: ColorScheme,
    fallbackColor: Color,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3,
    globalWallpaperVisible: Boolean = false
): Color {
    val resolvedColor = if (uiPreset == UiPreset.MD3) {
        when {
            androidNativeVariant == AndroidNativeVariant.MIUIX -> colorScheme.surfaceContainer
            else -> colorScheme.surfaceContainerHigh
        }
    } else {
        fallbackColor
    }
    return resolveGlobalWallpaperListContainerColor(
        containerColor = resolvedColor,
        colorScheme = colorScheme,
        globalWallpaperVisible = globalWallpaperVisible,
        targetAlpha = 0.48f
    )
}

internal fun shouldUseNativeMiuixSearchBar(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): Boolean = uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX

internal fun resolveGlobalWallpaperListContainerColor(
    containerColor: Color,
    colorScheme: ColorScheme,
    globalWallpaperVisible: Boolean,
    targetAlpha: Float
): Color {
    if (!globalWallpaperVisible || containerColor.alpha == 0f) return containerColor
    if (!isDefaultListContainerColor(containerColor, colorScheme)) return containerColor
    val adjustedAlpha = if (colorScheme.background.luminance() > 0.5f) {
        targetAlpha
    } else {
        (targetAlpha + 0.12f).coerceAtMost(0.78f)
    }
    return containerColor.copy(alpha = containerColor.alpha.coerceAtMost(adjustedAlpha))
}

private fun isDefaultListContainerColor(
    color: Color,
    colorScheme: ColorScheme
): Boolean {
    val opaqueColor = color.copy(alpha = 1f)
    return opaqueColor == colorScheme.background.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surface.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceVariant.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceContainer.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceContainerLow.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceContainerHigh.copy(alpha = 1f)
}

@Suppress("UNUSED_PARAMETER")
internal fun resolveAdaptiveSemanticIconTint(
    iconTint: Color,
    uiPreset: UiPreset,
    colorScheme: ColorScheme,
    useSemanticAccentRoles: Boolean = true,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3,
): Color {
    if (
        uiPreset != UiPreset.MD3 ||
        androidNativeVariant != AndroidNativeVariant.MATERIAL3 ||
        iconTint == Color.Unspecified
    ) {
        return iconTint
    }
    // Settings icons are navigation affordances rather than status indicators.
    // MD3 therefore uses the active theme accent consistently across the whole
    // settings hierarchy; iOS and MIUIX keep their familiar per-item colors.
    return colorScheme.primary
}

@Suppress("UNUSED_PARAMETER")
internal fun resolveAdaptivePreferenceIconContainerColor(
    iconTint: Color,
    semanticTint: Color,
    treatment: AppPreferenceIconTreatment,
): Color = semanticTint

internal fun resolveAdaptivePreferenceIconContentColor(
    containerColor: Color,
    colorScheme: ColorScheme,
): Color {
    if (containerColor == Color.Unspecified) return Color.Unspecified
    val opaqueContainer = containerColor.copy(alpha = 1f)
    return when (opaqueContainer) {
        colorScheme.primary.copy(alpha = 1f) -> colorScheme.onPrimary
        colorScheme.secondary.copy(alpha = 1f) -> colorScheme.onSecondary
        colorScheme.tertiary.copy(alpha = 1f) -> colorScheme.onTertiary
        colorScheme.error.copy(alpha = 1f) -> colorScheme.onError
        colorScheme.primaryContainer.copy(alpha = 1f) -> colorScheme.onPrimaryContainer
        colorScheme.secondaryContainer.copy(alpha = 1f) -> colorScheme.onSecondaryContainer
        colorScheme.tertiaryContainer.copy(alpha = 1f) -> colorScheme.onTertiaryContainer
        colorScheme.errorContainer.copy(alpha = 1f) -> colorScheme.onErrorContainer
        else -> if (opaqueContainer.luminance() >= 0.72f) Color.Black else Color.White
    }
}

internal fun resolveAdaptivePreferenceIconBackgroundAlpha(
    treatment: AppPreferenceIconTreatment,
    tonalAlpha: Float,
): Float = if (treatment == AppPreferenceIconTreatment.FILLED) 1f else tonalAlpha

internal fun resolveAdaptiveSwitchVisualSpec(
    uiPreset: UiPreset,
    colorScheme: ColorScheme
): AdaptiveSwitchVisualSpec {
    return if (uiPreset == UiPreset.MD3) {
        AdaptiveSwitchVisualSpec(
            usePlatformDefaults = true,
            checkedThumbColor = colorScheme.onPrimary,
            checkedTrackColor = colorScheme.primary,
            uncheckedThumbColor = colorScheme.surface,
            uncheckedTrackColor = colorScheme.surfaceVariant,
            uncheckedBorderColor = colorScheme.outline.copy(alpha = 0.55f)
        )
    } else {
        AdaptiveSwitchVisualSpec(
            usePlatformDefaults = false,
            checkedThumbColor = Color.White,
            checkedTrackColor = colorScheme.primary,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE9E9EA),
            uncheckedBorderColor = Color.Transparent
        )
    }
}

@Composable
fun rememberAdaptiveSemanticIconTint(
    iconTint: Color,
    uiPreset: UiPreset = LocalUiPreset.current,
    dynamicColorActive: Boolean = LocalDynamicColorActive.current
): Color {
    val colorScheme = MaterialTheme.colorScheme
    val androidNativeVariant = LocalAndroidNativeVariant.current
    return remember(iconTint, uiPreset, dynamicColorActive, colorScheme, androidNativeVariant) {
        resolveAdaptiveSemanticIconTint(
            iconTint = iconTint,
            uiPreset = uiPreset,
            colorScheme = colorScheme,
            useSemanticAccentRoles = dynamicColorActive,
            androidNativeVariant = androidNativeVariant,
        )
    }
}

@Composable
fun rememberAdaptivePreferenceIconContentColor(
    containerColor: Color,
): Color {
    val colorScheme = MaterialTheme.colorScheme
    return remember(containerColor, colorScheme) {
        resolveAdaptivePreferenceIconContentColor(containerColor, colorScheme)
    }
}

@Composable
fun rememberAdaptivePreferenceIconContainerColor(
    iconTint: Color,
): Color {
    val treatment = LocalAppPreferenceIconTreatment.current
    val semanticTint = rememberAdaptiveSemanticIconTint(iconTint)
    return remember(iconTint, semanticTint, treatment) {
        resolveAdaptivePreferenceIconContainerColor(iconTint, semanticTint, treatment)
    }
}

@Composable
fun AppAdaptiveSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val settingsLiquidGlassEnabled = LocalSettingsLiquidGlassEnabled.current
    val colorScheme = MaterialTheme.colorScheme
    val switchSpec = remember(uiPreset, colorScheme) {
        resolveAdaptiveSwitchVisualSpec(
            uiPreset = uiPreset,
            colorScheme = colorScheme
        )
    }
    when (
        resolveAppAdaptiveSwitchTreatment(
            uiPreset = uiPreset,
            androidNativeVariant = androidNativeVariant,
            settingsLiquidGlassEnabled = settingsLiquidGlassEnabled
        )
    ) {
        AppAdaptiveSwitchTreatment.MATERIAL -> {
            if (switchSpec.usePlatformDefaults) {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    modifier = modifier
                )
            } else {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    modifier = modifier,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = switchSpec.checkedThumbColor,
                        checkedTrackColor = switchSpec.checkedTrackColor,
                        checkedBorderColor = switchSpec.checkedTrackColor,
                        uncheckedThumbColor = switchSpec.uncheckedThumbColor,
                        uncheckedTrackColor = switchSpec.uncheckedTrackColor,
                        uncheckedBorderColor = switchSpec.uncheckedBorderColor
                    )
                )
            }
        }
        AppAdaptiveSwitchTreatment.MIUIX -> {
            MiuixSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = modifier
            )
        }
        AppAdaptiveSwitchTreatment.CUPERTINO -> {
            CupertinoSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = modifier,
                colors = CupertinoSwitchDefaults.colors(
                    thumbColor = switchSpec.checkedThumbColor,
                    checkedTrackColor = switchSpec.checkedTrackColor,
                    uncheckedTrackColor = switchSpec.uncheckedTrackColor
                )
            )
        }
        AppAdaptiveSwitchTreatment.LIQUID_GLASS -> {
            AppLiquidGlassSwitchRenderer(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun AppLiquidGlassSwitchRenderer(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val clickPulse = remember { Animatable(0f) }
    var didInitializePulse by remember { mutableStateOf(false) }
    val layoutSpec = remember { resolveLiquidSwitchLayoutSpec() }
    val motionSpec = remember { resolveLiquidSwitchMotionSpec() }
    val themePrimaryColor = MaterialTheme.colorScheme.primary
    val uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    val position by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = motionSpec.selectionSpring.toSpringSpec(),
        label = "settingsLiquidSwitchPosition"
    )
    val pressProgress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = motionSpec.pressSpring.toSpringSpec(),
        label = "settingsLiquidSwitchPress"
    )
    val trackColor = resolveLiquidSwitchTrackColor(
        checked = checked,
        themePrimaryColor = themePrimaryColor,
        uncheckedTrackColor = uncheckedTrackColor
    )
    val thumbOffsetDp by animateFloatAsState(
        targetValue = layoutSpec.checkedThumbOffsetXDp * position,
        animationSpec = motionSpec.selectionSpring.toSpringSpec(),
        label = "settingsLiquidSwitchThumbOffset"
    )
    val highlightProgress = max(pressProgress, clickPulse.value)
    val targetThumbTransform = resolveLiquidSwitchThumbTransform(highlightProgress, motionSpec)
    val thumbScaleX = targetThumbTransform.scaleX
    val thumbScaleY = targetThumbTransform.scaleY
    val thumbPulseOverscan = 2.dp * highlightProgress

    LaunchedEffect(checked) {
        if (!didInitializePulse) {
            didInitializePulse = true
            return@LaunchedEffect
        }
        clickPulse.snapTo(1f)
        clickPulse.animateTo(0f, motionSpec.indicatorScaleSpring.toSpringSpec())
    }

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .size(width = layoutSpec.containerWidthDp.dp, height = layoutSpec.containerHeightDp.dp)
            .clip(RoundedCornerShape(50))
            .clipToBounds()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = androidx.compose.ui.semantics.Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = layoutSpec.trackOffsetYDp.dp)
                .size(width = layoutSpec.trackWidthDp.dp, height = layoutSpec.trackHeightDp.dp)
                .clip(RoundedCornerShape(50))
                .background(trackColor)
                .drawBehind {
                    drawRect(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (checked) 0.18f else 0.08f),
                                Color.White.copy(alpha = 0.02f),
                                Color.Black.copy(alpha = if (checked) 0.04f else 0.02f)
                            )
                        )
                    )
                }
        )
        Box(
            modifier = Modifier
                // lambda 版：thumbOffsetDp 是开关拨动时逐帧变化的动画值，
                // 非 lambda 版会把它固化到布局阶段，每帧重新测量整个开关。
                .offset {
                    IntOffset(
                        x = (thumbOffsetDp.dp - thumbPulseOverscan).roundToPx(),
                        y = layoutSpec.thumbOffsetYDp.dp.roundToPx()
                    )
                }
                .size(
                    width = layoutSpec.thumbWidthDp.dp + thumbPulseOverscan * 2,
                    height = layoutSpec.thumbHeightDp.dp
                )
                .graphicsLayer {
                    scaleX = thumbScaleX
                    scaleY = thumbScaleY
                    shape = RoundedCornerShape(50)
                    clip = false
                }
                .clip(RoundedCornerShape(50))
                .drawBehind {
                    drawRoundRect(
                        color = Color.White,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
                    )
                }
        )
    }
}

@Composable
fun AdaptivePreferenceSectionTitleRenderer(title: String) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(
            uiPreset = uiPreset,
            androidNativeVariant = androidNativeVariant
        )
    }
    if (uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX) {
        SmallTitle(
            text = title,
            textColor = AppSurfaceTokens.onSurfaceVariantSummary(),
            insideMargin = PaddingValues(
                start = visualSpec.sectionStartPaddingDp.dp,
                top = 24.dp,
                bottom = 8.dp
            )
        )
        return
    }
    Text(
        text = if (uiPreset == UiPreset.MD3) title else title.uppercase(),
        style = if (uiPreset == UiPreset.MD3) {
            MaterialTheme.typography.titleSmall
        } else {
            MaterialTheme.typography.labelMedium
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = if (uiPreset == UiPreset.MD3) {
            0.sp
        } else {
            0.5.sp
        },
        modifier = Modifier.padding(
            start = visualSpec.sectionStartPaddingDp.dp,
            top = if (uiPreset == UiPreset.MD3) 28.dp else 24.dp,
            bottom = if (uiPreset == UiPreset.MD3) 10.dp else 8.dp
        )
    )
}

/**
 * Continuous iOS corners do not compose cleanly with [Surface] borders; use standard arcs
 * for bordered list groups so the stroke follows smooth quarter-circle corners.
 */
internal fun resolveAdaptiveGroupSurfaceShape(
    uiPreset: UiPreset,
    requestedShape: Shape?,
    defaultShape: RoundedCornerShape,
    border: BorderStroke?
): Shape {
    val resolved = requestedShape ?: defaultShape
    if (border != null && uiPreset == UiPreset.IOS && resolved !is RoundedCornerShape) {
        return defaultShape
    }
    return resolved
}

@Composable
fun AdaptivePreferenceGroupRenderer(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shape: androidx.compose.ui.graphics.Shape? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    presentation: AppPreferenceGroupPresentation = AppPreferenceGroupPresentation.CARD,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedPresentation = if (
        LocalAppPreferenceGroupPresentation.current == AppPreferenceGroupPresentation.FLAT
    ) {
        AppPreferenceGroupPresentation.FLAT
    } else {
        presentation
    }
    if (resolvedPresentation == AppPreferenceGroupPresentation.FLAT) {
        Column(modifier = modifier, content = content)
        return
    }

    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val cornerRadiusScale = LocalCornerRadiusScale.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(
            uiPreset = uiPreset,
            androidNativeVariant = androidNativeVariant
        )
    }
    val groupCornerRadius = iOSCornerRadius.Medium * cornerRadiusScale
    val colorScheme = MaterialTheme.colorScheme
    val defaultShape = RoundedCornerShape(
        if (uiPreset == UiPreset.MD3) visualSpec.groupCornerRadiusDp.dp else groupCornerRadius
    )
    val appliedShape = resolveAdaptiveGroupSurfaceShape(
        uiPreset = uiPreset,
        requestedShape = shape,
        defaultShape = defaultShape,
        border = border
    )
    val resolvedContainerColor = resolveAdaptiveGroupContainerColor(
        uiPreset = uiPreset,
        colorScheme = colorScheme,
        fallbackColor = containerColor,
        androidNativeVariant = androidNativeVariant,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    )

    if (uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX) {
        MiuixCard(
            modifier = modifier.padding(horizontal = 14.dp),
            cornerRadius = visualSpec.groupCornerRadiusDp.dp,
            insideMargin = PaddingValues(0.dp),
            colors = MiuixCardDefaults.defaultColors(color = resolvedContainerColor)
        ) {
            content()
        }
        return
    }
    
    Surface(
        modifier = modifier.padding(
            horizontal = if (uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX) {
                14.dp
            } else if (uiPreset == UiPreset.MD3) {
                12.dp
            } else {
                16.dp
            }
        ),
        shape = appliedShape,
        color = resolvedContainerColor,
        shadowElevation = 0.dp,
        tonalElevation = visualSpec.groupTonalElevationDp.dp,
        border = if (uiPreset == UiPreset.MD3) {
            androidx.compose.foundation.BorderStroke(
                0.8.dp,
                when {
                    androidNativeVariant == AndroidNativeVariant.MIUIX -> colorScheme.outline.copy(alpha = 0.22f)
                    else -> colorScheme.outlineVariant.copy(alpha = 0.6f)
                }
            )
        } else {
            border
        }
    ) {
        Column(content = content)
    }
}

@Composable
internal fun AdaptiveSwitchPreferenceContent(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(uiPreset, androidNativeVariant)
    }
    val rowSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListRowVisualSpec(uiPreset, androidNativeVariant)
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = if (iconTreatment == AppPreferenceIconTreatment.FILLED) {
        filledIconContentColor
    } else {
        effectiveIconTint
    }
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val cornerRadiusScale = LocalCornerRadiusScale.current
    val iconCornerRadius = if (uiPreset == UiPreset.MD3) visualSpec.iconCornerRadiusDp.dp else iOSCornerRadius.Small * cornerRadiusScale
    if (shouldRouteIosSwitchItemToMiuixSwitchPreference(uiPreset, androidNativeVariant)) {
        val platformHaptic = LocalHapticFeedback.current
        val effectiveHaptic = if (LocalAppThemeConfig.current.hapticFeedbackEnabled) {
            platformHaptic
        } else {
            NoOpHapticFeedback
        }
        CompositionLocalProvider(LocalHapticFeedback provides effectiveHaptic) {
            MiuixSwitchPreference(
                checked = checked,
                onCheckedChange = onCheckedChange,
                title = title,
                titleColor = BasicComponentDefaults.titleColor(color = textColor),
                summary = subtitle,
                summaryColor = BasicComponentDefaults.summaryColor(color = subtitleColor),
                enabled = enabled,
                insideMargin = PaddingValues(
                    horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                    vertical = rowSpec.insideVerticalPaddingDp.dp
                ),
                startAction = {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }
                }
            )
        }
        return
    }
    if (uiPreset == UiPreset.MD3) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = rowSpec.minTouchTargetHeightDp.dp)
                .alpha(if (enabled) 1f else 0.6f)
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(
                    horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                    vertical = rowSpec.insideVerticalPaddingDp.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(visualSpec.iconContainerSizeDp.dp)
                        .adaptiveSquircleBackground(
                            color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                            cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconContentColor,
                        modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = textColor)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
                }
            }
            Spacer(modifier = Modifier.width(rowSpec.trailingSpacingDp.dp))
            AppAdaptiveSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(visualSpec.iconContainerSizeDp.dp)
                    .adaptiveSquircleBackground(
                        color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                        cornerRadius = iconCornerRadius,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconContentColor,
                    modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = textColor)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
            }
        }
        Spacer(modifier = Modifier.width(rowSpec.trailingSpacingDp.dp))
        AppAdaptiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun AdaptiveSliderPreferenceRenderer(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(uiPreset, androidNativeVariant)
    }
    val rowSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListRowVisualSpec(uiPreset, androidNativeVariant)
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = if (iconTreatment == AppPreferenceIconTreatment.FILLED) {
        filledIconContentColor
    } else {
        effectiveIconTint
    }
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val cornerRadiusScale = LocalCornerRadiusScale.current
    val iconCornerRadius = if (uiPreset == UiPreset.MD3) {
        visualSpec.iconCornerRadiusDp.dp
    } else {
        iOSCornerRadius.Small * cornerRadiusScale
    }
    if (shouldRouteIosSliderPreferenceToMiuixSliderPreference(uiPreset, androidNativeVariant)) {
        MiuixSliderPreference(
            value = value,
            onValueChange = onValueChange,
            title = title,
            titleColor = BasicComponentDefaults.titleColor(color = textColor),
            summary = subtitle,
            summaryColor = BasicComponentDefaults.summaryColor(color = subtitleColor),
            valueText = valueLabel,
            valueRange = valueRange,
            steps = steps,
            insideMargin = PaddingValues(
                horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                vertical = rowSpec.insideVerticalPaddingDp.dp
            ),
            startAction = {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(visualSpec.iconContainerSizeDp.dp)
                            .adaptiveSquircleBackground(
                                color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconContentColor,
                            modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                        )
                    }
                }
            }
        )
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                vertical = rowSpec.insideVerticalPaddingDp.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(visualSpec.iconContainerSizeDp.dp)
                        .adaptiveSquircleBackground(
                            color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                            cornerRadius = iconCornerRadius,
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconContentColor,
                        modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = textColor)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                }
            }
            if (valueLabel != null) {
                Spacer(modifier = Modifier.width(rowSpec.trailingSpacingDp.dp))
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = valueColor
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
internal fun AdaptivePreferenceContent(
    icon: ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    copyValue: String? = null,
    onClick: (() -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    chevronTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
    centered: Boolean = false,
    enableCopy: Boolean = false,
    onCopyRequest: ((text: String, label: String?) -> Unit)? = null,
    showChevron: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(uiPreset, androidNativeVariant)
    }
    val rowSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListRowVisualSpec(uiPreset, androidNativeVariant)
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = if (iconTreatment == AppPreferenceIconTreatment.FILLED) {
        filledIconContentColor
    } else {
        effectiveIconTint
    }
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val cornerRadiusScale = LocalCornerRadiusScale.current
    val iconCornerRadius = if (uiPreset == UiPreset.MD3) visualSpec.iconCornerRadiusDp.dp else iOSCornerRadius.Small * cornerRadiusScale
    val clickableRenderer = resolveAppClickableItemRenderer(
        uiPreset = uiPreset,
        androidNativeVariant = androidNativeVariant,
        onClick = onClick,
        showChevron = showChevron,
        centered = centered
    )
    if (clickableRenderer == AppClickableItemRenderer.MIUIX_ARROW) {
        MiuixArrowPreference(
            title = title,
            titleColor = BasicComponentDefaults.titleColor(color = textColor),
            summary = subtitle,
            summaryColor = BasicComponentDefaults.summaryColor(color = subtitleColor),
            onClick = onClick,
            insideMargin = PaddingValues(
                horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                vertical = rowSpec.insideVerticalPaddingDp.dp
            ),
            startAction = {
                when {
                    icon != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }

                    iconPainter != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = if (effectiveIconTint == Color.Unspecified) {
                                        Color.Transparent
                                    } else {
                                        effectiveIconTint.copy(alpha = iconBackgroundAlpha)
                                    },
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }
                }
            },
            endActions = {
                trailingContent?.invoke()
                if (!value.isNullOrBlank()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = valueColor,
                        modifier = Modifier.onLongPressAction(
                            enabled = enableCopy && onCopyRequest != null,
                            onLongPress = { onCopyRequest?.invoke(copyValue ?: value, title) },
                        )
                    )
                }
            }
        )
        return
    }
    if (clickableRenderer == AppClickableItemRenderer.MD3_BASIC) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = rowSpec.minTouchTargetHeightDp.dp)
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(
                    horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                    vertical = rowSpec.insideVerticalPaddingDp.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null || iconPainter != null) {
                Box(
                    modifier = Modifier
                        .size(visualSpec.iconContainerSizeDp.dp)
                        .adaptiveSquircleBackground(
                            color = if (effectiveIconTint == Color.Unspecified) {
                                Color.Transparent
                            } else {
                                effectiveIconTint.copy(alpha = iconBackgroundAlpha)
                            },
                            cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconContentColor,
                            modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                        )
                    } else if (iconPainter != null) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = null,
                            tint = effectiveIconTint,
                            modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                }
            }
            if (trailingContent != null || !value.isNullOrBlank() || (onClick != null && showChevron)) {
                Spacer(modifier = Modifier.width(rowSpec.trailingSpacingDp.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    trailingContent?.invoke()
                    if (!value.isNullOrBlank()) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = valueColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            modifier = Modifier
                                .widthIn(max = 128.dp)
                                .onLongPressAction(
                                    enabled = enableCopy && onCopyRequest != null,
                                    onLongPress = { onCopyRequest?.invoke(copyValue ?: value, title) },
                                )
                        )
                    }
                    if (onClick != null && showChevron) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = chevronTint,
                            modifier = Modifier.size(rowSpec.trailingIconSizeDp.dp)
                        )
                    }
                }
            }
        }
        return
    }
    if (clickableRenderer == AppClickableItemRenderer.MIUIX_BASIC) {
        BasicComponent(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = rowSpec.minTouchTargetHeightDp.dp),
            title = title,
            summary = subtitle,
            onClick = onClick,
            insideMargin = PaddingValues(
                horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                vertical = rowSpec.insideVerticalPaddingDp.dp
            ),
            startAction = {
                when {
                    icon != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }

                    iconPainter != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = if (effectiveIconTint == Color.Unspecified) {
                                        Color.Transparent
                                    } else {
                                        effectiveIconTint.copy(alpha = iconBackgroundAlpha)
                                    },
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }
                }
            },
            endActions = {
                trailingContent?.invoke()
                if (!value.isNullOrBlank()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppSurfaceTokens.onSurfaceVariantSummary(),
                        modifier = Modifier.onLongPressAction(
                            enabled = enableCopy && onCopyRequest != null,
                            onLongPress = { onCopyRequest?.invoke(copyValue ?: value, title) },
                        )
                    )
                    Spacer(modifier = Modifier.width(rowSpec.trailingSpacingDp.dp))
                }
                if (onClick != null && showChevron) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppSurfaceTokens.onSurfaceVariantActions(),
                        modifier = Modifier.size(rowSpec.trailingIconSizeDp.dp)
                    )
                }
            }
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start
    ) {
        if (!centered) {
            if (icon != null || iconPainter != null) {
                if (effectiveIconTint != Color.Unspecified) {
                    Box(
                        modifier = Modifier
                            .size(visualSpec.iconContainerSizeDp.dp)
                            .adaptiveSquircleBackground(
                                color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                cornerRadius = iconCornerRadius,
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        } else if (iconPainter != null) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.size(visualSpec.iconContainerSizeDp.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(visualSpec.iconContainerSizeDp.dp)
                            )
                        } else if (iconPainter != null) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(visualSpec.iconContainerSizeDp.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
            }
        }
        
        if (centered) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    maxLines = if (subtitle != null) 2 else 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        if (!centered) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                trailingContent?.invoke()
                if (value != null) {
                    Text(
                        text = value, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = valueColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.onLongPressAction(
                            enabled = enableCopy && onCopyRequest != null,
                            onLongPress = { onCopyRequest?.invoke(copyValue ?: value, title) },
                        )
                    )
                }
                if (onClick != null && showChevron) {
                    Spacer(modifier = Modifier.width(6.dp))
                    if (uiPreset == UiPreset.MD3) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = chevronTint,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(CupertinoIcons.Default.ChevronForward, null, tint = chevronTint, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdaptivePreferenceDividerRenderer(
    modifier: Modifier = Modifier,
    startIndent: androidx.compose.ui.unit.Dp = 66.dp
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(uiPreset, androidNativeVariant)
    }
    if (visualSpec.dividerThicknessDp <= 0f) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (uiPreset == UiPreset.MD3) visualSpec.dividerStartIndentDp.dp else startIndent)
            .height(visualSpec.dividerThicknessDp.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) // Subtle separator
    )
}


@Composable
fun AdaptivePreferenceGridItemRenderer(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(uiPreset, androidNativeVariant)
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = if (iconTreatment == AppPreferenceIconTreatment.FILLED) {
        filledIconContentColor
    } else {
        effectiveIconTint
    }
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val cornerRadiusScale = LocalCornerRadiusScale.current
    val itemCornerRadius = if (uiPreset == UiPreset.MD3) visualSpec.gridCornerRadiusDp.dp else iOSCornerRadius.Medium * cornerRadiusScale
    val resolvedContainerColor = resolveGlobalWallpaperListContainerColor(
        containerColor = containerColor,
        colorScheme = MaterialTheme.colorScheme,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current,
        targetAlpha = 0.62f
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(itemCornerRadius))
            .background(resolvedContainerColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .adaptiveSquircleBackground(
                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                    cornerRadius = iOSCornerRadius.Small * cornerRadiusScale,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconContentColor,
                modifier = Modifier.size(26.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AdaptiveSearchFieldRenderer(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    heightOverride: Dp? = null,
    forceExpandedInput: Boolean = false,
    topBarChrome: Boolean = false,
    onSearch: () -> Unit = {},
    onClear: () -> Unit = { onQueryChange("") },
    showClearAction: Boolean = true,
    autoFocusEnabled: Boolean = forceExpandedInput,
    focusRequester: FocusRequester? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val colorScheme = MaterialTheme.colorScheme
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveListComponentVisualSpec(
            uiPreset = uiPreset,
            androidNativeVariant = androidNativeVariant
        )
    }
    val cornerRadiusScale = LocalCornerRadiusScale.current
    val searchBarCornerRadius = if (uiPreset == UiPreset.MD3) visualSpec.searchBarCornerRadiusDp.dp else iOSCornerRadius.Small * cornerRadiusScale
    val resolvedContainerColor = resolveAdaptiveSearchBarContainerColor(
        uiPreset = uiPreset,
        colorScheme = colorScheme,
        fallbackColor = containerColor,
        androidNativeVariant = androidNativeVariant,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    )
    val resolvedHeight = heightOverride ?: visualSpec.searchBarHeightDp.dp

    if (forceExpandedInput) {
        val fallbackFocusRequester = remember { FocusRequester() }
        val resolvedFocusRequester = focusRequester ?: fallbackFocusRequester
        LaunchedEffect(resolvedFocusRequester, autoFocusEnabled) {
            if (autoFocusEnabled) {
                delay(80)
                runCatching { resolvedFocusRequester.requestFocus() }
            }
        }
        val focusModifier = Modifier.focusRequester(resolvedFocusRequester)
        if (shouldUseNativeMiuixSearchBar(uiPreset, androidNativeVariant)) {
            MiuixAdaptiveSearchBar(
                query = query,
                onQueryChange = onQueryChange,
                modifier = modifier.then(focusModifier),
                placeholder = placeholder,
                containerColor = resolvedContainerColor,
                height = resolvedHeight,
                forceExpandedInput = true,
                onSearch = onSearch,
                interactionSource = interactionSource,
            )
            return
        }
        // 顶栏固定高度不能用 OutlinedTextField：默认 contentPadding 会把字裁掉（平板尤其明显）。
        // 用 BasicTextField + 可选聚焦描边，保证 44–56dp 内文字完整可见。
        if (topBarChrome) {
            val textStyle = MaterialTheme.typography.bodyLarge
            val showLeadingIcon = uiPreset == UiPreset.IOS
            val resolvedInteraction = interactionSource ?: remember { MutableInteractionSource() }
            val isFocused by resolvedInteraction.collectIsFocusedAsState()
            val fieldShape = RoundedCornerShape(searchBarCornerRadius)
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = modifier
                    .fillMaxWidth()
                    .height(resolvedHeight)
                    .clip(fieldShape)
                    .background(resolvedContainerColor, fieldShape)
                    .then(
                        if (isFocused) {
                            Modifier.border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = fieldShape,
                            )
                        } else {
                            Modifier
                        }
                    )
                    .then(focusModifier),
                textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                interactionSource = resolvedInteraction,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                    ) {
                        if (showLeadingIcon) {
                            Icon(
                                imageVector = CupertinoIcons.Default.MagnifyingGlass,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.weight(1f),
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    style = textStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                        if (showClearAction && query.isNotEmpty()) {
                            IconButton(
                                onClick = onClear,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                },
            )
            return
        }
        if (uiPreset == UiPreset.MD3) {
            val textStyle = MaterialTheme.typography.bodyMedium
            val sizeModifier = Modifier
                .fillMaxWidth()
                .heightIn(min = resolvedHeight)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = modifier
                    .then(sizeModifier)
                    .then(focusModifier),
                placeholder = {
                    Text(
                        text = placeholder,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = textStyle,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = if (showClearAction && query.isNotEmpty()) {
                    {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                textStyle = textStyle.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                shape = RoundedCornerShape(searchBarCornerRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = resolvedContainerColor,
                    unfocusedContainerColor = resolvedContainerColor,
                    disabledContainerColor = resolvedContainerColor,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                interactionSource = interactionSource,
            )
            return
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier
                .fillMaxWidth()
                .height(resolvedHeight)
                .clip(RoundedCornerShape(searchBarCornerRadius))
                .background(resolvedContainerColor)
                .then(focusModifier),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Default.MagnifyingGlass,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                    if (showClearAction && query.isNotEmpty()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                imageVector = CupertinoIcons.Default.XmarkCircle,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            },
        )
        return
    }

    if (shouldUseNativeMiuixSearchBar(uiPreset, androidNativeVariant)) {
        MiuixAdaptiveSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = modifier,
            placeholder = placeholder,
            containerColor = resolvedContainerColor,
            height = resolvedHeight,
            forceExpandedInput = forceExpandedInput,
            onSearch = { onSearch() },
            interactionSource = interactionSource,
        )
        return
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(resolvedHeight)
            .clip(RoundedCornerShape(searchBarCornerRadius))
            .background(resolvedContainerColor),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                if (uiPreset == UiPreset.MD3) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = CupertinoIcons.Default.MagnifyingGlass,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
                if (showClearAction && query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(20.dp)
                    ) {
                        val clearIcon = if (uiPreset == UiPreset.MD3) {
                            Icons.Default.Clear
                        } else {
                            CupertinoIcons.Default.XmarkCircle
                        }
                        Icon(
                            imageVector = clearIcon,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AppSearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val colorScheme = MaterialTheme.colorScheme
    val visualSpec = rememberAdaptiveListVisualCapabilities().componentSpec
    val resolvedContainerColor = resolveAdaptiveSearchBarContainerColor(
        uiPreset = uiPreset,
        colorScheme = colorScheme,
        fallbackColor = containerColor,
        androidNativeVariant = androidNativeVariant,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current,
    )
    val cornerRadius = if (uiPreset == UiPreset.MD3) {
        visualSpec.searchBarCornerRadiusDp.dp
    } else {
        iOSCornerRadius.Small * LocalCornerRadiusScale.current
    }
    val searchIcon = if (uiPreset == UiPreset.MD3) {
        Icons.Default.Search
    } else {
        CupertinoIcons.Default.MagnifyingGlass
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = visualSpec.searchBarHeightDp.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(resolvedContainerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = searchIcon,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AdaptiveTextFieldRenderer(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    if (shouldUseNativeMiuixSearchBar(uiPreset, androidNativeVariant)) {
        Column(modifier = modifier.fillMaxWidth()) {
            MiuixTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = label ?: placeholder.orEmpty(),
                useLabelAsPlaceholder = label == null,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                colors = MiuixTextFieldDefaults.textFieldColors(
                    borderColor = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MiuixTheme.colorScheme.primary
                    },
                ),
            )
            supportingText?.invoke()
        }
        return
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        supportingText = supportingText
    )
}

@Composable
private fun MiuixAdaptiveSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    @Suppress("UNUSED_PARAMETER") containerColor: Color,
    height: androidx.compose.ui.unit.Dp,
    forceExpandedInput: Boolean = false,
    onSearch: () -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
) {
    if (forceExpandedInput) {
        InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { onSearch() },
            expanded = true,
            onExpandedChange = {},
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            label = placeholder,
            interactionSource = interactionSource,
        )
        return
    }
    var expanded by rememberSaveable(query.isNotBlank()) {
        mutableStateOf(query.isNotBlank())
    }
    InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onSearch() },
        expanded = expanded || query.isNotBlank(),
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        label = placeholder,
        interactionSource = interactionSource,
    )
}
