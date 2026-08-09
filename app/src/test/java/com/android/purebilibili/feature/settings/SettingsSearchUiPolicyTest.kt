package com.android.purebilibili.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircle
import com.android.purebilibili.R
import com.android.purebilibili.core.ui.AppIcons
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SettingsSearchUiPolicyTest {

    @Test
    fun telegramSearchResult_reusesSettingsSectionIconResource() {
        val visual = resolveSettingsEntryVisual(SettingsSearchTarget.TELEGRAM)

        assertEquals(R.drawable.ic_telegram_mono, visual.iconResId)
    }

    @Test
    fun twitterSearchResult_usesTwitterIcon() {
        val visual = resolveSettingsEntryVisual(SettingsSearchTarget.TWITTER)

        assertSame(AppIcons.Twitter, visual.icon)
    }

    @Test
    fun playbackSearchResult_usesPlaybackIcon() {
        val visual = resolveSettingsEntryVisual(SettingsSearchTarget.PLAYBACK)

        assertSame(Icons.Outlined.PlayCircle, visual.icon)
    }

    @Test
    fun webDavSearchResult_reusesDataStorageSectionIcon() {
        val visual = resolveSettingsEntryVisual(SettingsSearchTarget.WEBDAV_BACKUP)

        assertSame(Icons.Outlined.CloudUpload, visual.icon)
    }

    @Test
    fun homeFeedSearchResult_usesHomeSemanticIcon() {
        val visual = resolveSettingsEntryVisual(SettingsSearchTarget.HOME_FEED)

        assertSame(Icons.Outlined.Home, visual.icon)
    }
}
