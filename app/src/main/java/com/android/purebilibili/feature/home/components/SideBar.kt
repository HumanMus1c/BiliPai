package com.android.purebilibili.feature.home.components

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.R
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.core.ui.resolveGlobalWallpaperProtectiveColor
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.core.util.WindowWidthSizeClass
import com.android.purebilibili.core.util.rememberHapticFeedback
import dev.chrisbanes.haze.HazeState
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.SidebarRight
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.NavigationRail as MiuixNavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailDefaults as MiuixNavigationRailDefaults
import top.yukonga.miuix.kmp.basic.NavigationRailItem as MiuixNavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue as MiuixNavigationRailValue
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState as rememberMiuixNavigationRailState

/**
 * 平板端侧边导航栏 - 垂直版本的 FrostedBottomBar
 */
internal fun performHomeSideBarItemTap(
    haptic: (HapticType) -> Unit,
    onClick: () -> Unit
) {
    haptic(HapticType.LIGHT)
    onClick()
}

@Composable
fun FrostedSideBar(
    currentItem: BottomNavItem = BottomNavItem.HOME,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onHomeDoubleTap: () -> Unit = {},
    visibleItems: List<BottomNavItem> = listOf(
        BottomNavItem.HOME,
        BottomNavItem.DYNAMIC,
        BottomNavItem.HISTORY,
        BottomNavItem.PROFILE
    ),
    itemColorIndices: Map<String, Int> = emptyMap(),
    uiSkinDecoration: BottomBarUiSkinDecoration? = null,
    onToggleSidebar: (() -> Unit)? = null
) {
    when (
        resolveSideBarRenderer(
            uiPreset = LocalUiPreset.current,
            androidNativeVariant = LocalAndroidNativeVariant.current
        )
    ) {
        SideBarRenderer.MIUIX_NAVIGATION_RAIL -> {
            MiuixSideBar(
                currentItem = currentItem,
                onItemClick = onItemClick,
                modifier = modifier,
                firstItemModifier = firstItemModifier,
                hazeState = hazeState,
                onHomeDoubleTap = onHomeDoubleTap,
                visibleItems = visibleItems,
                uiSkinDecoration = uiSkinDecoration,
                onToggleSidebar = onToggleSidebar
            )
        }
        SideBarRenderer.FROSTED -> {
            FrostedSideBarContent(
                currentItem = currentItem,
                onItemClick = onItemClick,
                modifier = modifier,
                firstItemModifier = firstItemModifier,
                hazeState = hazeState,
                onHomeDoubleTap = onHomeDoubleTap,
                visibleItems = visibleItems,
                uiSkinDecoration = uiSkinDecoration,
                onToggleSidebar = onToggleSidebar
            )
        }
    }
}

@Composable
private fun MiuixSideBar(
    currentItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier,
    firstItemModifier: Modifier,
    hazeState: HazeState?,
    onHomeDoubleTap: () -> Unit,
    visibleItems: List<BottomNavItem>,
    uiSkinDecoration: BottomBarUiSkinDecoration?,
    onToggleSidebar: (() -> Unit)?
) {
    val haptic = rememberHapticFeedback()
    val isExpandedWidthClass =
        LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded
    val expandable = shouldUseExpandableMiuixSideBar(isExpandedWidthClass)
    val railState = if (expandable) {
        rememberMiuixNavigationRailState(MiuixNavigationRailValue.Expanded)
    } else {
        null
    }
    val chromeBackground = AppSurfaceTokens.surface()
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val blurIntensity = com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity()
    val backgroundAlpha = com.android.purebilibili.core.ui.blur.BlurStyles.getBackgroundAlpha(blurIntensity)
    val railColor = if (hazeState != null) {
        val rawColor = chromeBackground.copy(alpha = backgroundAlpha)
        if (globalWallpaperVisible) {
            val protectiveColor = resolveGlobalWallpaperProtectiveColor(
                baseColor = chromeBackground,
                lightAlpha = 0.70f,
                darkAlpha = 0.76f
            )
            rawColor.copy(alpha = maxOf(rawColor.alpha, protectiveColor.alpha))
        } else {
            rawColor
        }
    } else {
        chromeBackground
    }
    var lastHomeClickMs by remember { mutableLongStateOf(0L) }

    MiuixNavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (hazeState != null) {
                    Modifier.unifiedBlur(hazeState, shape = androidx.compose.ui.graphics.RectangleShape)
                } else {
                    Modifier
                }
            ),
        state = railState,
        color = railColor,
        showDivider = true,
        minWidth = MiuixNavigationRailDefaults.MinWidth,
        expandedWidth = MiuixNavigationRailDefaults.ExpandedWidth
    ) {
        visibleItems.forEachIndexed { itemIndex, item ->
            val isSelected = item == currentItem
            val itemLabel = resolveBottomNavItemLabel(item)
            val skinIconPath = uiSkinDecoration?.iconPathFor(item, selected = isSelected)
            val itemModifier = if (itemIndex == 0) firstItemModifier else Modifier
            val onItemTap = {
                val nowMs = SystemClock.elapsedRealtime()
                when (
                    resolveHomeSideBarClickAction(
                        item = item,
                        nowMs = nowMs,
                        lastHomeClickMs = lastHomeClickMs
                    )
                ) {
                    HomeSideBarClickAction.HOME_DOUBLE_TAP -> {
                        haptic(HapticType.MEDIUM)
                        onHomeDoubleTap()
                    }
                    HomeSideBarClickAction.NAVIGATE -> {
                        performHomeSideBarItemTap(
                            haptic = haptic,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
                if (item == BottomNavItem.HOME) {
                    lastHomeClickMs = nowMs
                }
            }

            if (shouldUseMiuixOfficialSideBarItem(skinIconPath)) {
                MiuixNavigationRailItem(
                    selected = isSelected,
                    onClick = onItemTap,
                    icon = resolveMaterialBottomBarIcon(item, isSelected),
                    label = itemLabel,
                    modifier = itemModifier
                )
            } else {
                MiuixSideBarSkinItem(
                    selected = isSelected,
                    label = itemLabel,
                    skinIconPath = skinIconPath,
                    onClick = onItemTap,
                    modifier = itemModifier
                )
            }
        }

        if (onToggleSidebar != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val sidebarLabel = stringResource(R.string.sidebar_toggle)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(AppShapes.container(ContainerLevel.Card))
                    .clickable {
                        haptic(HapticType.LIGHT)
                        onToggleSidebar()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    CupertinoIcons.Outlined.SidebarRight,
                    contentDescription = sidebarLabel,
                    tint = AppSurfaceTokens.onSurfaceVariantSummary(),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.MiuixSideBarSkinItem(
    selected: Boolean,
    label: String,
    skinIconPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val unselectedColor = AppSurfaceTokens.onSurface().copy(alpha = 0.6f)
    val iconColor = if (selected) primaryColor else unselectedColor
    Column(
        modifier = modifier
            .padding(vertical = 12.dp)
            .size(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            if (skinIconPath != null) {
                BottomBarSkinIcon(
                    iconPath = skinIconPath,
                    contentDescription = label,
                    size = resolveBottomBarMiuixSkinDockIconSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = iconColor
        )
    }
}

@Composable
private fun FrostedSideBarContent(
    currentItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier,
    firstItemModifier: Modifier,
    hazeState: HazeState?,
    onHomeDoubleTap: () -> Unit,
    visibleItems: List<BottomNavItem>,
    uiSkinDecoration: BottomBarUiSkinDecoration?,
    onToggleSidebar: (() -> Unit)?
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    val blurIntensity = com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity()
    val backgroundAlpha = com.android.purebilibili.core.ui.blur.BlurStyles.getBackgroundAlpha(blurIntensity)
    val chromeBackground = AppSurfaceTokens.chromeBackground()
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val sideBarContainerColor = if (hazeState != null) {
        val rawColor = chromeBackground.copy(alpha = backgroundAlpha)
        if (globalWallpaperVisible) {
            val protectiveColor = resolveGlobalWallpaperProtectiveColor(
                baseColor = chromeBackground,
                lightAlpha = 0.70f,
                darkAlpha = 0.76f
            )
            rawColor.copy(alpha = maxOf(rawColor.alpha, protectiveColor.alpha))
        } else {
            rawColor
        }
    } else {
        chromeBackground.copy(alpha = 0.95f)
    }

    val sideBarWidth = 80.dp

    Surface(
        modifier = modifier
            .width(sideBarWidth)
            .fillMaxHeight()
            .then(
                if (hazeState != null) {
                    Modifier.unifiedBlur(hazeState, shape = androidx.compose.ui.graphics.RectangleShape)
                } else {
                    Modifier.background(chromeBackground)
                }
            ),
        shape = androidx.compose.ui.graphics.RectangleShape,
        color = sideBarContainerColor,
        border = if (hazeState != null) {
            androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                )
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            visibleItems.forEachIndexed { itemIndex, item ->
                val isSelected = item == currentItem
                val itemLabel = resolveBottomNavItemLabel(item)

                var isPending by remember { mutableStateOf(false) }
                var wobbleAngle by remember { mutableFloatStateOf(0f) }

                val primaryColor = MaterialTheme.colorScheme.primary
                val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                val skinIconPath = uiSkinDecoration?.iconPathFor(item, selected = isSelected)

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected || isPending) primaryColor else unselectedColor,
                    animationSpec = spring(),
                    label = "iconColor"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.35f, stiffness = 300f),
                    label = "scale"
                )

                val animatedWobble by animateFloatAsState(
                    targetValue = wobbleAngle,
                    animationSpec = spring(dampingRatio = 0.2f, stiffness = 600f),
                    label = "wobble"
                )

                LaunchedEffect(wobbleAngle) {
                    if (wobbleAngle != 0f) {
                        kotlinx.coroutines.delay(50)
                        wobbleAngle = 0f
                    }
                }
                val triggerItemClick = {
                    isPending = true
                    performHomeSideBarItemTap(
                        haptic = haptic,
                        onClick = { onItemClick(item) }
                    )
                    wobbleAngle = 8f
                    scope.launch {
                        kotlinx.coroutines.delay(90)
                        isPending = false
                    }
                }

                Column(
                    modifier = Modifier
                        .size(64.dp)
                        .then(if (itemIndex == 0) firstItemModifier else Modifier)
                        .then(
                            if (item == BottomNavItem.HOME) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            triggerItemClick()
                                        },
                                        onDoubleTap = {
                                            haptic(HapticType.MEDIUM)
                                            onHomeDoubleTap()
                                        }
                                    )
                                }
                            } else {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    triggerItemClick()
                                }
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                rotationZ = animatedWobble
                            }
                    ) {
                        CompositionLocalProvider(LocalContentColor provides iconColor) {
                            if (skinIconPath != null) {
                                BottomBarSkinIcon(
                                    iconPath = skinIconPath,
                                    contentDescription = itemLabel,
                                    size = resolveBottomBarMiuixSkinDockIconSize()
                                )
                            } else if (isSelected) {
                                item.selectedIcon()
                            } else {
                                item.unselectedIcon()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = itemLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = iconColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            if (onToggleSidebar != null) {
                val sidebarLabel = stringResource(R.string.sidebar_toggle)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(AppShapes.container(ContainerLevel.Card))
                        .clickable {
                            haptic(HapticType.LIGHT)
                            onToggleSidebar()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        CupertinoIcons.Outlined.SidebarRight,
                        contentDescription = sidebarLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
