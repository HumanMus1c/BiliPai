package com.android.purebilibili.core.ui.wallpaper

import com.android.purebilibili.core.ui.adaptive.AdaptiveWidthClass
import kotlin.math.abs

enum class SplashWallpaperLayout {
    FULL_CROP,
    POSTER_CARD_BLUR_BG,
}

enum class ProfileWallpaperLayout {
    TOP_BANNER_BLUR_BG,
    POSTER_CARD_BLUR_BG,
}

fun resolveSplashWallpaperLayout(
    widthClass: AdaptiveWidthClass,
    imageAspectRatio: Float? = null,
    screenAspectRatio: Float = 9f / 16f,
    compactAspectMismatchThreshold: Float = 0.08f,
): SplashWallpaperLayout {
    return when (widthClass) {
        AdaptiveWidthClass.Compact -> {
            if (
                imageAspectRatio != null &&
                abs(imageAspectRatio - screenAspectRatio) > compactAspectMismatchThreshold
            ) {
                SplashWallpaperLayout.POSTER_CARD_BLUR_BG
            } else {
                SplashWallpaperLayout.FULL_CROP
            }
        }
        AdaptiveWidthClass.Medium,
        AdaptiveWidthClass.Expanded -> SplashWallpaperLayout.FULL_CROP
    }
}

fun resolveProfileWallpaperLayout(widthClass: AdaptiveWidthClass): ProfileWallpaperLayout {
    return when (widthClass) {
        AdaptiveWidthClass.Compact -> ProfileWallpaperLayout.TOP_BANNER_BLUR_BG
        AdaptiveWidthClass.Medium,
        AdaptiveWidthClass.Expanded -> ProfileWallpaperLayout.POSTER_CARD_BLUR_BG
    }
}
