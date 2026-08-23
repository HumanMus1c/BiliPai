package com.android.purebilibili.core.plugin.skin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SkinCatalogPolicyTest {

    private fun entry(
        packageZipUrl: String? = null,
        packageUrlCdn: String? = null,
        colorMode: String? = null,
        capabilities: SkinCatalogCapabilities = SkinCatalogCapabilities()
    ) = SkinCatalogEntry(
        id = "t",
        name = "主题",
        previewUrl = "https://example.com/p.jpg",
        packageZipUrl = packageZipUrl,
        packageUrlCdn = packageUrlCdn,
        colorMode = colorMode,
        capabilities = capabilities
    )

    @Test
    fun preferredPackageUrlPrefersRawZipOverCdn() {
        assertEquals(
            "https://raw.githubusercontent.com/x.zip",
            entry(
                packageZipUrl = "https://raw.githubusercontent.com/x.zip",
                packageUrlCdn = "http://i0.hdslb.com/y.zip"
            ).preferredPackageUrl()
        )
    }

    @Test
    fun preferredPackageUrlFallsBackToCdnWhenNoRawZip() {
        assertEquals(
            "http://i0.hdslb.com/y.zip",
            entry(packageUrlCdn = "http://i0.hdslb.com/y.zip").preferredPackageUrl()
        )
    }

    @Test
    fun preferredPackageUrlIsNullWhenNeitherPresent() {
        assertNull(entry().preferredPackageUrl())
    }

    @Test
    fun isDarkReflectsColorMode() {
        assertTrue(entry(colorMode = "dark").isDark)
        assertFalse(entry(colorMode = "light").isDark)
        assertFalse(entry(colorMode = null).isDark)
        assertFalse(entry(colorMode = "DARK").isDark.not())
    }

    @Test
    fun displayNameFallsBackToUtf8RepositoryDirectoryForLegacyMojibake() {
        val corrupted = SkinCatalogEntry(
            id = "12周年夏日狂欢",
            name = "12周\uFFFD\uFFFD\uFFFD",
            previewUrl = "https://example.com/p.jpg"
        )

        assertEquals("12周年夏日狂欢", corrupted.displayName)
        assertEquals("主题", entry().displayName)
    }

    @Test
    fun capabilityLabelsListedInDisplayOrder() {
        val caps = SkinCatalogCapabilities(
            bottomBarIcons = true,
            profileBackground = true,
            topAtmosphere = true,
            sideBackground = true
        )
        assertEquals(
            listOf("底栏图标", "个人页背景", "顶部氛围", "侧栏背景"),
            caps.labels()
        )
    }

    @Test
    fun emptyCapabilitiesHaveNoLabels() {
        val caps = SkinCatalogCapabilities()
        assertTrue(caps.isEmpty)
        assertTrue(caps.labels().isEmpty())
    }
}
