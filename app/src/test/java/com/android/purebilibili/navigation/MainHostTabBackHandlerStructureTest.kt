package com.android.purebilibili.navigation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainHostTabBackHandlerStructureTest {

    @Test
    fun mainHostTabBackHandler_usesNavigationBackHandlerInsteadOfBackHandler() {
        val source = mainHostTabBackHandlerSource()

        assertTrue(source.contains("NavigationBackHandler("))
        assertTrue(source.contains("rememberNavigationEventState(NavigationEventInfo.None)"))
        assertFalse(source.contains("import androidx.activity.compose.BackHandler"))
        assertFalse(source.contains("BackHandler("))
    }

    private fun mainHostTabBackHandlerSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation/MainHostTabBackHandler.kt"),
            File("src/main/java/com/android/purebilibili/navigation/MainHostTabBackHandler.kt"),
        ).first { it.exists() }.readText()
    }
}