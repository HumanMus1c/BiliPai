package com.android.purebilibili.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.android.purebilibili.core.network.DynamicRepostContentItem
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.store.TokenManager
import com.android.purebilibili.data.model.response.DynamicCreateFeedContent
import com.android.purebilibili.data.model.response.DynamicCreateFeedReq
import com.android.purebilibili.data.model.response.DynamicCreateFeedRequest
import com.android.purebilibili.data.model.response.DynamicCreateOption
import com.android.purebilibili.data.model.response.DynamicCreatePic
import com.android.purebilibili.data.model.response.DynamicCreateVoteInfo
import com.android.purebilibili.data.model.response.DynamicCreateVoteOption
import com.android.purebilibili.data.model.response.DynamicCreateVoteRequest
import com.android.purebilibili.data.model.response.DynamicCreatedReserve
import com.android.purebilibili.data.model.response.DynamicCreatedVote
import com.android.purebilibili.data.model.response.DynamicPublishDraft
import com.android.purebilibili.data.model.response.buildDynamicCreateContents
import com.android.purebilibili.data.model.response.resolveCreatedDynamicId
import com.android.purebilibili.data.model.response.resolveDynamicCreateScene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random

object DynamicCreateRepository {
    suspend fun publish(context: Context, draft: DynamicPublishDraft): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val csrf = TokenManager.csrfCache.orEmpty()
            if (csrf.isBlank()) error("请先登录")
            val mid = TokenManager.midCache ?: 0L
            val pics = draft.imageUris.map { uriString ->
                uploadImage(context, Uri.parse(uriString))
            }
            val contents = buildDynamicCreateContents(
                text = draft.text,
                voteId = draft.voteId,
                voteTitle = draft.voteTitle
            )
            if (contents.isEmpty() && pics.isEmpty()) {
                error("内容不能为空")
            }
            val request = DynamicCreateFeedRequest(
                dyn_req = DynamicCreateFeedReq(
                    content = DynamicCreateFeedContent(
                        contents = contents.ifEmpty {
                            listOf(DynamicRepostContentItem(raw_text = " ", type = 1, biz_id = ""))
                        },
                        title = draft.title.trim().takeIf { it.isNotEmpty() }
                    ),
                    scene = resolveDynamicCreateScene(pics.isNotEmpty()),
                    pics = pics.takeIf { it.isNotEmpty() },
                    attach_card = resolveReserveAttachCard(draft.reserveId),
                    option = if (draft.private) DynamicCreateOption(private_pub = 1) else null,
                    upload_id = "${mid}_${System.currentTimeMillis() / 1000}_${Random.nextInt(1000, 10000)}"
                )
            )
            val response = NetworkModule.dynamicApi.createFeedDynamic(csrf = csrf, body = request)
            if (response.code != 0) {
                error(response.message.ifBlank { "发布失败" })
            }
            resolveCreatedDynamicId(response.data).ifBlank { "ok" }
        }
    }

    suspend fun createVote(
        title: String,
        options: List<String>,
        description: String = "",
        choiceCount: Int = 1,
        durationSeconds: Int
    ): Result<DynamicCreatedVote> = withContext(Dispatchers.IO) {
        runCatching {
            val csrf = TokenManager.csrfCache.orEmpty()
            if (csrf.isBlank()) error("请先登录")
            val cleanedOptions = options.map { it.trim() }.filter { it.isNotEmpty() }
            if (title.isBlank() || cleanedOptions.size < 2) {
                error("至少填写标题和两个选项")
            }
            val response = NetworkModule.dynamicApi.createVote(
                csrf = csrf,
                body = DynamicCreateVoteRequest(
                    vote_info = DynamicCreateVoteInfo(
                        title = title.trim(),
                        desc = description.trim(),
                        choice_cnt = choiceCount.coerceIn(1, cleanedOptions.size),
                        duration = durationSeconds.coerceAtLeast(60),
                        options = cleanedOptions.map { DynamicCreateVoteOption(opt_desc = it) },
                        vote_publisher = TokenManager.midCache ?: 0L
                    )
                )
            )
            if (response.code != 0) {
                error(response.message.ifBlank { "创建投票失败" })
            }
            val voteId = response.data?.vote_id ?: 0L
            if (voteId <= 0L) error("投票创建失败")
            DynamicCreatedVote(voteId = voteId, title = title.trim())
        }
    }

    suspend fun createReserve(
        title: String,
        livePlanStartTimeSeconds: Long,
        subType: Int = 0
    ): Result<DynamicCreatedReserve> = withContext(Dispatchers.IO) {
        runCatching {
            val csrf = TokenManager.csrfCache.orEmpty()
            if (csrf.isBlank()) error("请先登录")
            if (title.isBlank()) error("请填写预约标题")
            val response = NetworkModule.dynamicApi.createReserve(
                subType = subType,
                title = title.trim(),
                livePlanStartTime = livePlanStartTimeSeconds,
                csrf = csrf
            )
            if (response.code != 0) {
                error(response.message.ifBlank { "创建预约失败" })
            }
            val reserveId = response.data?.sid ?: 0L
            if (reserveId <= 0L) error("预约创建失败")
            DynamicCreatedReserve(reserveId = reserveId, title = title.trim())
        }
    }

    private suspend fun uploadImage(context: Context, uri: Uri): DynamicCreatePic {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法读取图片")
        if (bytes.isEmpty()) error("图片内容为空")
        if (bytes.size > 15 * 1024 * 1024) error("图片过大（单张最大 15MB）")
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val fileName = queryDisplayName(context, uri) ?: "dyn_${System.currentTimeMillis()}.jpg"
        val uploaded = CommentRepository.uploadCommentImage(
            fileName = fileName,
            mimeType = mimeType,
            bytes = bytes
        ).getOrElse { throw it }
        return DynamicCreatePic(
            img_src = uploaded.imgSrc,
            img_width = uploaded.imgWidth,
            img_height = uploaded.imgHeight,
            img_size = uploaded.imgSize
        )
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
                }
        }.getOrNull()
    }

    private fun resolveReserveAttachCard(reserveId: Long): JsonObject? {
        if (reserveId <= 0L) return null
        return buildJsonObject {
            put("common_card", buildJsonObject {
                put("type", 14)
                put("biz_id", reserveId)
                put("reserve_source", 0)
                put("reserve_lottery", 0)
            })
        }
    }
}