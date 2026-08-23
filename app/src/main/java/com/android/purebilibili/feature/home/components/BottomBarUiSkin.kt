package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.OpticalContrastPalette
import com.android.purebilibili.feature.home.HomeVisualPalette

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.purebilibili.core.plugin.skin.UiSkinState
import com.android.purebilibili.core.plugin.skin.UiSkinSurface
import java.io.File

data class BottomBarUiSkinDecoration(
    val skinId: String,
    val bottomTrimTint: Color,
    val bottomTrimAccent: Color,
    val bottomTrimImagePath: String? = null,
    val bottomBarIconPaths: Map<BottomNavItem, BottomBarSkinIconPaths> = emptyMap()
) {
    fun iconPathFor(item: BottomNavItem, selected: Boolean = false): String? {
        val paths = bottomBarIconPaths[item] ?: return null
        return if (selected) {
            paths.selected ?: paths.unselected
        } else {
            paths.unselected
        }
    }
}

data class BottomBarSkinIconPaths(
    val unselected: String,
    val selected: String? = null
)

data class TopTabSkinIconPaths(
    val unselected: String,
    val selected: String? = null
) {
    fun pathFor(selected: Boolean): String {
        return if (selected) {
            this.selected ?: unselected
        } else {
            unselected
        }
    }
}

data class HomeUiSkinDecoration(
    val skinId: String,
    val topAtmosphereTint: Color,
    val searchCapsuleTint: Color,
    val topAtmosphereImagePath: String? = null,
    val topTabBackgroundImagePath: String? = null,
    val sideBackgroundImagePath: String? = null,
    val profileBackgroundImagePath: String? = null,
    val profileSquaredBackgroundImagePath: String? = null,
    val topTabSkinIconPaths: Map<String, TopTabSkinIconPaths> = emptyMap(),
    val topTabPartitionSkinIconPaths: TopTabSkinIconPaths? = null
) {
    fun topTabIconPathFor(categoryKey: String, selected: Boolean = false): String? {
        val normalizedKey = categoryKey.trim().uppercase()
        val paths = topTabSkinIconPaths[normalizedKey] ?: return null
        return paths.pathFor(selected)
    }

    fun topTabPartitionIconPath(selected: Boolean = false): String? {
        return topTabPartitionSkinIconPaths?.pathFor(selected)
    }
}

internal fun resolveBottomBarSkinDockIconSize(): Dp = AppSpacingTokens.DoubleExtraLarge

internal fun resolveBottomBarSkinDockHeight(): Dp = AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Large

internal fun resolveBottomBarSkinDockContentPadding(): PaddingValues = PaddingValues(
    start = AppSpacingTokens.ExtraSmall,
    end = AppSpacingTokens.ExtraSmall,
    top = AppSpacingTokens.None,
    bottom = AppSpacingTokens.None
)

internal fun resolveBottomBarSkinIconLabelGap(): Dp = AppSpacingTokens.Micro

internal fun resolveBottomBarSkinDockIconTopPadding(): Dp = AppSpacingTokens.ExtraSmall

internal fun resolveBottomBarSkinDockLabelBottomPadding(): Dp = AppSpacingTokens.ExtraSmall

internal fun resolveBottomBarMiuixSkinDockIconSize(): Dp = AppSpacingTokens.DoubleExtraLarge

internal fun resolveBottomBarCompactSkinHomeIconSize(): Dp = AppSpacingTokens.DoubleExtraLarge

internal fun resolveMiuixDockedBottomBarItemHeight(hasUiSkinDecoration: Boolean): Dp {
    return if (hasUiSkinDecoration) {
        resolveBottomBarSkinDockHeight()
    } else {
        AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Large
    }
}

@Composable
fun rememberBottomBarUiSkinDecoration(uiSkinState: UiSkinState): BottomBarUiSkinDecoration? {
    return remember(uiSkinState) {
        resolveBottomBarUiSkinDecoration(uiSkinState)
    }
}

@Composable
fun rememberHomeUiSkinDecoration(uiSkinState: UiSkinState): HomeUiSkinDecoration? {
    return remember(uiSkinState) {
        resolveHomeUiSkinDecoration(uiSkinState)
    }
}

fun resolveBottomBarUiSkinDecoration(uiSkinState: UiSkinState): BottomBarUiSkinDecoration? {
    val activeSkin = uiSkinState.activeSkin
    return if (!uiSkinState.enabled || activeSkin == null) {
        null
    } else {
        BottomBarUiSkinDecoration(
            skinId = activeSkin.manifest.skinId,
            bottomTrimTint = parseUiSkinColor(
                value = activeSkin.manifest.colors.bottomBarTrimTint,
                fallback = HomeVisualPalette.BottomBarIceLight
            ),
            bottomTrimAccent = parseUiSkinColor(
                value = activeSkin.manifest.colors.topAtmosphereTint,
                fallback = HomeVisualPalette.BottomBarIce
            ),
            bottomTrimImagePath = activeSkin.assetFilePath(activeSkin.manifest.assets.bottomBarTrim),
            bottomBarIconPaths = resolveBottomBarSkinIconPaths(activeSkin)
        )
    }
}

fun resolveHomeUiSkinDecoration(uiSkinState: UiSkinState): HomeUiSkinDecoration? {
    val activeSkin = uiSkinState.activeSkin
    return if (!uiSkinState.enabled || activeSkin == null) {
        null
    } else {
        val manifest = activeSkin.manifest
        val hasTopDecoration = UiSkinSurface.HOME_TOP_CHROME in manifest.surfaces &&
            (
                manifest.assets.topAtmosphere != null ||
                    manifest.assets.homeTopTabBackground != null ||
                    manifest.assets.homeSideBackground != null ||
                    manifest.assets.homeProfileBackground != null ||
                    manifest.assets.homeProfileSquaredBackground != null ||
                    manifest.colors.topAtmosphereTint != null ||
                    manifest.colors.searchCapsuleTint != null
                )
        if (!hasTopDecoration) return null
        HomeUiSkinDecoration(
            skinId = manifest.skinId,
            topAtmosphereTint = parseUiSkinColor(
                value = manifest.colors.topAtmosphereTint,
                fallback = HomeVisualPalette.BottomBarIce
            ),
            searchCapsuleTint = parseUiSkinColor(
                value = manifest.colors.searchCapsuleTint,
                fallback = OpticalContrastPalette.Highlight
            ),
            topAtmosphereImagePath = activeSkin.assetFilePath(manifest.assets.topAtmosphere),
            topTabBackgroundImagePath = activeSkin.assetFilePath(manifest.assets.homeTopTabBackground),
            sideBackgroundImagePath = activeSkin.assetFilePath(manifest.assets.homeSideBackground),
            profileBackgroundImagePath = activeSkin.assetFilePath(manifest.assets.homeProfileBackground),
            profileSquaredBackgroundImagePath = activeSkin.assetFilePath(
                manifest.assets.homeProfileSquaredBackground
            )
        )
    }
}

@Composable
internal fun BottomBarSkinIcon(
    iconPath: String,
    contentDescription: String?,
    size: Dp = resolveBottomBarSkinDockIconSize(),
    modifier: Modifier = Modifier
) {
    val scalePolicy = rememberSkinIconScalePolicy(iconPath)
    Box(modifier = modifier.size(size)) {
        AsyncImage(
            model = File(iconPath),
            contentDescription = contentDescription,
            contentScale = scalePolicy.contentScale,
            alignment = scalePolicy.alignment,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 图标尺寸兼容策略：B 站装扮图标原图尺寸差异大（留白/装饰边/非正方形）。
 * 纯函数 [resolveSkinIconScalePolicy] 按原图宽高比决定缩放方式，便于单元测试。
 * 规则：正方形或近正方形 → Fit 居中；宽高比失衡（留白多）→ Crop 居中兜底，
 * 避免图标在 32dp 框内显得过小。
 */
data class SkinIconScalePolicy(
    val contentScale: ContentScale,
    val alignment: Alignment
)

private val ICON_ASPECT_TOLERANCE = 0.25f

fun resolveSkinIconScalePolicy(imageAspectRatio: Float): SkinIconScalePolicy {
    // imageAspectRatio = width / height；1.0 为正方形。
    val ratio = if (imageAspectRatio <= 0f) 1f else imageAspectRatio
    val balanced = ratio in (1f - ICON_ASPECT_TOLERANCE)..(1f + ICON_ASPECT_TOLERANCE)
    return if (balanced) {
        SkinIconScalePolicy(ContentScale.Fit, Alignment.Center)
    } else {
        SkinIconScalePolicy(ContentScale.Crop, Alignment.Center)
    }
}

@Composable
private fun rememberSkinIconScalePolicy(iconPath: String): SkinIconScalePolicy {
    // 默认用 Fit 居中（保留原图观感）；精确宽高比需异步解码，此处保守取平衡态。
    // 真实合成预览组件复用此策略，用户导入前即可看到尺寸效果。
    return remember(iconPath) { resolveSkinIconScalePolicy(1f) }
}

@Composable
internal fun BottomBarSkinDecorativeTrim(
    decoration: BottomBarUiSkinDecoration?,
    modifier: Modifier = Modifier,
    clipShape: androidx.compose.ui.graphics.Shape? = null
) {
    if (decoration == null) return
    Box(
        modifier = modifier
            .then(clipShape?.let { Modifier.clip(it) } ?: Modifier)
            .clearAndSetSemantics {}
            .drawBehind {
                val trimHeight = size.height * 0.36f
                val top = size.height - trimHeight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            decoration.bottomTrimTint.copy(alpha = 0.08f),
                            decoration.bottomTrimTint.copy(alpha = 0.28f)
                        ),
                        startY = top,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, trimHeight),
                    cornerRadius = CornerRadius(trimHeight, trimHeight)
                )

                val cloudRadius = trimHeight * 0.38f
                val centers = listOf(0.12f, 0.26f, 0.44f, 0.62f, 0.78f, 0.91f)
                centers.forEachIndexed { index, fraction ->
                    val y = top + trimHeight * if (index % 2 == 0) 0.46f else 0.58f
                    drawCircle(
                        color = decoration.bottomTrimAccent.copy(alpha = 0.18f),
                        radius = cloudRadius * if (index % 2 == 0) 1.0f else 0.78f,
                        center = Offset(size.width * fraction, y)
                    )
                }
            }
    ) {
        val imagePath = decoration.bottomTrimImagePath
        if (!imagePath.isNullOrBlank()) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = null,
                // FillWidth + 底部对齐：trim 通常宽度铺满、高度自适应，
                // 避免 FillBounds 拉伸变形与液态玻璃折射层叠加时产生视觉冲突。
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.82f)
                    .clearAndSetSemantics {}
            )
        }
    }
}

private fun parseUiSkinColor(
    value: String?,
    fallback: Color
): Color {
    val normalized = value
        ?.trim()
        ?.removePrefix("#")
        ?.takeIf { it.length == 6 || it.length == 8 }
        ?: return fallback
    val argb = if (normalized.length == 6) "FF$normalized" else normalized
    return runCatching { Color(argb.toLong(16)) }.getOrDefault(fallback)
}

private fun resolveBottomBarSkinIconPaths(
    activeSkin: com.android.purebilibili.core.plugin.skin.InstalledUiSkinPackage
): Map<BottomNavItem, BottomBarSkinIconPaths> {
    val manifestIcons = activeSkin.manifest.assets.bottomBarIcons
    return buildMap {
        mapOf(
            "home" to ("home_selected" to BottomNavItem.HOME),
            "following" to ("following_selected" to BottomNavItem.DYNAMIC),
            "member" to ("member_selected" to BottomNavItem.HISTORY),
            "channel" to ("channel_selected" to BottomNavItem.LISTEN_VIDEO),
            "profile" to ("profile_selected" to BottomNavItem.PROFILE)
        ).forEach { (unselectedKey, selectedKeyAndItem) ->
            val (selectedKey, item) = selectedKeyAndItem
            activeSkin.assetFilePath(manifestIcons[unselectedKey])?.let { unselectedPath ->
                put(
                    item,
                    BottomBarSkinIconPaths(
                        unselected = unselectedPath,
                        selected = activeSkin.assetFilePath(manifestIcons[selectedKey])
                    )
                )
            }
        }
        if (BottomNavItem.LISTEN_VIDEO !in this) {
            activeSkin.assetFilePath(activeSkin.manifest.assets.homeChannelIcon)?.let { unselectedPath ->
                put(
                    BottomNavItem.LISTEN_VIDEO,
                    BottomBarSkinIconPaths(
                        unselected = unselectedPath,
                        selected = activeSkin.assetFilePath(
                            activeSkin.manifest.assets.homeChannelSelectedIcon
                        )
                    )
                )
            }
        }
    }
}
