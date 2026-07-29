package com.android.purebilibili.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.theme.UiPreset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.purebilibili.core.theme.iOSBlue
import top.yukonga.miuix.kmp.overlay.OverlayDialog

enum class AppAlertDialogRenderer {
    CUPERTINO_LOCAL,
    MATERIAL_ALERT,
    LOCAL_DIALOG,
    MIUIX_OVERLAY
}

fun resolveAppAlertDialogRenderer(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): AppAlertDialogRenderer = when {
    // 设置等页对话框常在 AdaptiveScaffold 外层组合；OverlayDialog 依赖 Miuix Scaffold
    // 的 DialogStates host，否则点击后状态变了但弹窗不可见。改用窗口 Dialog 承载。
    uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX ->
        AppAlertDialogRenderer.LOCAL_DIALOG
    uiPreset == UiPreset.MD3 -> AppAlertDialogRenderer.MATERIAL_ALERT
    else -> AppAlertDialogRenderer.CUPERTINO_LOCAL
}

data class DialogActionLayoutPolicy(
    val expandToContainer: Boolean
)

fun resolveDialogActionLayoutPolicy(
    uiPreset: UiPreset
): DialogActionLayoutPolicy {
    return DialogActionLayoutPolicy(
        expandToContainer = uiPreset == UiPreset.IOS
    )
}

/**
 * iOS-style Alert Dialog.
 * Mimics the look of standard iOS UIAlertController (Alert style).
 */
@Composable
internal fun AdaptiveAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape? = null,
    containerColor: Color? = null,
    tonalElevation: Dp? = null,
    presentationProgress: Float = 1f,
    properties: DialogProperties = DialogProperties()
) {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    when (resolveAppAlertDialogRenderer(uiPreset, androidNativeVariant)) {
        AppAlertDialogRenderer.MIUIX_OVERLAY -> {
            OverlayDialog(
                show = true,
                onDismissRequest = onDismissRequest,
            ) {
                MiuixAlertDialogBody(
                    icon = icon,
                    title = title,
                    text = text,
                    confirmButton = confirmButton,
                    dismissButton = dismissButton,
                )
            }
            return
        }
        AppAlertDialogRenderer.LOCAL_DIALOG -> {
            Dialog(
                onDismissRequest = onDismissRequest,
                properties = properties
            ) {
                Surface(
                    modifier = modifier.widthIn(min = 280.dp, max = 360.dp),
                    shape = shape ?: MaterialTheme.shapes.extraLarge,
                    color = containerColor ?: AppSurfaceTokens.cardContainer(),
                    tonalElevation = tonalElevation ?: 6.dp,
                ) {
                    MiuixAlertDialogBody(
                        icon = icon,
                        title = title,
                        text = text,
                        confirmButton = confirmButton,
                        dismissButton = dismissButton,
                    )
                }
            }
            return
        }
        AppAlertDialogRenderer.MATERIAL_ALERT -> {
            AlertDialog(
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                icon = icon,
                title = title,
                text = text,
                confirmButton = { confirmButton?.invoke() ?: Spacer(modifier = Modifier) },
                dismissButton = dismissButton,
                properties = properties,
                shape = shape ?: MaterialTheme.shapes.extraLarge,
                containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
                tonalElevation = tonalElevation ?: AlertDialogDefaults.TonalElevation,
            )
            return
        }
        AppAlertDialogRenderer.CUPERTINO_LOCAL -> Unit
    }

    val progressVisual = resolveInteractiveOverlayProgressVisual(
        presentationProgress = presentationProgress,
        surfaceType = InteractiveOverlaySurfaceType.DIALOG,
        blurActive = false,
        maxScrimAlpha = 0f
    )
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier.width(270.dp), // Standard iOS Alert width
            shape = shape ?: RoundedCornerShape(14.dp),
            color = containerColor ?: MaterialTheme.colorScheme.surface.copy(
                alpha = 0.95f * progressVisual.surfaceAlphaMultiplier
            ),
            tonalElevation = tonalElevation ?: 0.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title and Text Content
                Column(
                    modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (icon != null) {
                        Box(contentAlignment = Alignment.Center) {
                            icon()
                        }
                        if (title != null || text != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    if (title != null) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                title()
                            }
                        }
                    }
                    
                    if (title != null && text != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (text != null) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                text()
                            }
                        }
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
                
                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min) // Ensure equal height
                ) {
                    if (dismissButton != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProvideTextStyle(
                                value = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Normal, // Dismiss is usually normal
                                    color = iOSBlue
                                )
                            ) {
                                dismissButton()
                            }
                        }
                    }
                    
                    if (dismissButton != null && confirmButton != null) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), 
                            thickness = 0.5.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                    
                    if (confirmButton != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProvideTextStyle(
                                value = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold, // Confirm is usually bold
                                    color = iOSBlue
                                )
                            ) {
                                confirmButton()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixAlertDialogBody(
    icon: @Composable (() -> Unit)?,
    title: @Composable (() -> Unit)?,
    text: @Composable (() -> Unit)?,
    confirmButton: @Composable (() -> Unit)?,
    dismissButton: @Composable (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
        }
        if (title != null) {
            Box(
                modifier = Modifier.padding(
                    top = if (icon != null) 12.dp else 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                contentAlignment = Alignment.Center
            ) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    title()
                }
            }
        }
        if (text != null) {
            Box(
                modifier = Modifier.padding(
                    top = if (title != null) 8.dp else 12.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 12.dp
                ),
                contentAlignment = Alignment.Center
            ) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    text()
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (dismissButton != null) {
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    dismissButton()
                }
            }
            if (confirmButton != null) {
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    confirmButton()
                }
            }
        }
    }
}

/**
 * A helper for buttons inside the adaptive alert dialog if you need absolute control,
 * basically a wrapper that removes TextButton padding issues if standard TextButton is used.
 * But actually providing TextStyle above might be enough for simple Text() children.
 * If user passes TextButton, we might need to conform it.
 */
@Composable
internal fun AdaptiveDialogAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val layoutPolicy = resolveDialogActionLayoutPolicy(LocalUiPreset.current)
    Box(
        modifier = modifier
            .then(
                if (layoutPolicy.expandToContainer) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 40.dp)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
