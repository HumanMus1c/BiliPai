package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.UiPreset
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveBottomSheetPolicyTest {

    @Test
    fun `app sheet facade preserves caller content color`() {
        val path = "src/main/java/com/android/purebilibili/core/ui/AppSheetComponents.kt"
        val source = listOf(File(path), File("design-system/$path"))
            .firstOrNull(File::exists)
            ?.readText()
            ?: error("Cannot locate AppSheetComponents.kt from ${File(".").absolutePath}")

        assertTrue(source.contains("contentColor: Color = MaterialTheme.colorScheme.onSurface"))
        assertTrue(source.contains("contentColor = contentColor"))
        assertTrue(source.contains("content: @Composable ColumnScope.() -> Unit"))
    }

    @Test
    fun `md3 preset should use material drag handle and larger corner radius`() {
        val spec = resolveAdaptiveBottomSheetVisualSpec(UiPreset.MD3)

        assertEquals(28, spec.cornerRadiusDp)
        assertTrue(spec.useMaterialDragHandle)
    }

    @Test
    fun `ios preset should preserve compact sheet chrome`() {
        val spec = resolveAdaptiveBottomSheetVisualSpec(UiPreset.IOS)

        assertEquals(14, spec.cornerRadiusDp)
        assertFalse(spec.useMaterialDragHandle)
    }

    @Test
    fun `ios preset should use softer sheet motion`() {
        val spec = resolveAdaptiveBottomSheetMotionSpec(UiPreset.IOS)

        assertEquals(360, spec.scrimEnterDurationMillis)
        assertEquals(180, spec.scrimExitDurationMillis)
        assertEquals(360, spec.contentEnterFadeDurationMillis)
        assertEquals(180, spec.contentExitFadeDurationMillis)
    }

    @Test
    fun `md3 preset should keep sheet dismiss faster than enter`() {
        val spec = resolveAdaptiveBottomSheetMotionSpec(UiPreset.MD3)

        assertTrue(spec.scrimExitDurationMillis < spec.scrimEnterDurationMillis)
        assertTrue(spec.contentExitFadeDurationMillis < spec.contentEnterFadeDurationMillis)
    }

    @Test
    fun `overlay visual progress should scale scrim and disable blur when hidden`() {
        val hidden = resolveInteractiveOverlayProgressVisual(
            presentationProgress = 0f,
            surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
            blurActive = true,
            maxScrimAlpha = 0.5f
        )
        val half = resolveInteractiveOverlayProgressVisual(
            presentationProgress = 0.5f,
            surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
            blurActive = true,
            maxScrimAlpha = 0.5f
        )
        val shown = resolveInteractiveOverlayProgressVisual(
            presentationProgress = 1f,
            surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
            blurActive = true,
            maxScrimAlpha = 0.5f
        )

        assertEquals(0f, hidden.scrimAlpha, 0.001f)
        assertFalse(hidden.blurEnabled)
        assertTrue(hidden.forceLowBlurBudget)

        assertEquals(0.25f, half.scrimAlpha, 0.001f)
        assertTrue(half.blurEnabled)
        assertTrue(half.forceLowBlurBudget)
        assertTrue(half.surfaceAlphaMultiplier < shown.surfaceAlphaMultiplier)

        assertEquals(0.5f, shown.scrimAlpha, 0.001f)
        assertTrue(shown.blurEnabled)
        assertFalse(shown.forceLowBlurBudget)
        assertEquals(1f, shown.surfaceAlphaMultiplier, 0.001f)
    }
}
