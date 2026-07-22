package com.android.purebilibili.feature.video.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.network.WbiUtils
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.ui.bottomSheetContentEnterTransition
import com.android.purebilibili.core.ui.bottomSheetContentExitTransition
import com.android.purebilibili.core.ui.bottomSheetScrimEnterTransition
import com.android.purebilibili.core.ui.bottomSheetScrimExitTransition
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.Owner
import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.SpaceVideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility

/**
 * 竖屏详情点 UP 头像：半屏预览（关注 / 进入空间 / 近期投稿网格）。
 * 深浅色走 [resolveUpPreviewSheetSurfaceColors]。
 */
@Composable
fun UpPreviewSheet(
    visible: Boolean,
    owner: Owner,
    isFollowing: Boolean,
    followerCount: Int? = null,
    videoCount: Int? = null,
    likeCount: Int? = null,
    seedVideos: List<RelatedVideo> = emptyList(),
    onDismiss: () -> Unit,
    onFollowClick: () -> Unit,
    onEnterSpace: (Long) -> Unit,
    onVideoClick: (bvid: String, cid: Long) -> Unit,
) {
    if (!visible && owner.mid <= 0L) return

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val colors = resolveUpPreviewSheetSurfaceColors(MaterialTheme.colorScheme)

    var loading by remember(owner.mid) { mutableStateOf(false) }
    var videos by remember(owner.mid) {
        mutableStateOf(
            seedVideos
                .filter { it.owner.mid == owner.mid || it.owner.mid <= 0L }
                .mapNotNull { it.toUpPreviewVideoItem() }
                .take(12)
        )
    }
    var resolvedFollower by remember(owner.mid, followerCount) {
        mutableStateOf(followerCount)
    }
    var resolvedVideoCount by remember(owner.mid, videoCount) {
        mutableStateOf(videoCount)
    }
    var resolvedLikeCount by remember(owner.mid, likeCount) {
        mutableStateOf(likeCount)
    }

    LaunchedEffect(visible, owner.mid) {
        if (!visible || owner.mid <= 0L) return@LaunchedEffect
        loading = videos.isEmpty()
        withContext(Dispatchers.IO) {
            runCatching {
                val nav = NetworkModule.api.getNavInfo()
                val wbi = nav.data?.wbi_img
                val imgKey = wbi?.img_url?.substringAfterLast("/")?.substringBefore(".")
                val subKey = wbi?.sub_url?.substringAfterLast("/")?.substringBefore(".")
                if (!imgKey.isNullOrBlank() && !subKey.isNullOrBlank()) {
                    val videoParams = WbiUtils.sign(
                        mapOf(
                            "mid" to owner.mid.toString(),
                            "pn" to "1",
                            "ps" to "12",
                            "order" to "pubdate",
                        ),
                        imgKey,
                        subKey,
                    )
                    val videoResp = NetworkModule.spaceApi.getSpaceVideos(videoParams)
                    if (videoResp.code == 0) {
                        val list = videoResp.data?.list?.vlist.orEmpty()
                            .map { it.toUpPreviewVideoItem() }
                        if (list.isNotEmpty()) {
                            videos = list
                        }
                        val count = videoResp.data?.page?.count
                        if (count != null && count > 0) {
                            resolvedVideoCount = count
                        }
                    }
                }
                val relation = NetworkModule.spaceApi.getRelationStat(owner.mid)
                if (relation.code == 0) {
                    relation.data?.follower?.let { resolvedFollower = it }
                }
                val upStat = NetworkModule.spaceApi.getUpStat(owner.mid)
                if (upStat.code == 0) {
                    upStat.data?.likes?.let {
                        resolvedLikeCount = it.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
                    }
                }
            }
        }
        loading = false
    }

    BackHandler(enabled = visible) { onDismiss() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = bottomSheetScrimEnterTransition(uiPreset, androidNativeVariant),
            exit = bottomSheetScrimExitTransition(uiPreset, androidNativeVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.scrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = bottomSheetContentEnterTransition(uiPreset, androidNativeVariant),
            exit = bottomSheetContentExitTransition(uiPreset, androidNativeVariant),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.72f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                color = colors.sheetColor,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = WindowInsets.navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding()
                        )
                ) {
                    // 顶图：用头像/封面做柔和渐变，深浅色都可读
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        colors.followFillColor.copy(alpha = 0.28f),
                                        colors.sheetColor,
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                                .size(width = 36.dp, height = 4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.supportingColor.copy(alpha = 0.35f))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = FormatUtils.fixImageUrl(owner.face),
                            contentDescription = "UP主头像",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.coverPlaceholderColor),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = owner.name.ifBlank { "UP主" },
                                color = colors.titleColor,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = resolveUpPreviewStatLine(
                                    followerCount = resolvedFollower,
                                    videoCount = resolvedVideoCount,
                                    likeCount = resolvedLikeCount,
                                ).ifBlank { " " },
                                color = colors.supportingColor,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            onClick = onFollowClick,
                            shape = RoundedCornerShape(999.dp),
                            color = if (isFollowing) {
                                colors.followingFillColor
                            } else {
                                colors.followFillColor
                            },
                        ) {
                            Text(
                                text = if (isFollowing) "已关注" else "+ 关注",
                                color = if (isFollowing) {
                                    colors.followingContentColor
                                } else {
                                    colors.followContentColor
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "进入空间  >",
                            color = colors.enterSpaceColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEnterSpace(owner.mid) }
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.dividerColor)
                    )

                    when {
                        loading && videos.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.followFillColor,
                                )
                            }
                        }
                        videos.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "暂无投稿",
                                    color = colors.supportingColor,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(videos, key = { it.bvid }) { item ->
                                    UpPreviewVideoCard(
                                        item = item,
                                        colors = colors,
                                        onClick = {
                                            resolveUpPreviewVideoClickTarget(item.bvid)?.let {
                                                onVideoClick(it.first, it.second)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpPreviewVideoCard(
    item: UpPreviewVideoItem,
    colors: UpPreviewSheetSurfaceColors,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.coverPlaceholderColor)
        ) {
            AsyncImage(
                model = FormatUtils.fixImageUrl(item.coverUrl),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // 左下播放量 / 右下时长
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatUpPreviewCount(item.playCount),
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1,
                )
                if (item.durationText.isNotBlank()) {
                    Text(
                        text = item.durationText,
                        color = Color.White,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.title,
            color = colors.titleColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 17.sp,
        )
        if (item.createdAtSeconds > 0L) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatUpPreviewRelativeDate(item.createdAtSeconds),
                color = colors.supportingColor,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
    }
}

private fun RelatedVideo.toUpPreviewVideoItem(): UpPreviewVideoItem? {
    if (bvid.isBlank()) return null
    return UpPreviewVideoItem(
        bvid = bvid,
        title = title,
        coverUrl = pic,
        playCount = stat.view,
        durationText = FormatUtils.formatDuration(duration),
        createdAtSeconds = 0L,
    )
}

private fun SpaceVideoItem.toUpPreviewVideoItem(): UpPreviewVideoItem {
    return UpPreviewVideoItem(
        bvid = bvid,
        title = title,
        coverUrl = pic,
        playCount = play,
        durationText = length,
        createdAtSeconds = created,
    )
}

private fun formatUpPreviewRelativeDate(createdAtSeconds: Long): String {
    if (createdAtSeconds <= 0L) return ""
    val nowSec = System.currentTimeMillis() / 1000L
    val delta = (nowSec - createdAtSeconds).coerceAtLeast(0L)
    val days = TimeUnit.SECONDS.toDays(delta)
    return when {
        days <= 0L -> "今天"
        days == 1L -> "昨天"
        days < 30L -> "${days}天前"
        days < 365L -> "${days / 30}个月前"
        else -> "${days / 365}年前"
    }
}
