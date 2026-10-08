package com.android.purebilibili.core.util

import android.os.Build

/**
 * vivo Android 13 tombstones show hwuiTask0 crashing in SurfaceControl.Transaction.setMatrix
 * while SurfaceView reports its position. Keep SDR playback off that native surface path.
 * HDR callers retain SurfaceView because TextureView cannot preserve HDR display output.
 */
internal fun prefersTextureVideoSurface(
    sdkInt: Int = Build.VERSION.SDK_INT,
    manufacturer: String? = Build.MANUFACTURER,
    brand: String? = Build.BRAND,
): Boolean = sdkInt == 33 &&
    (manufacturer.equals("vivo", ignoreCase = true) ||
        brand.equals("vivo", ignoreCase = true) || brand.equals("iQOO", ignoreCase = true))
