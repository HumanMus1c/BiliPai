package com.android.purebilibili.core.ui

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AppSemanticVisualPolicyTest {

    private val palette = AppSemanticAccentPalette(
        primary = Color(0xFF112233),
        secondary = Color(0xFF223344),
        tertiary = Color(0xFF334455),
        error = Color(0xFF445566),
    )

    @Test
    fun iosUsesCupertinoIconsAndFallbackAccent() {
        val policy = resolveAppSemanticVisualPolicy(
            uiPreset = UiPreset.IOS,
            androidNativeVariant = AndroidNativeVariant.MIUIX,
            materialPalette = palette,
        )

        assertEquals(AppSemanticIconFamily.CUPERTINO, policy.iconFamily)
        assertFalse(policy.prefersGroupedListCards)
        assertNull(policy.accentPalette)
        assertEquals(
            Color(0xFFABCDEF),
            policy.resolveAccent(AppSemanticAccentRole.TERTIARY, Color(0xFFABCDEF)),
        )
    }

    @Test
    fun material3AndMiuixUseMaterialSemanticVisuals() {
        listOf(AndroidNativeVariant.MATERIAL3, AndroidNativeVariant.MIUIX).forEach { variant ->
            val policy = resolveAppSemanticVisualPolicy(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = variant,
                materialPalette = palette,
            )

            assertEquals(AppSemanticIconFamily.MATERIAL, policy.iconFamily)
            assertEquals(
                variant == AndroidNativeVariant.MIUIX,
                policy.prefersGroupedListCards,
            )
            assertEquals(palette, policy.accentPalette)
        }
    }

    @Test
    fun chromeLiquidGlassHonorsIndependentAndGlobalCapabilities() {
        assertFalse(
            resolveAppChromeLiquidGlassEnabled(
                supportsIndependentLiquidGlass = false,
                individualEnabled = true,
                androidNativeEnabled = false,
            )
        )
        assertEquals(
            true,
            resolveAppChromeLiquidGlassEnabled(
                supportsIndependentLiquidGlass = true,
                individualEnabled = true,
                androidNativeEnabled = false,
            )
        )
        assertEquals(
            true,
            resolveAppChromeLiquidGlassEnabled(
                supportsIndependentLiquidGlass = false,
                individualEnabled = false,
                androidNativeEnabled = true,
            )
        )
    }

    @Test
    fun staticMaterialPaletteCollapsesNonErrorAccentsToPrimary() {
        val scheme = lightColorScheme(
            primary = Color(0xFFAA3366),
            secondary = Color(0xFF335577),
            tertiary = Color(0xFF556677),
            error = Color(0xFFCC1122),
        )
        val staticPalette = resolveAppSemanticAccentPalette(
            colorScheme = scheme,
            useSemanticAccentRoles = false,
        )

        assertEquals(scheme.primary, staticPalette.primary)
        assertEquals(scheme.primary, staticPalette.secondary)
        assertEquals(scheme.primary, staticPalette.tertiary)
        assertEquals(scheme.error, staticPalette.error)
    }

    @Test
    fun settingsSemanticPoliciesDoNotReadConcreteStyles() {
        val paths = listOf(
            "app/src/main/java/com/android/purebilibili/feature/settings/SettingsEntryVisualPolicy.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/SettingsSemanticIconPolicy.kt",
        )
        val styleDependency = Regex(
            """\b(UiPreset|UiStyle|AndroidNativeVariant|LocalUiPreset|LocalUiStyle|LocalAndroidNativeVariant)\b"""
        )

        paths.forEach { path ->
            val source = loadSource(path)
            assertFalse(styleDependency.containsMatchIn(source), "Concrete style dependency remains in $path")
        }
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        return listOf(File(path), File(normalizedPath), File("../$path"))
            .firstOrNull(File::exists)
            ?.readText()
            ?.replace("\r\n", "\n")
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
