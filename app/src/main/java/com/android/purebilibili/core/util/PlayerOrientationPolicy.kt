package com.android.purebilibili.core.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.window.layout.WindowMetricsCalculator

internal const val LARGE_SCREEN_SMALLEST_WIDTH_DP = 600

internal fun isFoldableCoverWindow(
    smallestScreenWidthDp: Int,
    currentWindowWidthDp: Int?,
    currentWindowHeightDp: Int?,
    maximumWidthDp: Int? = null,
    maximumHeightDp: Int? = null,
): Boolean {
    val isDeviceLargeScreen = smallestScreenWidthDp >= LARGE_SCREEN_SMALLEST_WIDTH_DP ||
        (maximumWidthDp != null && maximumHeightDp != null &&
            minOf(maximumWidthDp, maximumHeightDp) >= LARGE_SCREEN_SMALLEST_WIDTH_DP)
    return isDeviceLargeScreen &&
        currentWindowWidthDp != null && currentWindowHeightDp != null &&
        minOf(currentWindowWidthDp, currentWindowHeightDp) < LARGE_SCREEN_SMALLEST_WIDTH_DP
}

internal fun shouldRequestPhysicalPlayerOrientation(
    smallestScreenWidthDp: Int,
    currentWindowWidthDp: Int? = null,
    currentWindowHeightDp: Int? = null,
    maximumWidthDp: Int? = null,
    maximumHeightDp: Int? = null,
    platformIgnoresLargeScreenOrientationRequests: Boolean =
        Build.VERSION.SDK_INT >= 36,
): Boolean {
    val isCoverWindow = isFoldableCoverWindow(
        smallestScreenWidthDp = smallestScreenWidthDp,
        currentWindowWidthDp = currentWindowWidthDp,
        currentWindowHeightDp = currentWindowHeightDp,
        maximumWidthDp = maximumWidthDp,
        maximumHeightDp = maximumHeightDp,
    )
    val isLargeScreen = smallestScreenWidthDp >= LARGE_SCREEN_SMALLEST_WIDTH_DP
    return !isLargeScreen || isCoverWindow || !platformIgnoresLargeScreenOrientationRequests
}

/**
 * Android 16+ ignores orientation restrictions for target-36+ apps on large screens,
 * and target 37 removes the manifest opt-out. Older Android releases still honor
 * requestedOrientation on tablets, so do not discard a user's fullscreen request there.
 */
internal fun Activity.applyPlayerRequestedOrientation(requestedOrientation: Int): Boolean {
    val density = resources.displayMetrics.density.coerceAtLeast(1f)
    val maxBounds = runCatching {
        WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(this).bounds
    }.getOrNull()
    val effectiveOrientation = if (
        shouldRequestPhysicalPlayerOrientation(
            smallestScreenWidthDp = resources.configuration.smallestScreenWidthDp,
            currentWindowWidthDp = resources.configuration.screenWidthDp,
            currentWindowHeightDp = resources.configuration.screenHeightDp,
            maximumWidthDp = maxBounds?.let { (it.width() / density).toInt() },
            maximumHeightDp = maxBounds?.let { (it.height() / density).toInt() },
        )
    ) {
        requestedOrientation
    } else {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    if (this.requestedOrientation == effectiveOrientation) return false
    this.requestedOrientation = effectiveOrientation
    return true
}
