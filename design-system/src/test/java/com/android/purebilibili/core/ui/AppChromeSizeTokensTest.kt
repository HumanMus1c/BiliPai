package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AppChromeSizeTokensTest {

    @Test
    fun `icon actions keep a 48dp outer touch target`() {
        assertEquals(48.dp, AppChromeSizeTokens.MinimumTouchTarget)
    }

    @Test
    fun `ios compact capsule tokens keep global chrome dense and aligned`() {
        val spec = resolveCompactCapsuleChromeSpec(
            uiPreset = UiPreset.IOS,
            androidNativeVariant = AndroidNativeVariant.MATERIAL3
        )

        assertEquals(44, spec.primaryHeightDp)
        assertEquals(40, spec.secondaryButtonSizeDp)
        assertEquals(36, spec.chipHeightDp)
        assertEquals(32, spec.compactChipHeightDp)
        assertEquals(22, spec.primaryCornerRadiusDp)
        assertEquals(20, spec.secondaryButtonCornerRadiusDp)
        assertEquals(20, spec.iconSizeDp)
        assertEquals(8, spec.standardGapDp)
    }

    @Test
    fun `md3 compact capsule tokens align search bar and touch targets with material3 chrome`() {
        val spec = resolveCompactCapsuleChromeSpec(
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MATERIAL3
        )

        assertEquals(56, spec.primaryHeightDp)
        assertEquals(48, spec.secondaryButtonSizeDp)
        assertEquals(32, spec.chipHeightDp)
        assertEquals(28, spec.compactChipHeightDp)
        assertEquals(28, spec.primaryCornerRadiusDp)
        assertEquals(24, spec.secondaryButtonCornerRadiusDp)
        assertEquals(24, spec.iconSizeDp)
        assertEquals(12, spec.standardGapDp)
    }

    @Test
    fun `miuix compact capsule tokens keep denser search chrome within material touch bounds`() {
        val spec = resolveCompactCapsuleChromeSpec(
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MIUIX
        )

        assertEquals(48, spec.primaryHeightDp)
        assertEquals(48, spec.secondaryButtonSizeDp)
        assertEquals(22, spec.primaryCornerRadiusDp)
        assertEquals(14, spec.inputHorizontalPaddingDp)
        assertEquals(8, spec.standardGapDp)
    }
}
