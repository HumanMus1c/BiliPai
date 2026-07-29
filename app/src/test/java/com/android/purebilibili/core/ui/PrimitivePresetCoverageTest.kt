package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Asserts every shared iOS* primitive exposes a preset-aware renderer decision
 * so feature screens get the right look on iOS / MD3 / Miuix without primitive
 * call sites changing. Compose UI tests would assert actual rendered nodes;
 * here we assert the policy layer that drives the dispatch.
 */
class PrimitivePresetCoverageTest {

    @Test
    fun unifiedRenderer_matches_uiPresetMatrix() {
        assertEquals(
            PresetPrimitiveRenderer.IOS,
            resolvePresetPrimitiveRenderer(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        )
        assertEquals(
            PresetPrimitiveRenderer.MATERIAL3,
            resolvePresetPrimitiveRenderer(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        )
        assertEquals(
            PresetPrimitiveRenderer.MIUIX_BRIDGED,
            resolvePresetPrimitiveRenderer(UiPreset.MD3, AndroidNativeVariant.MIUIX)
        )
    }

    @Test
    fun legacyLargeTitleBar_isRemovedAfterFeatureMigration() {
        val legacySource = listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/iOSLargeTitleBar.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/iOSLargeTitleBar.kt"),
        )
        assertEquals(false, legacySource.any { it.exists() })
    }

    @Test
    fun dialogActionLayoutPolicy_stillBranches() {
        // AppDialogComponents exposes a preset-aware action layout policy.
        // Verify it continues to differentiate iOS vs MD3.
        val iosLayout = resolveDialogActionLayoutPolicy(UiPreset.IOS)
        val md3Layout = resolveDialogActionLayoutPolicy(UiPreset.MD3)
        assertEquals(true, iosLayout.expandToContainer)
        assertEquals(false, md3Layout.expandToContainer)
    }

    @Test
    fun adaptiveBottomSheetVisual_stillBranches() {
        // AppSheetComponents exposes resolveAdaptiveBottomSheetVisualSpec.
        val ios = resolveAdaptiveBottomSheetVisualSpec(UiPreset.IOS)
        val md3 = resolveAdaptiveBottomSheetVisualSpec(UiPreset.MD3)
        assertEquals(14, ios.cornerRadiusDp)
        assertEquals(28, md3.cornerRadiusDp)
        assertEquals(false, ios.useMaterialDragHandle)
        assertEquals(true, md3.useMaterialDragHandle)
    }
}
