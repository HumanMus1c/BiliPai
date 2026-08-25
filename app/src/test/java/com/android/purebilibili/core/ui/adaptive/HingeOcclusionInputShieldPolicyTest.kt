package com.android.purebilibili.core.ui.adaptive

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.util.AppFoldingFeatureInfo
import com.android.purebilibili.core.util.AppWindowAdaptiveInfo
import com.android.purebilibili.core.util.WindowHeightSizeClass
import com.android.purebilibili.core.util.WindowSizeClass
import com.android.purebilibili.core.util.WindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HingeOcclusionInputShieldPolicyTest {
    private val windowSizeClass = WindowSizeClass(
        widthSizeClass = WindowWidthSizeClass.Expanded,
        heightSizeClass = WindowHeightSizeClass.Medium,
        widthDp = 1000.dp,
        heightDp = 800.dp,
    )

    @Test
    fun occludingHinge_returnsClippedInputExclusionBounds() {
        val adaptiveInfo = AppWindowAdaptiveInfo(
            windowSizeClass = windowSizeClass,
            foldingFeature = AppFoldingFeatureInfo(
                hingeBounds = IntRect(left = 490, top = -20, right = 510, bottom = 820),
                isOccluding = true,
            ),
        )

        assertEquals(
            IntRect(left = 490, top = 0, right = 510, bottom = 800),
            resolveOccludingHingeInputBounds(adaptiveInfo, 1000, 800),
        )
    }

    @Test
    fun separatingButNotOccludingHinge_doesNotInstallGlobalInputShield() {
        val adaptiveInfo = AppWindowAdaptiveInfo(
            windowSizeClass = windowSizeClass,
            foldingFeature = AppFoldingFeatureInfo(
                hingeBounds = IntRect(left = 490, top = 0, right = 510, bottom = 800),
                isSeparating = true,
                isOccluding = false,
            ),
        )

        assertNull(resolveOccludingHingeInputBounds(adaptiveInfo, 1000, 800))
    }
}
