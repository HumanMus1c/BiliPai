// 文件路径: feature/dynamic/components/DynamicTopBar.kt
package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppDropdownMenu
import com.android.purebilibili.core.ui.components.AppDropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
//  Material Icons
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.rememberAppGridLayoutIcon
import com.android.purebilibili.core.ui.rememberAppListLayoutIcon
import com.android.purebilibili.feature.dynamic.resolveDynamicTopBarHorizontalPadding
import com.android.purebilibili.feature.dynamic.resolveDynamicTopBarLiquidTabSpec
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.android.purebilibili.feature.home.components.DynamicPublishSkinDecoration
import coil.compose.AsyncImage
import java.io.File
import com.android.purebilibili.feature.home.components.biliPaiFloatingDockShell
import com.android.purebilibili.feature.home.components.resolveLiquidGlassTuning
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.Backdrop

//  动态页面布局模式
enum class DynamicDisplayMode {
    SIDEBAR,
    SIDEBAR_RIGHT,
    HORIZONTAL,
    DRAWER_LEFT,
    DRAWER_RIGHT
}

internal fun DynamicDisplayMode.isHorizontalUserList(): Boolean = this == DynamicDisplayMode.HORIZONTAL

internal fun DynamicDisplayMode.isFixedSidebar(): Boolean =
    this == DynamicDisplayMode.SIDEBAR || this == DynamicDisplayMode.SIDEBAR_RIGHT

internal fun DynamicDisplayMode.isRightAligned(): Boolean =
    this == DynamicDisplayMode.SIDEBAR_RIGHT || this == DynamicDisplayMode.DRAWER_RIGHT

internal fun DynamicDisplayMode.isDrawer(): Boolean =
    this == DynamicDisplayMode.DRAWER_LEFT || this == DynamicDisplayMode.DRAWER_RIGHT

internal fun resolveDynamicDisplayModeLabel(mode: DynamicDisplayMode): String = when (mode) {
    DynamicDisplayMode.SIDEBAR -> "左侧竖条"
    DynamicDisplayMode.SIDEBAR_RIGHT -> "右侧竖条"
    DynamicDisplayMode.HORIZONTAL -> "顶部横条"
    DynamicDisplayMode.DRAWER_LEFT -> "左侧抽屉"
    DynamicDisplayMode.DRAWER_RIGHT -> "右侧抽屉"
}

/**
 *  带Tab的顶栏
 */
@Composable
fun DynamicTopBarWithTabs(
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    displayMode: DynamicDisplayMode = DynamicDisplayMode.SIDEBAR,
    onDisplayModeChange: (DynamicDisplayMode) -> Unit = {},
    onPublishClick: (() -> Unit)? = null,
    publishSkinDecoration: DynamicPublishSkinDecoration? = null,
    dockBackdrop: Backdrop? = null,
    indicatorPositionProvider: (() -> Float)? = null,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density).let { with(density) { it.toDp() } }
    val liquidTabSpec = resolveDynamicTopBarLiquidTabSpec()
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val liquidGlassEnabled = homeSettings.androidNativeLiquidGlassEnabled
    val liquidGlassTuning = remember(
        homeSettings.liquidGlassProgress,
        homeSettings.liquidGlassAdvancedSettings,
        homeSettings.liquidGlassReadabilityMode,
    ) {
        resolveLiquidGlassTuning(
            progress = homeSettings.liquidGlassProgress,
            advancedSettings = homeSettings.liquidGlassAdvancedSettings,
            readabilityMode = homeSettings.liquidGlassReadabilityMode,
        )
    }
    val dockShape = AppShapes.container(ContainerLevel.Pill)
    val dockColor = AppSurfaceTokens.surfaceContainerHigh()

    Column(
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.height(statusBarHeight))

        // 顶部不再铺设整块背景；两个悬浮 Dock 独立读取内容 Backdrop 和全局玻璃预设。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(liquidTabSpec.heightDp.dp)
                .padding(horizontal = resolveDynamicTopBarHorizontalPadding()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomBarLiquidSegmentedControl(
                items = tabs,
                selectedIndex = selectedTab,
                onSelected = onTabSelected,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (liquidGlassEnabled) {
                            Modifier
                        } else {
                            Modifier
                                .clip(dockShape)
                                .background(dockColor)
                        }
                    ),
                height = liquidTabSpec.heightDp.dp,
                indicatorHeight = liquidTabSpec.indicatorHeightDp.dp,
                labelFontSize = liquidTabSpec.labelFontSizeSp.sp,
                indicatorPositionProvider = indicatorPositionProvider,
                isScrollInProgressProvider = { false },
                forceLiquidChrome = liquidGlassEnabled,
                liquidGlassEffectsEnabled = liquidGlassEnabled,
                miuixBackdrop = dockBackdrop,
                containerColorOverride = dockColor,
                liquidGlassTuningOverride = liquidGlassTuning,
            )

            val localActionDockBackdrop = rememberLayerBackdrop()
            val actionDockBackdrop = dockBackdrop ?: localActionDockBackdrop
            Box {
                if (dockBackdrop == null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0f)
                            .layerBackdrop(localActionDockBackdrop)
                            .background(AppSurfaceTokens.background())
                    )
                }
                Row(
                    modifier = Modifier
                        .then(
                            if (liquidGlassEnabled) {
                                Modifier.biliPaiFloatingDockShell(
                                    backdrop = actionDockBackdrop,
                                    containerColor = dockColor,
                                    pressProgress = 0f,
                                    shape = dockShape,
                                    liquidGlassTuning = liquidGlassTuning,
                                )
                            } else {
                                Modifier.background(dockColor, dockShape)
                            }
                        )
                        .clip(dockShape),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var showLayoutMenu by remember { mutableStateOf(false) }
                    Box {
                        AppIconButton(
                            onClick = { showLayoutMenu = true },
                            modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget)
                        ) {
                            AppIcon(
                                imageVector = if (displayMode.isHorizontalUserList())
                                    rememberAppGridLayoutIcon() else rememberAppListLayoutIcon(),
                                contentDescription = "关注列表位置",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                            )
                        }
                        AppDropdownMenu(
                            expanded = showLayoutMenu,
                            onDismissRequest = { showLayoutMenu = false }
                        ) {
                            DynamicDisplayMode.entries.forEach { mode ->
                                AppDropdownMenuItem(
                                    text = { AppText(resolveDynamicDisplayModeLabel(mode)) },
                                    onClick = {
                                        showLayoutMenu = false
                                        onDisplayModeChange(mode)
                                    }
                                )
                            }
                        }
                    }

                    //  发布动态入口（对齐 BiliPai AppBar actions 的发布按钮）
                    if (onPublishClick != null) {
                        val publishInteractionSource = remember { MutableInteractionSource() }
                        val publishPressed by publishInteractionSource.collectIsPressedAsState()
                        val publishIconPaths = publishSkinDecoration?.iconPaths
                        AppIconButton(
                            onClick = onPublishClick,
                            modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget),
                            interactionSource = publishInteractionSource,
                        ) {
                            if (publishIconPaths != null) {
                                AsyncImage(
                                    model = File(publishIconPaths.pathFor(publishPressed)),
                                    contentDescription = "发布动态",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(AppSpacingTokens.DoubleExtraLarge),
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(AppSpacingTokens.DoubleExtraLarge)
                                        .then(
                                            if (publishSkinDecoration?.hasShade == true) {
                                                Modifier.background(
                                                    brush = Brush.verticalGradient(
                                                        listOf(
                                                            publishSkinDecoration.shadeTop,
                                                            publishSkinDecoration.shadeBottom,
                                                        )
                                                    ),
                                                    shape = CircleShape,
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                ) {
                                    AppIcon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "发布动态",
                                        tint = publishSkinDecoration?.iconTint
                                            ?.takeUnless { it == Color.Unspecified }
                                            ?: MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun resolveDynamicTabSelectedColor(primaryColor: Color): Color = primaryColor
