package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class LiquidDockViewportStructureTest {

    @Test
    fun `liquid dock viewport leaves vertical bloom and dispersion unclipped`() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/components/LiquidDockViewport.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/components/LiquidDockViewport.kt"),
        ).first { it.exists() }.readText()

        assertTrue(source.contains("if (liquidGlassEnabled) return this"))
        assertTrue(source.contains("return this.clip(shape)"))
    }
}
