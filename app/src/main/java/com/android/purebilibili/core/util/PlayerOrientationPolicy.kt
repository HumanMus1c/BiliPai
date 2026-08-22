package com.android.purebilibili.core.util

import android.app.Activity
import android.content.pm.ActivityInfo

internal const val LARGE_SCREEN_SMALLEST_WIDTH_DP = 600

internal fun shouldRequestPhysicalPlayerOrientation(smallestScreenWidthDp: Int): Boolean =
    smallestScreenWidthDp in 1 until LARGE_SCREEN_SMALLEST_WIDTH_DP

/**
 * Android 17 ignores orientation restrictions on large screens. Release any old
 * phone-only lock there and let fullscreen remain an in-app layout state.
 */
internal fun Activity.applyPlayerRequestedOrientation(requestedOrientation: Int): Boolean {
    val effectiveOrientation = if (
        shouldRequestPhysicalPlayerOrientation(resources.configuration.smallestScreenWidthDp)
    ) {
        requestedOrientation
    } else {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    if (this.requestedOrientation == effectiveOrientation) return false
    this.requestedOrientation = effectiveOrientation
    return true
}
