package com.android.purebilibili.data.repository

import com.android.purebilibili.data.model.response.DynamicItem

internal fun shouldFetchStandardDetailForPlainTextDynamic(item: DynamicItem): Boolean {
    val content = item.modules.module_dynamic ?: return false
    return item.type == "DYNAMIC_TYPE_WORD" && content.desc?.text?.isNotBlank() == true
}

internal fun mergeDynamicDetailWithLongerDesc(
    desktopItem: DynamicItem,
    standardItem: DynamicItem,
): DynamicItem {
    val desktopContent = desktopItem.modules.module_dynamic ?: return desktopItem
    val standardDesc = standardItem.modules.module_dynamic?.desc ?: return desktopItem
    val desktopDesc = desktopContent.desc
    if (standardDesc.text.length <= desktopDesc?.text?.length ?: 0) return desktopItem

    return desktopItem.copy(
        modules = desktopItem.modules.copy(
            module_dynamic = desktopContent.copy(desc = standardDesc)
        )
    )
}

internal fun shouldFallbackForDynamicDetail(item: DynamicItem): Boolean {
    val modules = item.modules
    val hasDescText = modules.module_dynamic?.desc?.text?.isNotBlank() == true
    val hasMajorContent = modules.module_dynamic?.major != null
    val hasOrig = item.orig != null

    // 可渲染内容都没有时，说明解析结构可能不兼容，应该走 fallback
    val hasRenderableContent = hasDescText || hasMajorContent || hasOrig
    return !hasRenderableContent
}

internal fun shouldFetchOpusDetailForDynamicDetail(item: DynamicItem): Boolean {
    val major = item.modules.module_dynamic?.major ?: return false
    return major.type == "MAJOR_TYPE_OPUS" || major.opus != null
}
