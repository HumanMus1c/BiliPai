package com.android.purebilibili.feature.plugin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.components.AppButton
import androidx.compose.material3.ButtonDefaults
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.plugin.PluginManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

@Composable
fun EyeProtectionOverlay() {
    val plugins by PluginManager.pluginsFlow.collectAsStateWithLifecycle()
    val pluginInfo = plugins.find { it.plugin.id == "eye_protection" } ?: return
    val plugin = pluginInfo.plugin as? EyeProtectionPlugin ?: return
    val pluginEnabled = pluginInfo.enabled
    val settingsPreviewEnabled by plugin.settingsPreviewEnabled.collectAsStateWithLifecycle()
    if (!pluginEnabled && !settingsPreviewEnabled) return

    val isNightModeActive by plugin.isNightModeActive.collectAsStateWithLifecycle()
    val brightnessLevel by plugin.brightnessLevel.collectAsStateWithLifecycle()
    val warmFilterStrength by plugin.warmFilterStrength.collectAsStateWithLifecycle()
    val careReminder by plugin.careReminder.collectAsStateWithLifecycle()

    val darknessAlpha by animateFloatAsState(
        targetValue = (1f - brightnessLevel).coerceIn(0f, 0.7f),
        label = "eye_darkness"
    )
    val warmTopAlpha by animateFloatAsState(
        targetValue = warmFilterStrength * 0.3f,
        label = "eye_warm_top"
    )
    val warmBottomAlpha by animateFloatAsState(
        targetValue = warmFilterStrength * 0.2f,
        label = "eye_warm_bottom"
    )

    AnimatedVisibility(
        visible = isNightModeActive || settingsPreviewEnabled,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = Color.Black.copy(alpha = darknessAlpha))
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF9800).copy(alpha = warmTopAlpha),
                            Color(0xFFFF5722).copy(alpha = warmBottomAlpha)
                        )
                    )
                )
            }
        }
    }

    careReminder?.let { reminder ->
        RestReminderDialog(
            reminder = reminder,
            snoozeMinutes = plugin.getSnoozeMinutes(),
            onDismiss = { plugin.dismissReminder() },
            onSnooze = { plugin.snoozeReminder() },
            onRest = { plugin.confirmRest() }
        )
    }
}

@Composable
private fun RestReminderDialog(
    reminder: EyeCareReminder,
    snoozeMinutes: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onRest: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val layoutPolicy = remember(configuration.screenHeightDp) {
        resolveEyeReminderDialogLayoutPolicy(screenHeightDp = configuration.screenHeightDp)
    }
    AppAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(layoutPolicy.maxHeightFraction),
        shape = AppShapes.container(ContainerLevel.Floating),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color(0xFF7E57C2).copy(alpha = 0.15f),
                        shape = AppShapes.container(ContainerLevel.Card)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = Color(0xFF7E57C2),
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        title = {
            AppText(
                text = reminder.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AppText(
                    text = "你已连续观看 ${reminder.usageMinutes} 分钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppText(
                    text = reminder.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppText(
                    text = reminder.suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            AppButton(
                onClick = onRest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
                shape = AppShapes.container(ContainerLevel.Card),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppText("我去休息 20 秒", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            if (layoutPolicy.useCompactSecondaryActions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppTextButton(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f)
                    ) {
                        AppText("${snoozeMinutes} 分钟后提醒")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    AppTextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        AppText(
                            "先继续观看",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AppTextButton(
                        onClick = onSnooze,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppText("${snoozeMinutes} 分钟后提醒")
                    }
                    AppTextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppText(
                            "先继续观看",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}
