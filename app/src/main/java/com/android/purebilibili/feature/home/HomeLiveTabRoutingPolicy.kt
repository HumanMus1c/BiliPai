package com.android.purebilibili.feature.home

/**
 * 首页顶栏「直播」与底栏「直播」统一入口：
 * 顶栏点直播时不再停留在首页内嵌直播分类，而是打开底栏同一套直播首页（LiveList）。
 */
fun shouldOpenLiveListFromHomeTopTab(category: HomeCategory): Boolean =
    category == HomeCategory.LIVE

/** 首页顶栏「追番」使用番剧独立页，而不是首页 Pager 内的普通分类内容。 */
fun shouldOpenBangumiFromHomeTopTab(category: HomeCategory): Boolean =
    category == HomeCategory.ANIME
