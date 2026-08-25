// File: feature/video/ui/components/QualityMenu.kt
package com.android.purebilibili.feature.video.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import com.android.purebilibili.core.ui.components.AppHorizontalDivider
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

/** Minimum touch target for quality/speed rows (Material accessibility). */
private val QualityMenuRowMinHeight = 48.dp

/**
 * Quality Selection Menu
 *
 * Displays a menu for selecting video quality.
 *
 * Requirement Reference: AC2.3 - Reusable quality menu
 */
@Composable
fun QualitySelectionMenu(
    qualities: List<String>,
    qualityIds: List<Int> = emptyList(),
    switchableQualityIds: List<Int> = emptyList(),
    currentQuality: String,
    isLoggedIn: Boolean = false,
    isVip: Boolean = false,
    onQualitySelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    useDialog: Boolean = false
) {
    fun getQualityTag(qualityId: Int): String? {
        return when (qualityId) {
            129, 127, 126, 125, 120 -> if (!isVip) "大会员" else null
            116, 112, 100 -> if (!isVip) "大会员" else null
            80 -> if (!isLoggedIn) "登录" else null
            else -> null
        }
    }

    fun isQualityAvailable(qualityId: Int): Boolean {
        return when {
            qualityId == 100 || qualityId >= 112 -> isVip
            qualityId >= 80 -> isLoggedIn
            else -> true
        }
    }

    fun hasSwitchableTrack(qualityId: Int): Boolean {
        return qualityId in switchableQualityIds
    }

    val menuContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AppSurface(
                modifier = Modifier
                    .widthIn(min = 220.dp, max = 300.dp)
                    .heightIn(max = 440.dp)
                    .clip(AppShapes.container(ContainerLevel.Card))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                color = Color(0xFF2B2B2B),
                shape = AppShapes.container(ContainerLevel.Card),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AppText(
                        text = "画质选择",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    AppHorizontalDivider(color = Color.White.copy(0.1f))
                    qualities.forEachIndexed { index, quality ->
                        val isSelected = quality == currentQuality
                        val qualityId = qualityIds.getOrNull(index) ?: 0
                        val tag = getQualityTag(qualityId)
                        val hasPermission = isQualityAvailable(qualityId)
                        val isSwitchable = hasSwitchableTrack(qualityId)
                        val isEnabled = hasPermission && isSwitchable

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = QualityMenuRowMinHeight)
                                .clickable(
                                    enabled = isEnabled && !isSelected,
                                    role = Role.Button,
                                    onClick = { onQualitySelected(index) }
                                )
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(
                                text = quality,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    !isEnabled -> Color.White.copy(0.4f)
                                    else -> Color.White.copy(0.9f)
                                },
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            if (tag != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AppSurface(
                                    color = if (tag == "大会员") {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color(0xFF666666)
                                    },
                                    shape = AppShapes.container(ContainerLevel.Tag)
                                ) {
                                    AppText(
                                        text = tag,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            if (isSelected) {
                                AppIcon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (useDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            menuContent()
        }
    } else {
        menuContent()
    }
}

/**
 * Speed Selection Menu
 *
 * Displays a menu for selecting playback speed.
 */
enum class SpeedSelectionMenuPlacement {
    CENTER,
    RIGHT_SIDE
}

@Composable
fun SpeedSelectionMenu(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
    placement: SpeedSelectionMenuPlacement
) {
    val speedOptions = PlaybackSpeed.OPTIONS.asReversed()
    val contentAlignment = when (placement) {
        SpeedSelectionMenuPlacement.CENTER -> Alignment.Center
        SpeedSelectionMenuPlacement.RIGHT_SIDE -> Alignment.CenterEnd
    }
    val scrimColor = when (placement) {
        SpeedSelectionMenuPlacement.CENTER -> Color.Black.copy(alpha = 0.5f)
        SpeedSelectionMenuPlacement.RIGHT_SIDE -> Color.Transparent
    }

    val menuContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = contentAlignment
        ) {
            AppSurface(
                modifier = Modifier
                    .then(
                        if (placement == SpeedSelectionMenuPlacement.RIGHT_SIDE) {
                            Modifier.padding(end = 24.dp)
                        } else {
                            Modifier
                        }
                    )
                    .widthIn(min = 200.dp, max = 260.dp)
                    .heightIn(max = 440.dp)
                    .clip(AppShapes.container(ContainerLevel.Card))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                color = Color(0xFF2B2B2B),
                shape = AppShapes.container(ContainerLevel.Card),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AppText(
                        text = "播放速度",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    AppHorizontalDivider(color = Color.White.copy(0.1f))
                    speedOptions.forEach { speed ->
                        val isSelected = speed == currentSpeed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = QualityMenuRowMinHeight)
                                .clickable(
                                    role = Role.Button,
                                    onClick = { onSpeedSelected(speed) }
                                )
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(
                                text = if (speed == 1.0f) "正常" else "${speed}x",
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.White.copy(0.9f)
                                },
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                AppIcon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    menuContent()
}

@Composable
fun SpeedSelectionMenuDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
    placement: SpeedSelectionMenuPlacement = SpeedSelectionMenuPlacement.CENTER
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SpeedSelectionMenu(
            currentSpeed = currentSpeed,
            onSpeedSelected = onSpeedSelected,
            onDismiss = onDismiss,
            placement = placement
        )
    }
}
