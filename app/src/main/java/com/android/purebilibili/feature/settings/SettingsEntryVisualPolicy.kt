package com.android.purebilibili.feature.settings

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.R
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.theme.iOSGreen
import com.android.purebilibili.core.theme.iOSOrange
import com.android.purebilibili.core.theme.iOSPink
import com.android.purebilibili.core.theme.iOSPurple
import com.android.purebilibili.core.theme.iOSRed
import com.android.purebilibili.core.theme.iOSTeal
import com.android.purebilibili.core.ui.AppIcons
import com.android.purebilibili.core.ui.AppSemanticAccentRole
import com.android.purebilibili.core.ui.AppSemanticVisualPolicy
import com.android.purebilibili.core.ui.rememberAppSemanticVisualPolicy

internal data class SettingsEntryVisual(
    val icon: ImageVector? = null,
    @DrawableRes val iconResId: Int? = null,
    val iconTint: Color
)

@Composable
internal fun rememberSettingsEntryTint(
    role: AppSemanticAccentRole,
    iosTint: Color,
): Color {
    val policy = rememberAppSemanticVisualPolicy()
    return remember(role, iosTint, policy) {
        policy.resolveAccent(role, fallback = iosTint)
    }
}

private fun resolveMd3SettingsEntryTintRole(
    target: SettingsSearchTarget
): AppSemanticAccentRole = when (target) {
    SettingsSearchTarget.INTERFACE_THEME,
    SettingsSearchTarget.HOME_FEED,
    SettingsSearchTarget.NAVIGATION,
    SettingsSearchTarget.APPEARANCE,
    SettingsSearchTarget.ANIMATION,
    SettingsSearchTarget.PLUGINS,
    SettingsSearchTarget.OPEN_SOURCE_HOME,
    SettingsSearchTarget.REPLAY_ONBOARDING,
    SettingsSearchTarget.TIPS -> AppSemanticAccentRole.TERTIARY

    SettingsSearchTarget.PLAYBACK_QUALITY,
    SettingsSearchTarget.FULLSCREEN_GESTURE,
    SettingsSearchTarget.INTERACTION_COMMENT,
    SettingsSearchTarget.DATA_BACKUP,
    SettingsSearchTarget.PLAYBACK,
    SettingsSearchTarget.BOTTOM_BAR,
    SettingsSearchTarget.SETTINGS_SHARE,
    SettingsSearchTarget.WEBDAV_BACKUP,
    SettingsSearchTarget.DOWNLOAD_PATH,
    SettingsSearchTarget.IMAGE_SAVE_PATH,
    SettingsSearchTarget.EXPORT_LOGS,
    SettingsSearchTarget.OPEN_SOURCE_LICENSES,
    SettingsSearchTarget.VIEW_RELEASE_NOTES,
    SettingsSearchTarget.OPEN_LINKS,
    SettingsSearchTarget.PERMISSION -> AppSemanticAccentRole.SECONDARY

    SettingsSearchTarget.PRIVACY_PERMISSION,
    SettingsSearchTarget.DIAGNOSTICS,
    SettingsSearchTarget.ABOUT_SUPPORT,
    SettingsSearchTarget.CHECK_UPDATE,
    SettingsSearchTarget.DONATE,
    SettingsSearchTarget.TELEGRAM,
    SettingsSearchTarget.TWITTER,
    SettingsSearchTarget.DISCLAIMER,
    SettingsSearchTarget.BLOCKED_LIST,
    SettingsSearchTarget.CLEAR_CACHE -> AppSemanticAccentRole.PRIMARY
}

private fun resolveIosSettingsEntryTint(
    target: SettingsSearchTarget
): Color = when (target) {
    SettingsSearchTarget.INTERFACE_THEME -> iOSPink
    SettingsSearchTarget.HOME_FEED -> iOSOrange
    SettingsSearchTarget.NAVIGATION -> iOSBlue
    SettingsSearchTarget.PLAYBACK_QUALITY -> iOSGreen
    SettingsSearchTarget.FULLSCREEN_GESTURE -> iOSPurple
    SettingsSearchTarget.INTERACTION_COMMENT -> iOSTeal
    SettingsSearchTarget.DATA_BACKUP -> iOSBlue
    SettingsSearchTarget.PRIVACY_PERMISSION -> iOSPurple
    SettingsSearchTarget.DIAGNOSTICS -> iOSTeal
    SettingsSearchTarget.ABOUT_SUPPORT -> iOSOrange
    SettingsSearchTarget.APPEARANCE -> iOSPink
    SettingsSearchTarget.ANIMATION -> iOSPink
    SettingsSearchTarget.PLAYBACK -> iOSGreen
    SettingsSearchTarget.BOTTOM_BAR -> iOSBlue
    SettingsSearchTarget.PERMISSION -> iOSTeal
    SettingsSearchTarget.BLOCKED_LIST -> iOSBlue
    SettingsSearchTarget.SETTINGS_SHARE -> iOSGreen
    SettingsSearchTarget.WEBDAV_BACKUP -> iOSBlue
    SettingsSearchTarget.DOWNLOAD_PATH -> iOSBlue
    SettingsSearchTarget.IMAGE_SAVE_PATH -> iOSTeal
    SettingsSearchTarget.CLEAR_CACHE -> iOSBlue
    SettingsSearchTarget.PLUGINS -> iOSPurple
    SettingsSearchTarget.EXPORT_LOGS -> iOSTeal
    SettingsSearchTarget.OPEN_SOURCE_LICENSES -> iOSOrange
    SettingsSearchTarget.OPEN_SOURCE_HOME -> iOSPurple
    SettingsSearchTarget.CHECK_UPDATE -> iOSBlue
    SettingsSearchTarget.VIEW_RELEASE_NOTES -> iOSTeal
    SettingsSearchTarget.REPLAY_ONBOARDING -> iOSPink
    SettingsSearchTarget.TIPS -> iOSOrange
    SettingsSearchTarget.OPEN_LINKS -> iOSTeal
    SettingsSearchTarget.DONATE -> iOSRed
    SettingsSearchTarget.TELEGRAM -> iOSBlue
    SettingsSearchTarget.TWITTER -> iOSBlue
    SettingsSearchTarget.DISCLAIMER -> iOSBlue
}

@Composable
internal fun rememberSettingsEntryVisual(
    target: SettingsSearchTarget,
): SettingsEntryVisual {
    val policy = rememberAppSemanticVisualPolicy()
    return remember(target, policy) {
        resolveSettingsEntryVisual(target, policy)
    }
}

internal fun resolveSettingsEntryVisual(
    target: SettingsSearchTarget,
    policy: AppSemanticVisualPolicy = AppSemanticVisualPolicy.Cupertino,
): SettingsEntryVisual {
    val iconTint = policy.resolveAccent(
        role = resolveMd3SettingsEntryTintRole(target),
        fallback = resolveIosSettingsEntryTint(target),
    )
    return when (target) {
        SettingsSearchTarget.TELEGRAM -> SettingsEntryVisual(
            iconResId = R.drawable.ic_telegram_mono,
            iconTint = iconTint
        )
        SettingsSearchTarget.TWITTER -> SettingsEntryVisual(
            icon = AppIcons.Twitter,
            iconTint = iconTint
        )
        else -> SettingsEntryVisual(
            icon = resolveSettingsSemanticIcon(
                role = resolveSettingsSearchTargetIconRole(target),
                iconFamily = policy.iconFamily,
            ),
            iconTint = iconTint
        )
    }
}
