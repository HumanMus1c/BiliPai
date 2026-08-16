package com.android.purebilibili.data.model.response

import com.android.purebilibili.core.network.DynamicRepostContentItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicCreateFeedRequest(
    val dyn_req: DynamicCreateFeedReq
)

@Serializable
data class DynamicCreateFeedReq(
    val content: DynamicCreateFeedContent,
    val scene: Int,
    val pics: List<DynamicCreatePic>? = null,
    val attach_card: JsonObject? = null,
    val option: DynamicCreateOption? = null,
    val upload_id: String,
    val meta: DynamicCreateMeta = DynamicCreateMeta()
)

@Serializable
data class DynamicCreateFeedContent(
    val contents: List<DynamicRepostContentItem>,
    val title: String? = null
)

@Serializable
data class DynamicCreatePic(
    val img_src: String,
    val img_width: Int,
    val img_height: Int,
    val img_size: Float
)

@Serializable
data class DynamicCreateOption(
    val private_pub: Int? = null
)

@Serializable
data class DynamicCreateMeta(
    val app_meta: DynamicCreateAppMeta = DynamicCreateAppMeta()
)

@Serializable
data class DynamicCreateAppMeta(
    val from: String = "create.dynamic.web",
    val mobi_app: String = "web"
)

@Serializable
data class DynamicCreateFeedResponse(
    val code: Int = 0,
    val message: String = "",
    val data: DynamicCreateFeedData? = null
)

@Serializable
data class DynamicCreateFeedData(
    val dyn_id: Long = 0,
    val dyn_id_str: String = "",
    val dynamic_id_str: String = ""
)

@Serializable
data class DynamicCreateVoteRequest(
    val vote_info: DynamicCreateVoteInfo
)

@Serializable
data class DynamicCreateVoteInfo(
    val title: String,
    val desc: String = "",
    val type: Int = 0,
    val choice_cnt: Int = 1,
    val duration: Int,
    val options: List<DynamicCreateVoteOption>,
    val only_fans_level: Int = 0,
    val vote_publisher: Long = 0
)

@Serializable
data class DynamicCreateVoteOption(
    val opt_desc: String,
    val img_url: String = ""
)

@Serializable
data class DynamicCreateVoteResponse(
    val code: Int = 0,
    val message: String = "",
    val data: DynamicCreateVoteData? = null
)

@Serializable
data class DynamicCreateVoteData(
    val vote_id: Long = 0
)

@Serializable
data class DynamicCreateReserveResponse(
    val code: Int = 0,
    val message: String = "",
    val data: DynamicCreateReserveData? = null
)

@Serializable
data class DynamicCreateReserveData(
    val sid: Long = 0
)

data class DynamicPublishDraft(
    val text: String,
    val title: String = "",
    val imageUris: List<String> = emptyList(),
    val voteId: Long = 0L,
    val voteTitle: String = "",
    val reserveId: Long = 0L,
    val private: Boolean = false
)

data class DynamicCreatedVote(
    val voteId: Long,
    val title: String
)

data class DynamicCreatedReserve(
    val reserveId: Long,
    val title: String
)

internal fun resolveCreatedDynamicId(data: DynamicCreateFeedData?): String {
    return data?.dyn_id_str?.trim().orEmpty()
        .ifBlank { data?.dynamic_id_str?.trim().orEmpty() }
        .ifBlank { data?.dyn_id?.takeIf { it > 0L }?.toString().orEmpty() }
}

internal fun resolveDynamicCreateScene(
    hasImages: Boolean
): Int = if (hasImages) 2 else 1

internal fun buildDynamicCreateContents(
    text: String,
    voteId: Long,
    voteTitle: String
): List<DynamicRepostContentItem> {
    val items = mutableListOf<DynamicRepostContentItem>()
    val trimmed = text.trim()
    if (trimmed.isNotEmpty()) {
        items += DynamicRepostContentItem(raw_text = trimmed, type = 1, biz_id = "")
    }
    if (voteId > 0L) {
        items += DynamicRepostContentItem(
            raw_text = voteTitle.ifBlank { "投票" },
            type = 4,
            biz_id = voteId.toString()
        )
        items += DynamicRepostContentItem(raw_text = " ", type = 1, biz_id = "")
    }
    return items
}