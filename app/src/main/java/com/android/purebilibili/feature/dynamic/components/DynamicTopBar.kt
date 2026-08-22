// 文件路径: feature/dynamic/components/DynamicTopBar.kt
package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.AppSurfaceTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//  Material Icons
import com.android.purebilibili.core.ui.rememberAppGridLayoutIcon
import com.android.purebilibili.core.ui.rememberAppListLayoutIcon
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.resolveGlobalWallpaperProtectiveColor
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.feature.dynamic.resolveDynamicTopBarHorizontalPadding
import com.android.purebilibili.feature.dynamic.resolveDynamicTopBarLiquidTabSpec
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.android.purebilibili.core.ui.blur.BlurStyles
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import dev.chrisbanes.haze.HazeState

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
    hazeState: HazeState? = null,
    indicatorPositionProvider: (() -> Float)? = null,
    isScrollInProgressProvider: () -> Boolean = { false },
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density).let { with(density) { it.toDp() } }
    val liquidTabSpec = resolveDynamicTopBarLiquidTabSpec()
    
    //  读取当前模糊强度以确定背景透明度
    val blurIntensity = currentUnifiedBlurIntensity()
    val backgroundAlpha = BlurStyles.getBackgroundAlpha(blurIntensity)
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val shouldUseHeaderBlur = shouldUseDynamicTopBarHeaderBlur(
        hasHazeState = hazeState != null,
        globalWallpaperVisible = globalWallpaperVisible,
    )
    
    //  使用 blurIntensity 对应的背景透明度实现毛玻璃质感
    val headerColor = resolveDynamicTopBarHeaderColor(
        surfaceColor = AppSurfaceTokens.surface(),
        backgroundAlpha = if (shouldUseHeaderBlur) backgroundAlpha else 0f,
        globalWallpaperVisible = globalWallpaperVisible
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (shouldUseHeaderBlur && hazeState != null) Modifier.unifiedBlur(hazeState) else Modifier)
            .background(headerColor)
    ) {
        Column {
            Spacer(modifier = Modifier.height(statusBarHeight))
            
            //  紧凑标签行：宽屏动态页优先展示内容密度
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(liquidTabSpec.heightDp.dp)
                    .padding(horizontal = resolveDynamicTopBarHorizontalPadding()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarLiquidSegmentedControl(
                    items = tabs,
                    selectedIndex = selectedTab,
                    onSelected = onTabSelected,
                    modifier = Modifier.weight(1f),
                    height = liquidTabSpec.heightDp.dp,
                    indicatorHeight = liquidTabSpec.indicatorHeightDp.dp,
                    labelFontSize = liquidTabSpec.labelFontSizeSp.sp,
                    indicatorPositionProvider = indicatorPositionProvider,
                    isScrollInProgressProvider = isScrollInProgressProvider,
                    liquidGlassEffectsEnabled = false,
                )
                
                //  布局模式切换按钮
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
                    AppIconButton(
                        onClick = onPublishClick,
                        modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget)
                    ) {
                        AppIcon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "发布动态",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                        )
                    }
                }
            }
        }
    }
}

internal fun resolveDynamicTabSelectedColor(primaryColor: Color): Color = primaryColor

internal fun resolveDynamicTopBarHeaderColor(
    surfaceColor: Color,
    backgroundAlpha: Float,
    globalWallpaperVisible: Boolean
): Color {
    return if (globalWallpaperVisible) {
        val protectiveColor = resolveGlobalWallpaperProtectiveColor(surfaceColor)
        protectiveColor.copy(alpha = maxOf(protectiveColor.alpha, backgroundAlpha))
    } else {
        surfaceColor.copy(alpha = backgroundAlpha)
    }
}

internal fun shouldUseDynamicTopBarHeaderBlur(
    hasHazeState: Boolean,
    globalWallpaperVisible: Boolean,
    liquidGlassEnabled: Boolean = false,
): Boolean {
    return hasHazeState && !globalWallpaperVisible && !liquidGlassEnabled
}
