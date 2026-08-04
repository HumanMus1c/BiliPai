package com.android.purebilibili.feature.home.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeNavigationMiuixStructureTest {

    @Test
    fun `home navigation runtime does not select Cupertino or Material icons`() {
        val homeSources = listOf(
            "BottomBar.kt",
            "TopBar.kt",
            "HomeHeader.kt",
            "HomeNavigationIconPolicy.kt",
        ).map(::sourceText)

        homeSources.forEach { source ->
            assertFalse(source.contains("CupertinoIcons"))
            assertFalse(source.contains("androidx.compose.material.icons"))
            assertFalse(source.contains("fallbackIconFamily"))
        }
    }

    @Test
    fun `home header actions use Miuix search settings and messages icons`() {
        val source = sourceText("HomeHeader.kt")

        assertTrue(source.contains("val searchIcon = MiuixIcons.Search"))
        assertTrue(source.contains("val settingsIcon = MiuixIcons.Settings"))
        assertTrue(source.contains("val inboxIcon = MiuixIcons.Messages"))
    }

    private fun sourceText(fileName: String): String = listOf(
        File("app/src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
        File("src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
    ).first { it.exists() }.readText()
}
