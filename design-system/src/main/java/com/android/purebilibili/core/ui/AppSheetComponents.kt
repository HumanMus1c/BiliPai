package com.android.purebilibili.core.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.theme.iOSSystemGray4
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import com.android.purebilibili.core.ui.motion.AppMotionTokens

data class AdaptiveBottomSheetVisualSpec(
    val cornerRadiusDp: Int,
    val useMaterialDragHandle: Boolean
)

internal data class AdaptiveBottomSheetMotionSpec(
    val scrimEnterDurationMillis: Int,
    val scrimExitDurationMillis: Int,
    val contentEnterFadeDurationMillis: Int,
    val contentExitFadeDurationMillis: Int
)

fun resolveAdaptiveBottomSheetVisualSpec(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): AdaptiveBottomSheetVisualSpec {
    val cornerLevel = if (uiPreset == UiPreset.MD3) {
        ContainerLevel.Pill
    } else {
        ContainerLevel.Dialog
    }
    val cornerRadiusDp = AppShapes.resolveContainerCornerDp(
        level = cornerLevel,
        uiPreset = uiPreset,
        androidNativeVariant = androidNativeVariant
    ).value.toInt()
    return AdaptiveBottomSheetVisualSpec(
        cornerRadiusDp = cornerRadiusDp,
        useMaterialDragHandle = uiPreset == UiPreset.MD3
    )
}

internal fun resolveAdaptiveBottomSheetMotionSpec(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): AdaptiveBottomSheetMotionSpec {
    val tokens = resolveAndroidNativeChromeTokens(uiPreset, androidNativeVariant)
    return AdaptiveBottomSheetMotionSpec(
        scrimEnterDurationMillis = tokens.motionEmphasizedMillis,
        scrimExitDurationMillis = tokens.expressiveMotionDurationMillis,
        contentEnterFadeDurationMillis = tokens.motionEmphasizedMillis,
        contentExitFadeDurationMillis = tokens.expressiveMotionDurationMillis
    )
}

internal fun bottomSheetScrimEnterTransition(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): EnterTransition = fadeIn(
    AppMotionTokens.resolveBottomSheetFadeEnterSpec(uiPreset, androidNativeVariant)
)

internal fun bottomSheetScrimExitTransition(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): ExitTransition = fadeOut(
    AppMotionTokens.resolveBottomSheetFadeExitSpec(uiPreset, androidNativeVariant)
)

internal fun bottomSheetContentEnterTransition(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = AppMotionTokens.resolveBottomSheetSlideSpec(uiPreset, androidNativeVariant)
    ) + fadeIn(
        AppMotionTokens.resolveBottomSheetFadeEnterSpec(uiPreset, androidNativeVariant)
    )
}

internal fun bottomSheetContentExitTransition(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = AppMotionTokens.resolveBottomSheetSlideExitSpec(
            uiPreset,
            androidNativeVariant
        )
    ) + fadeOut(
        AppMotionTokens.resolveBottomSheetFadeExitSpec<Float>(
            uiPreset,
            androidNativeVariant
        )
    )
}

/**
 * iOS-style Modal Bottom Sheet wrapper.
 * Uses Material3 ModalBottomSheet but styled to match iOS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    presentationProgress: Float = 1f,
    dragHandle: @Composable (() -> Unit)? = { AppBottomSheetDragHandle() },
    windowInsets: androidx.compose.foundation.layout.WindowInsets = androidx.compose.material3.BottomSheetDefaults.modalWindowInsets,
    content: @Composable ColumnScope.() -> Unit
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val visualSpec = remember(uiPreset, androidNativeVariant) {
        resolveAdaptiveBottomSheetVisualSpec(uiPreset, androidNativeVariant)
    }
    val sheetShape = remember(visualSpec, uiPreset) {
        if (shouldUseIosContinuousRounding(uiPreset)) {
            IosContinuousRoundedCornerShape(
                topStart = visualSpec.cornerRadiusDp.dp,
                topEnd = visualSpec.cornerRadiusDp.dp
            )
        } else {
            RoundedCornerShape(
                topStart = visualSpec.cornerRadiusDp.dp,
                topEnd = visualSpec.cornerRadiusDp.dp
            )
        }
    }
    val progressVisual = resolveInteractiveOverlayProgressVisual(
        presentationProgress = presentationProgress,
        surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
        blurActive = true,
        maxScrimAlpha = scrimColor.alpha
    )
    val resolvedContainerColor = if (uiPreset == UiPreset.MD3) {
        if (isNativeMiuixEnabled(uiPreset, androidNativeVariant)) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    } else {
        containerColor
    }.let { color ->
        color.copy(alpha = color.alpha * progressVisual.surfaceAlphaMultiplier)
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = sheetShape,
        containerColor = resolvedContainerColor,
        contentColor = contentColor,
        scrimColor = scrimColor.copy(alpha = progressVisual.scrimAlpha),
        dragHandle = if (visualSpec.useMaterialDragHandle) {
            if (isNativeMiuixEnabled(uiPreset, androidNativeVariant)) {
                { AppBottomSheetDragHandle() }
            } else {
                { BottomSheetDefaults.DragHandle() }
            }
        } else {
            dragHandle
        },
        contentWindowInsets = { windowInsets },
        content = {
            content()
        }
    )
}

data class AppBottomSheetMotion(
    val scrimEnter: EnterTransition,
    val scrimExit: ExitTransition,
    val contentEnter: EnterTransition,
    val contentExit: ExitTransition,
    val scrimEnterDurationMillis: Int,
    val scrimExitDurationMillis: Int,
    val contentEnterFadeDurationMillis: Int,
    val contentExitFadeDurationMillis: Int,
)

@Composable
fun rememberAppBottomSheetMotion(): AppBottomSheetMotion {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    return remember(uiPreset, androidNativeVariant) {
        val motionSpec = resolveAdaptiveBottomSheetMotionSpec(uiPreset, androidNativeVariant)
        AppBottomSheetMotion(
            scrimEnter = bottomSheetScrimEnterTransition(uiPreset, androidNativeVariant),
            scrimExit = bottomSheetScrimExitTransition(uiPreset, androidNativeVariant),
            contentEnter = bottomSheetContentEnterTransition(uiPreset, androidNativeVariant),
            contentExit = bottomSheetContentExitTransition(uiPreset, androidNativeVariant),
            scrimEnterDurationMillis = motionSpec.scrimEnterDurationMillis,
            scrimExitDurationMillis = motionSpec.scrimExitDurationMillis,
            contentEnterFadeDurationMillis = motionSpec.contentEnterFadeDurationMillis,
            contentExitFadeDurationMillis = motionSpec.contentExitFadeDurationMillis,
        )
    }
}

@Composable
fun AppBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(iOSSystemGray4.copy(alpha = 0.4f))
        )
    }
}
