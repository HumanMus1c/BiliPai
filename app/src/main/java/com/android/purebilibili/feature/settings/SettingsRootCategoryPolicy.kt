package com.android.purebilibili.feature.settings

internal enum class SettingsRootCategory(
    val title: String,
    val subtitle: String,
    val searchTarget: SettingsSearchTarget
) {
    INTERFACE_HOME(
        title = "界面与首页",
        subtitle = "主题、动效、首页展示、壁纸与刷新",
        searchTarget = SettingsSearchTarget.INTERFACE_THEME
    ),
    DYNAMIC_RECOMMEND(
        title = "动态与推荐",
        subtitle = "推荐流类型、动态栏位、图片预览与 UP 栏",
        searchTarget = SettingsSearchTarget.HOME_FEED
    ),
    PLAYBACK_INTERACTION(
        title = "播放与互动",
        subtitle = "画质、解码、倍速、连播、评论与视频互动",
        searchTarget = SettingsSearchTarget.PLAYBACK_QUALITY
    ),
    NAVIGATION_GESTURE(
        title = "导航与手势",
        subtitle = "底栏、顶部标签、平板侧边栏、全屏与手势",
        searchTarget = SettingsSearchTarget.NAVIGATION
    ),
    DATA_PRIVACY(
        title = "数据与隐私",
        subtitle = "设置分享、WebDAV、下载缓存、权限、无痕与黑名单",
        searchTarget = SettingsSearchTarget.DATA_BACKUP
    ),
    EXTENSION_ABOUT(
        title = "扩展与关于",
        subtitle = "插件、诊断、日志、版本、更新、社群与支持",
        searchTarget = SettingsSearchTarget.DIAGNOSTICS
    )
}

internal fun resolveSettingsRootCategoryOrder(): List<SettingsRootCategory> = listOf(
    SettingsRootCategory.INTERFACE_HOME,
    SettingsRootCategory.DYNAMIC_RECOMMEND,
    SettingsRootCategory.PLAYBACK_INTERACTION,
    SettingsRootCategory.NAVIGATION_GESTURE,
    SettingsRootCategory.DATA_PRIVACY,
    SettingsRootCategory.EXTENSION_ABOUT
)

internal fun resolveTabletSettingsRootCategoryOrder(): List<SettingsRootCategory> =
    resolveSettingsRootCategoryOrder()

internal fun resolveSettingsRootCategoryForSearchTarget(
    target: SettingsSearchTarget
): SettingsRootCategory? = when (target) {
    SettingsSearchTarget.INTERFACE_THEME,
    SettingsSearchTarget.APPEARANCE,
    SettingsSearchTarget.ANIMATION -> SettingsRootCategory.INTERFACE_HOME

    SettingsSearchTarget.HOME_FEED -> SettingsRootCategory.DYNAMIC_RECOMMEND

    SettingsSearchTarget.PLAYBACK_QUALITY,
    SettingsSearchTarget.PLAYBACK,
    SettingsSearchTarget.INTERACTION_COMMENT -> SettingsRootCategory.PLAYBACK_INTERACTION

    SettingsSearchTarget.NAVIGATION,
    SettingsSearchTarget.BOTTOM_BAR,
    SettingsSearchTarget.FULLSCREEN_GESTURE -> SettingsRootCategory.NAVIGATION_GESTURE

    SettingsSearchTarget.DATA_BACKUP,
    SettingsSearchTarget.SETTINGS_SHARE,
    SettingsSearchTarget.WEBDAV_BACKUP,
    SettingsSearchTarget.DOWNLOAD_PATH,
    SettingsSearchTarget.IMAGE_SAVE_PATH,
    SettingsSearchTarget.CLEAR_CACHE,
    SettingsSearchTarget.PRIVACY_PERMISSION,
    SettingsSearchTarget.PERMISSION,
    SettingsSearchTarget.BLOCKED_LIST -> SettingsRootCategory.DATA_PRIVACY

    SettingsSearchTarget.DIAGNOSTICS,
    SettingsSearchTarget.PLUGINS,
    SettingsSearchTarget.EXPORT_LOGS,
    SettingsSearchTarget.ABOUT_SUPPORT,
    SettingsSearchTarget.OPEN_SOURCE_LICENSES,
    SettingsSearchTarget.OPEN_SOURCE_HOME,
    SettingsSearchTarget.CHECK_UPDATE,
    SettingsSearchTarget.VIEW_RELEASE_NOTES,
    SettingsSearchTarget.REPLAY_ONBOARDING,
    SettingsSearchTarget.TIPS,
    SettingsSearchTarget.OPEN_LINKS,
    SettingsSearchTarget.DISCLAIMER,
    SettingsSearchTarget.TELEGRAM,
    SettingsSearchTarget.TWITTER,
    SettingsSearchTarget.DONATE -> SettingsRootCategory.EXTENSION_ABOUT
}

internal fun isSceneSettingsSearchTarget(target: SettingsSearchTarget): Boolean = target in setOf(
    SettingsSearchTarget.INTERFACE_THEME,
    SettingsSearchTarget.HOME_FEED,
    SettingsSearchTarget.PLAYBACK_QUALITY,
    SettingsSearchTarget.NAVIGATION,
    SettingsSearchTarget.DATA_BACKUP,
    SettingsSearchTarget.DIAGNOSTICS
)

internal fun resolveSettingsRootCategoryListIndex(category: SettingsRootCategory): Int {
    val orderIndex = resolveSettingsRootCategoryOrder().indexOf(category)
    if (orderIndex < 0) return 0
    return 2 + orderIndex
}
