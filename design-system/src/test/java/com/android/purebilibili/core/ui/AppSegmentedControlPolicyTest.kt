package com.android.purebilibili.core.ui

import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSegmentedControlPolicyTest {

    @Test
    fun `material3 exposes material segmented capabilities`() {
        val policy = resolveAppSegmentedControlPolicy(AppUiStyle.MATERIAL3)

        assertTrue(policy.usesEmphasizedTitle)
        assertTrue(policy.usesMaterialFallback)
        assertTrue(policy.usesMaterialColorTokens)
        assertFalse(policy.usesNativeTabRow)
    }

    @Test
    fun `miuix exposes native tab row capability`() {
        val policy = resolveAppSegmentedControlPolicy(AppUiStyle.MIUIX)

        assertTrue(policy.usesEmphasizedTitle)
        assertTrue(policy.usesMaterialFallback)
        assertTrue(policy.usesNativeTabRow)
        assertFalse(policy.usesMaterialColorTokens)
    }

    @Test
    fun `segmented policy exposes semantic corners without a shared visual height`() {
        val material = resolveAppSegmentedControlPolicy(AppUiStyle.MATERIAL3)
        val miuix = resolveAppSegmentedControlPolicy(AppUiStyle.MIUIX)
        assertEquals(10.8.dp, material.preferredCornerRadius)
        assertEquals(13.8.dp, miuix.preferredCornerRadius)
    }

    @Test
    fun `visual height is derived from corner instead of a hardcoded 48dp`() {
        val geometry = resolveRoundedControlVisualGeometry(
            preferredCornerRadius = 14.4.dp,
            nativeMinimumHeight = 40.dp,
        )

        assertEquals(48f, geometry.height.value, absoluteTolerance = 0.001f)
        assertEquals(14.4.dp, geometry.cornerRadius)
        assertTrue(geometry.cornerRadius < geometry.height / 2)
    }

    @Test
    fun `native minimum wins when semantic corner already fits`() {
        val geometry = resolveRoundedControlVisualGeometry(
            preferredCornerRadius = 12.dp,
            nativeMinimumHeight = 42.dp,
        )

        assertEquals(42.dp, geometry.height)
        assertEquals(12.dp, geometry.cornerRadius)
    }

    @Test
    fun `native renderers do not force 48dp as visual height`() {
        val materialSource = loadSource(
            "src/main/java/com/android/purebilibili/core/ui/renderer/material3/" +
                "AppMaterial3SegmentedControl.kt"
        )
        val miuixSource = loadSource(
            "src/main/java/com/android/purebilibili/core/ui/renderer/miuix/" +
                "AppMiuixSegmentedControl.kt"
        )

        assertFalse(materialSource.contains("heightIn(min = 48.dp)"))
        assertTrue(miuixSource.contains("TabRowDefaults.TabRowHeight"))
        assertTrue(miuixSource.contains("resolveRoundedControlVisualGeometry("))
    }

    private fun loadSource(path: String): String = listOf(
        File(path),
        File("design-system/$path"),
    ).firstOrNull(File::exists)?.readText()
        ?: error("Cannot locate $path from ${File(".").absolutePath}")
}
