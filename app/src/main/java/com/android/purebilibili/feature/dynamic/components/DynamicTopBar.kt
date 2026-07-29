// 文件路径: feature/dynamic/components/DynamicTopBar.kt
package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.OpticalContrastPalette

import com.android.purebilibili.core.ui.AppSurfaceTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//  Cupertino Icons - iOS SF Symbols 风格图标
import com.android.purebilibili.core.ui.rememberAppGridLayoutIcon
import com.android.purebilibili.core.ui.rememberAppListLayoutIcon
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.resolveGlobalWallpaperProtectiveColor
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.feature.dynamic.resolveDynamicTopBarHorizontalPadding
import com.android.purebilibili.feature.dynamic.resolveDynamicTopBarLiquidTabSpec
import com.android.purebilibili.feature.dynamic.resolveDynamicTabIndicatorPosition
import com.android.purebilibili.core.ui.blur.BlurStyles
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import dev.chrisbanes.haze.HazeState

//  动态页面布局模式
enum class DynamicDisplayMode {
    SIDEBAR,     // 侧边栏模式（默认，UP主列表在左侧）
    HORIZONTAL   // 横向模式（UP主列表在顶部，类似 Telegram）
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
    hazeState: HazeState? = null,
    indicatorPositionProvider: (() -> Float)? = null
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
        globalWallpaperVisible = globalWallpaperVisible
    )
    
    //  使用 blurIntensity 对应的背景透明度实现毛玻璃质感
    val headerColor = resolveDynamicTopBarHeaderColor(
        surfaceColor = AppSurfaceTokens.surface(),
        backgroundAlpha = if (shouldUseHeaderBlur) backgroundAlpha else 0f,
        globalWallpaperVisible = globalWallpaperVisible
    )

    //  [关键修复] 使用透明背景，让主界面的渐变透出来
    Box(
        modifier = modifier
            .fillMaxWidth()
            // 应用模糊效果
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
                DynamicCompactTabRow(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.weight(1f),
                    indicatorPositionProvider = indicatorPositionProvider
                )
                
                //  布局模式切换按钮
                IconButton(
                    onClick = {
                        val newMode = if (displayMode == DynamicDisplayMode.SIDEBAR) 
                            DynamicDisplayMode.HORIZONTAL else DynamicDisplayMode.SIDEBAR
                        onDisplayModeChange(newMode)
                    },
                    modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget)
                ) {
                    Icon(
                        imageVector = if (displayMode == DynamicDisplayMode.SIDEBAR)
                            rememberAppListLayoutIcon() else rememberAppGridLayoutIcon(),
                        contentDescription = "切换布局模式",
                        tint = MaterialTheme.colorScheme.onSurface, // 自适应颜色
                        modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicCompactTabRow(
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    indicatorPositionProvider: (() -> Float)? = null
) {
    if (tabs.isEmpty()) return
    val safeSelectedIndex = selectedTab.coerceIn(tabs.indices)
    val indicatorPosition = resolveDynamicTabIndicatorPosition(
        selectedIndex = safeSelectedIndex,
        externalPosition = indicatorPositionProvider?.invoke(),
        itemCount = tabs.size,
    )
    val height = AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Medium

    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(height)) {
        val segmentWidth = maxWidth / tabs.size
        val underlineWidth = (segmentWidth * 0.42f)
            .coerceAtLeast(AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall)
            .coerceAtMost(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Small)
        val selectedColor = rememberDynamicTabSelectedColor()

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, label ->
                val selected = index == safeSelectedIndex
                val textColor = if (selected) selectedColor else rememberDynamicTabUnselectedColor()
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        color = textColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = segmentWidth * indicatorPosition + (segmentWidth - underlineWidth) / 2)
                .width(underlineWidth)
                .height(AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2)
                .clip(CircleShape)
                .background(selectedColor),
        )
    }
}

@Composable
private fun rememberDynamicTabSelectedColor(): Color = resolveDynamicTabSelectedColor(MaterialTheme.colorScheme.primary)

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
    globalWallpaperVisible: Boolean
): Boolean = hasHazeState && !globalWallpaperVisible

@Composable
private fun rememberDynamicTabUnselectedColor(): Color {
    return if (isDynamicTopBarDarkSurface(AppSurfaceTokens.surface())) {
        OpticalContrastPalette.Highlight.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun isDynamicTopBarDarkSurface(color: Color): Boolean {
    val perceivedBrightness = (color.red * 0.299f) + (color.green * 0.587f) + (color.blue * 0.114f)
    return perceivedBrightness < 0.45f
}
