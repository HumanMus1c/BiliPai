package com.android.purebilibili.core.theme

import androidx.compose.material3.MotionScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AndroidNativeVariantThemePolicyTest {

    @Test
    fun miuixVariant_usesMiuixAlignedTypography() {
        val typography = resolveMaterialTypography(AppUiStyle.MIUIX)

        assertEquals(BiliMiuixTypography.bodyMedium.fontSize, typography.bodyMedium.fontSize)
        assertEquals(BiliMiuixTypography.titleMedium.letterSpacing, typography.titleMedium.letterSpacing)
    }

    @Test
    fun material3Variant_usesMd3Typography() {
        val typography = resolveMaterialTypography(AppUiStyle.MATERIAL3)

        assertEquals(Md3Typography.bodyMedium.fontSize, typography.bodyMedium.fontSize)
        assertEquals(Md3Typography.titleMedium.letterSpacing, typography.titleMedium.letterSpacing)
        assertFalse(typography.bodyMedium.fontSize == BiliMiuixTypography.bodyMedium.fontSize)
    }

    @Test
    fun miuixVariant_enablesSmoothRoundingAndLargerCornerScale() {
        assertTrue(shouldUseMiuixSmoothRounding(AppUiStyle.MIUIX))
        assertEquals(
            MIUIX_CORNER_RADIUS_SCALE,
            resolveCornerRadiusScale(AppUiStyle.MIUIX)
        )
    }

    @Test
    fun material3Variant_keepsCompactCornerScaleWithoutSmoothRounding() {
        assertFalse(shouldUseMiuixSmoothRounding(AppUiStyle.MATERIAL3))
        assertEquals(
            MD3_CORNER_RADIUS_SCALE,
            resolveCornerRadiusScale(AppUiStyle.MATERIAL3)
        )
    }

    @Test
    fun material3Variant_usesExpressiveMotionScheme() {
        val motionScheme = resolveMaterialMotionScheme(AppUiStyle.MATERIAL3)

        assertSame(MotionScheme.expressive(), motionScheme)
        assertNotSame(MotionScheme.standard(), motionScheme)
    }

    @Test
    fun miuixVariant_keepsStandardMotionScheme() {
        val miuix = resolveMaterialMotionScheme(AppUiStyle.MIUIX)

        assertSame(MotionScheme.standard(), miuix)
    }

    @Test
    fun dualValueStyles_resolveChromeTokens() {
        val miuix = resolveAndroidNativeChromeTokens(AppUiStyle.MIUIX)
        val material = resolveAndroidNativeChromeTokens(AppUiStyle.MATERIAL3)

        assertEquals(24, material.containerCornerRadiusDp)
        assertEquals(20, miuix.containerCornerRadiusDp)
        assertTrue(material.pillCornerRadiusDp > miuix.pillCornerRadiusDp)
        assertTrue(material.selectedContainerAlpha < miuix.selectedContainerAlpha)
        assertEquals(1f, material.motionScale)
        assertEquals(1f, miuix.motionScale)
        assertEquals(3, material.tonalSurfaceElevationDp)
        assertEquals(0, miuix.tonalSurfaceElevationDp)
        assertEquals(240, miuix.motionEmphasizedMillis)
        assertEquals(300, material.motionEmphasizedMillis)
        assertEquals(180, miuix.motionStandardMillis)
        assertEquals(200, material.motionStandardMillis)
    }

    @Test
    fun dualValueStyles_resolveShapes() {
        val miuix = resolveMaterialShapes(AppUiStyle.MIUIX)
        val material = resolveMaterialShapes(AppUiStyle.MATERIAL3)

        assertSame(MiuixAlignedShapes, miuix)
        assertSame(Md3Shapes, material)
    }
}
