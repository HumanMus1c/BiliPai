package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class AppVersionPolicyTest {

    @Test
    fun appVersion_usesTwoDigitYearCalendarBuildScheme() {
        val buildFile = listOf(
            File("app/build.gradle.kts"),
            File("build.gradle.kts")
        ).first { it.exists() }.readText()

        assertTrue(buildFile.contains("versionCode = 283"))
        assertTrue(buildFile.contains("versionName = \"26.0805.1\""))
        // YY.MMDD.N — 两位年，不是 2026.
        assertTrue(!buildFile.contains("versionName = \"2026."))
        assertTrue(buildFile.contains("YY.MMDD.N") || buildFile.contains("两位年"))
    }
}
