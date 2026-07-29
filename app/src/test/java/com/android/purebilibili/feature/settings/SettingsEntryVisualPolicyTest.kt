package com.android.purebilibili.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.AppSemanticAccentPalette
import com.android.purebilibili.core.ui.AppSemanticVisualPolicy
import com.android.purebilibili.core.ui.resolveAppSemanticAccentPalette
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.DocOnDoc
import io.github.alexzhirkevich.cupertino.icons.outlined.Gift
import io.github.alexzhirkevich.cupertino.icons.outlined.HandTap
import io.github.alexzhirkevich.cupertino.icons.outlined.House
import io.github.alexzhirkevich.cupertino.icons.outlined.TextBubble
import io.github.alexzhirkevich.cupertino.icons.outlined.XmarkCircle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsEntryVisualPolicyTest {

    private val materialPalette = AppSemanticAccentPalette(
        primary = Color(0xFF112233),
        secondary = Color(0xFF223344),
        tertiary = Color(0xFF334455),
        error = Color(0xFF445566)
    )
    private val materialPolicy = AppSemanticVisualPolicy.material(materialPalette)

    @Test
    fun `general section entries should use distinct icons`() {
        val visuals = listOf(
            resolveSettingsEntryVisual(SettingsSearchTarget.APPEARANCE),
            resolveSettingsEntryVisual(SettingsSearchTarget.PLAYBACK),
            resolveSettingsEntryVisual(SettingsSearchTarget.BOTTOM_BAR)
        )

        assertTrue(visuals.all { it.icon != null })
        assertEquals(3, visuals.map { it.icon }.toSet().size)
    }

    @Test
    fun `settings scene entries should use semantic icons`() {
        assertEquals(
            CupertinoIcons.Outlined.House,
            resolveSettingsEntryVisual(SettingsSearchTarget.HOME_FEED).icon
        )
        assertEquals(
            CupertinoIcons.Outlined.TextBubble,
            resolveSettingsEntryVisual(SettingsSearchTarget.INTERACTION_COMMENT).icon
        )
        assertEquals(
            CupertinoIcons.Outlined.DocOnDoc,
            resolveSettingsEntryVisual(SettingsSearchTarget.DATA_BACKUP).icon
        )
    }

    @Test
    fun `blocked list should use explicit blocked semantic icon`() {
        val visual = resolveSettingsEntryVisual(SettingsSearchTarget.BLOCKED_LIST)
        assertNotNull(visual.icon)
        assertEquals(CupertinoIcons.Outlined.XmarkCircle, visual.icon)
    }

    @Test
    fun `donate should use gift semantic icon`() {
        val visual = resolveSettingsEntryVisual(SettingsSearchTarget.DONATE)
        assertNotNull(visual.icon)
        assertEquals(CupertinoIcons.Outlined.Gift, visual.icon)
    }

    @Test
    fun `md3 preset should use material semantic icons for key settings entries`() {
        assertEquals(
            Icons.Outlined.Home,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.HOME_FEED,
                materialPolicy
            ).icon
        )
        assertEquals(
            Icons.Outlined.ChatBubbleOutline,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.INTERACTION_COMMENT,
                materialPolicy
            ).icon
        )
        assertEquals(
            Icons.Outlined.Terminal,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.DIAGNOSTICS,
                materialPolicy
            ).icon
        )
        assertEquals(
            Icons.Outlined.Palette,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.APPEARANCE,
                materialPolicy
            ).icon
        )
        assertEquals(
            Icons.Outlined.Security,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.PERMISSION,
                materialPolicy
            ).icon
        )
        assertEquals(
            Icons.Outlined.TouchApp,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.FULLSCREEN_GESTURE,
                materialPolicy
            ).icon
        )
    }

    @Test
    fun `fullscreen gesture entry should use gesture semantic icon`() {
        assertEquals(
            CupertinoIcons.Outlined.HandTap,
            resolveSettingsEntryVisual(SettingsSearchTarget.FULLSCREEN_GESTURE).icon
        )
        assertEquals(
            Icons.Outlined.TouchApp,
            resolveSettingsEntryVisual(SettingsSearchTarget.FULLSCREEN_GESTURE, materialPolicy).icon
        )
    }

    @Test
    fun `md3 preset should derive settings entry tints from theme palette roles`() {
        assertEquals(
            materialPalette.tertiary,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.APPEARANCE,
                materialPolicy
            ).iconTint
        )
        assertEquals(
            materialPalette.secondary,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.PERMISSION,
                materialPolicy
            ).iconTint
        )
        assertEquals(
            materialPalette.primary,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.TELEGRAM,
                materialPolicy
            ).iconTint
        )
        assertEquals(
            materialPalette.primary,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.CLEAR_CACHE,
                materialPolicy
            ).iconTint
        )
    }

    @Test
    fun `md3 preset without dynamic color should collapse non error entry tints to primary`() {
        val staticPalette = resolveAppSemanticAccentPalette(
            colorScheme = androidx.compose.material3.lightColorScheme(
                primary = Color(0xFFAA3366),
                secondary = Color(0xFF335577),
                tertiary = Color(0xFF556677),
                error = Color(0xFFCC1122)
            ),
            useSemanticAccentRoles = false
        )
        val staticPolicy = AppSemanticVisualPolicy.material(staticPalette)

        assertEquals(
            staticPalette.primary,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.APPEARANCE,
                staticPolicy
            ).iconTint
        )
        assertEquals(
            staticPalette.primary,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.PERMISSION,
                staticPolicy
            ).iconTint
        )
        assertEquals(
            staticPalette.primary,
            resolveSettingsEntryVisual(
                SettingsSearchTarget.CLEAR_CACHE,
                staticPolicy
            ).iconTint
        )
    }

    @Test
    fun `root settings category icons should remain distinct`() {
        val rootCategoryIcons = resolveSettingsRootCategoryOrder()
            .map { resolveSettingsEntryVisual(it.searchTarget) }
            .mapNotNull { it.icon }

        assertEquals(rootCategoryIcons.size, rootCategoryIcons.toSet().size)
    }
}
