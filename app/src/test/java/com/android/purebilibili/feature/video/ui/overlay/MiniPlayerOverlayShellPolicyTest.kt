package com.android.purebilibili.feature.video.ui.overlay

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiniPlayerOverlayShellPolicyTest {

    @Test
    fun materialShellKeepsLayoutCornersAndLegacyAccent() {
        val layout = resolveMiniPlayerOverlayLayoutPolicy(widthDp = 393)
        val visual = resolveMiniPlayerOverlayShellVisual(
            layout = layout,
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MATERIAL3
        )

        assertEquals(layout.cardCornerRadiusDp, visual.cardCornerRadiusDp)
        assertEquals(layout.cardElevationDp, visual.cardElevationDp)
        assertEquals(layout.cardShadowDp, visual.cardShadowDp)
        assertEquals(layout.seekHintCornerRadiusDp, visual.seekHintCornerRadiusDp)
        assertFalse(visual.useThemePrimaryAccent)
    }

    @Test
    fun miuixShellUsesFlatterElevationAndThemePrimaryAccent() {
        val layout = resolveMiniPlayerOverlayLayoutPolicy(widthDp = 393)
        val visual = resolveMiniPlayerOverlayShellVisual(
            layout = layout,
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MIUIX
        )

        assertTrue(visual.cardCornerRadiusDp >= layout.cardCornerRadiusDp)
        assertEquals(0, visual.cardElevationDp)
        assertTrue(visual.cardShadowDp < layout.cardShadowDp)
        assertTrue(visual.seekHintCornerRadiusDp >= layout.seekHintCornerRadiusDp)
        assertTrue(visual.useThemePrimaryAccent)
    }

    @Test
    fun miniPlayerOverlayWiresShellVisualAndSurfaceTokens() {
        val source = load(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/overlay/MiniPlayerOverlay.kt"
        )
        assertTrue(source.contains("resolveMiniPlayerOverlayShellVisual("))
        assertTrue(source.contains("AppSurfaceTokens.primary()"))
        assertTrue(source.contains("shellVisual.cardCornerRadiusDp"))
        assertTrue(source.contains("shellVisual.useThemePrimaryAccent"))
    }

    private fun load(path: String): String {
        val normalized = path.removePrefix("app/")
        return listOf(File(path), File(normalized))
            .first { it.exists() }
            .readText()
    }
}
