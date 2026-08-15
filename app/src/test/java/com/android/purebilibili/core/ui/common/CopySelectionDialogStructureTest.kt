package com.android.purebilibili.core.ui.common

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CopySelectionDialogStructureTest {

    @Test
    fun copySelectionDialogUsesAppAlertChrome() {
        val source = File(
            "src/main/java/com/android/purebilibili/core/ui/common/CopySelectionDialog.kt"
        ).readText()
        assertTrue(source.contains("AppAlertDialog("))
        assertTrue(source.contains("AppTextButton("))
        assertTrue(source.contains("AppText("))
        assertFalse(source.contains("androidx.compose.material3.AlertDialog"))
        assertFalse(source.contains("TextButton("))
    }
}
