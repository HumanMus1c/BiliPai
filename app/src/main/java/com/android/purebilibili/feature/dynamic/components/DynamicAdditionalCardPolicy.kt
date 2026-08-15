package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.data.model.response.DynamicAdditional

internal data class DynamicAdditionalCardModel(
    val title: String,
    val subtitle: String,
    val cover: String,
    val jumpUrl: String,
    val kindLabel: String
)

internal fun resolveDynamicAdditionalCard(additional: DynamicAdditional?): DynamicAdditionalCardModel? {
    if (additional == null) return null
    return when (additional.type) {
        "ADDITIONAL_TYPE_UGC" -> additional.ugc?.takeIf { it.title.isNotBlank() }?.let {
            DynamicAdditionalCardModel(
                title = it.title,
                subtitle = it.desc_second,
                cover = it.cover,
                jumpUrl = it.jump_url,
                kindLabel = "投稿"
            )
        }
        "ADDITIONAL_TYPE_RESERVE" -> additional.reserve?.takeIf { it.state != -1 && it.title.isNotBlank() }?.let {
            DynamicAdditionalCardModel(
                title = it.title,
                subtitle = listOfNotNull(it.desc1?.text, it.desc2?.text)
                    .filter { text -> text.isNotBlank() }
                    .joinToString("  "),
                cover = "",
                jumpUrl = "",
                kindLabel = "预约"
            )
        }
        "ADDITIONAL_TYPE_GOODS" -> additional.goods?.items?.firstOrNull()?.let { goods ->
            DynamicAdditionalCardModel(
                title = goods.name.ifBlank { additional.goods.head_text.ifBlank { "商品" } },
                subtitle = goods.brief,
                cover = goods.cover,
                jumpUrl = goods.jump_url,
                kindLabel = "商品"
            )
        }
        "ADDITIONAL_TYPE_VOTE" -> additional.vote?.takeIf { it.desc.isNotBlank() }?.let {
            DynamicAdditionalCardModel(
                title = it.desc,
                subtitle = if (it.join_num > 0) "${it.join_num} 人参与" else "投票",
                cover = "",
                jumpUrl = "",
                kindLabel = "投票"
            )
        }
        "ADDITIONAL_TYPE_MATCH" -> additional.match?.takeIf { it.title.isNotBlank() }?.let {
            DynamicAdditionalCardModel(
                title = it.title,
                subtitle = it.sub_title,
                cover = "",
                jumpUrl = it.jump_url,
                kindLabel = "赛事"
            )
        }
        else -> null
    }
}
