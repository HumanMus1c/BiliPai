package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppPrimitiveComponentsStructureTest {

    @Test
    fun remainingLegacyPrimitiveApisStayAvailableDuringRendererMigration() {
        val source = loadSource()

        assertTrue(source.contains("fun AppButton("))
        assertTrue(source.contains(") = Button("))
        assertTrue(source.contains("colors: ButtonColors"))
        assertTrue(source.contains("fun AppIconButton("))
        assertTrue(source.contains(") = IconButton("))
        assertTrue(source.contains("fun AppFilledIconButton("))
        assertTrue(source.contains(") = FilledIconButton("))
        assertTrue(source.contains("fun AppTextButton("))
        assertTrue(source.contains(") = TextButton("))
        assertTrue(source.contains("fun AppOutlinedTextField("))
        assertTrue(source.contains("shouldUseMiuixOutlinedTextField("))
        assertTrue(source.contains("MiuixTextField("))
        assertTrue(source.contains("OutlinedTextField("))
        assertTrue(source.contains("fun AppDropdownMenu("))
        assertTrue(source.contains("fun AppModalNavigationDrawer("))
        assertTrue(source.contains("fun AppNavigationDrawerItem("))
        assertTrue(source.contains(") = NavigationDrawerItem("))
        assertTrue(source.contains("fun AppCircularProgressIndicator("))
        assertTrue(source.contains(") = CircularProgressIndicator("))
        assertTrue(source.contains("fun AppLinearProgressIndicator("))
        assertTrue(source.contains(") = LinearProgressIndicator("))
        assertTrue(source.contains("fun AppCheckbox("))
        assertTrue(source.contains(") = Checkbox("))
        assertTrue(source.contains("fun AppSwitch("))
        assertTrue(source.contains("colors: SwitchColors = SwitchDefaults.colors()"))
        assertTrue(source.contains("showThumbIcon: Boolean = true"))
        assertTrue(source.contains("Icons.Filled.Check"))
        assertTrue(source.contains("Icons.Filled.Close"))
        assertTrue(source.contains("resolveAppAdaptiveSwitchTreatment("))
        assertTrue(source.contains("MiuixSwitch("))
        assertTrue(source.contains("Switch("))
        assertTrue(source.contains("fun AppRadioButton("))
        assertTrue(source.contains(") = RadioButton("))
        assertTrue(source.contains("fun AppSlider("))
        assertTrue(source.contains("resolveAppSliderRenderer("))
        assertTrue(source.contains("MiuixSlider("))
        assertTrue(source.contains("Slider("))
        assertTrue(source.contains("fun AppOutlinedButton("))
        assertTrue(source.contains(") = OutlinedButton("))
        assertTrue(source.contains("fun AppCard("))
        assertTrue(source.contains("content: @Composable ColumnScope.() -> Unit"))
        assertTrue(source.contains(") = Card("))
        assertTrue(source.contains("fun AppAssistChip("))
        assertTrue(source.contains(") = AssistChip("))
        assertTrue(source.contains("fun AppFilterChip("))
        assertTrue(source.contains(") = FilterChip("))
        assertTrue(source.contains("fun AppBadge("))
        assertTrue(source.contains(") = Badge("))
        assertTrue(source.contains("fun AppFloatingActionButton("))
        assertTrue(source.contains(") = FloatingActionButton("))
        assertTrue(source.contains("fun AppSmallFloatingActionButton("))
        assertTrue(source.contains(") = SmallFloatingActionButton("))
        assertTrue(source.contains("fun AppTab("))
        assertTrue(source.contains(") = Tab("))
        assertTrue(source.contains("fun AppPrimaryTabRow("))
        assertTrue(source.contains(") = PrimaryTabRow("))
        assertTrue(source.contains("fun AppPrimaryScrollableTabRow("))
        assertTrue(source.contains(") = PrimaryScrollableTabRow("))
        assertTrue(source.contains("fun AppSuggestionChip("))
        assertTrue(source.contains(") = SuggestionChip("))
    }

    @Test
    fun surfaceFacadeRoutesEachThemeToItsNativeRenderer() {
        val facade = loadSource("components/AppSurface.kt")
        val material = loadSource("renderer/material3/AppMaterial3Surface.kt")
        val miuix = loadSource("renderer/miuix/AppMiuixSurface.kt")

        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Surface("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppMiuixSurface("))
        assertTrue(facade.contains("Color.Unspecified"))
        assertTrue(facade.contains("Dp.Unspecified"))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.Surface"))
        assertTrue(material.contains("import androidx.compose.material3.HorizontalDivider"))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.Surface"))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.HorizontalDivider"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    private fun loadSource(): String {
        val path = "src/main/java/com/android/purebilibili/core/ui/components/AppPrimitiveComponents.kt"
        return listOf(
            File(path),
            File("design-system/$path"),
        ).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate AppPrimitiveComponents.kt from ${File(".").absolutePath}")
    }

    private fun loadSource(relativePath: String): String {
        val path = "src/main/java/com/android/purebilibili/core/ui/$relativePath"
        return listOf(
            File(path),
            File("design-system/$path"),
        ).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
