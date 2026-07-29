// 文件路径: feature/dynamic/components/DynamicCommentSheet.kt
package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.rememberAppSegmentedControlPolicy

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.purebilibili.data.model.response.DynamicItem
import com.android.purebilibili.data.model.response.ReplyItem
import com.android.purebilibili.feature.dynamic.DynamicViewModel
import com.android.purebilibili.feature.dynamic.resolveDynamicCommentSheetTotalCount
import com.android.purebilibili.feature.video.ui.components.CommentPictures
import com.android.purebilibili.feature.video.ui.components.RichCommentText
import com.android.purebilibili.feature.video.ui.components.FanGroupDecorationBadge
import com.android.purebilibili.feature.video.ui.components.resolveFanGroupDecorationCardBgs
import com.android.purebilibili.feature.video.ui.components.resolveFanGroupVisualFromMemberAndSailing
import com.android.purebilibili.feature.video.ui.components.resolveInlineSubReplyToggleLabel
import com.android.purebilibili.feature.video.ui.components.resolveReplyPreviewTextContent
import com.android.purebilibili.feature.video.ui.components.resolveVisibleSubReplies
import com.android.purebilibili.feature.video.ui.components.shouldShowInlineSubReplyToggle
import com.android.purebilibili.feature.video.viewmodel.CommentSortMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import com.android.purebilibili.core.ui.rememberAppClearIcon
import com.android.purebilibili.core.ui.rememberAppCommentIcon
import com.android.purebilibili.core.ui.rememberAppLikeIcon
import com.android.purebilibili.core.ui.AppModalBottomSheet
import com.android.purebilibili.core.ui.components.AppTextField

@Composable
fun DynamicCommentOverlayHost(
    viewModel: DynamicViewModel,
    primaryItems: List<DynamicItem>,
    secondaryItems: List<DynamicItem> = emptyList(),
    toastContext: Context
) {
    val selectedDynamicId by viewModel.selectedDynamicId.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val commentsLoading by viewModel.commentsLoading.collectAsStateWithLifecycle()
    val commentsLoadingMore by viewModel.commentsLoadingMore.collectAsStateWithLifecycle()
    val subReplyState by viewModel.subReplyState.collectAsStateWithLifecycle()
    val liveCommentCount by viewModel.commentTotalCount.collectAsStateWithLifecycle()
    val sortMode by viewModel.dynamicCommentSortMode.collectAsStateWithLifecycle()
    val inspectionMode = LocalInspectionMode.current

    if (!selectedDynamicId.isNullOrBlank()) {
        val dynamicId = requireNotNull(selectedDynamicId)
        val dynamicItem = remember(dynamicId, primaryItems, secondaryItems) {
            primaryItems.find { it.id_str == dynamicId }
                ?: secondaryItems.find { it.id_str == dynamicId }
        }
        val fallbackCount = dynamicItem?.modules?.module_stat?.comment?.count ?: 0
        val totalCount = remember(liveCommentCount, fallbackCount) {
            resolveDynamicCommentSheetTotalCount(
                liveCount = liveCommentCount,
                fallbackCount = fallbackCount
            )
        }

        DynamicCommentSheet(
            comments = comments,
            totalCount = totalCount,
            sortMode = sortMode,
            isLoading = commentsLoading,
            isLoadingMore = commentsLoadingMore,
            onDismiss = { viewModel.closeCommentSheet() },
            onSortModeChange = { viewModel.setDynamicCommentSortMode(it) },
            onPostComment = { message ->
                viewModel.postComment(dynamicId, message) { _, msg ->
                    if (!inspectionMode) {
                        android.widget.Toast.makeText(toastContext, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onViewReplies = { reply -> viewModel.openSubReply(reply) },
            onLoadMore = { viewModel.loadMoreComments() }
        )
    }

    DynamicSubReplyPreviewHost(
        state = subReplyState,
        onDismiss = { viewModel.closeSubReply() },
        onLoadMore = { viewModel.loadMoreSubReplies() }
    )
}

/**
 *  动态评论底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicCommentSheet(
    comments: List<ReplyItem>,
    totalCount: Int,  //  [新增] 总评论数
    sortMode: CommentSortMode = CommentSortMode.HOT,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    onDismiss: () -> Unit,
    onSortModeChange: (CommentSortMode) -> Unit = {},
    onPostComment: (String) -> Unit,
    onViewReplies: (ReplyItem) -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val canLoadMore = comments.size < totalCount && !isLoading && !isLoadingMore
    var showImagePreview by remember { mutableStateOf(false) }
    var previewImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var previewInitialIndex by remember { mutableIntStateOf(0) }
    var previewSourceRect by remember { mutableStateOf<Rect?>(null) }
    var previewTextContent by remember { mutableStateOf<ImagePreviewTextContent?>(null) }
    val sortModes = remember { listOf(CommentSortMode.HOT, CommentSortMode.NEWEST) }

    if (showImagePreview && previewImages.isNotEmpty()) {
        ImagePreviewDialog(
            images = previewImages,
            initialIndex = previewInitialIndex,
            sourceRect = previewSourceRect,
            textContent = previewTextContent,
            onDismiss = {
                showImagePreview = false
                previewTextContent = null
            }
        )
    }

    LaunchedEffect(listState, comments.size, totalCount, isLoading, isLoadingMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .map { lastVisibleIndex ->
                val itemCount = listState.layoutInfo.totalItemsCount
                itemCount > 0 && lastVisibleIndex >= itemCount - 4
            }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                if (canLoadMore) onLoadMore()
            }
    }
    
    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacingTokens.Large, vertical = AppSpacingTokens.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "评论 ${if (totalCount > 0) "($totalCount)" else ""}",  //  [修改] 使用 totalCount
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                DynamicCommentSortControl(
                    items = sortModes.map { it.label },
                    selectedIndex = sortModes.indexOf(sortMode).coerceAtLeast(0),
                    onSelected = { index ->
                        sortModes.getOrNull(index)?.let(onSortModeChange)
                    }
                )
                Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                IconButton(onClick = onDismiss) {
                    Icon(
                        rememberAppClearIcon(),
                        contentDescription = "关闭",
                        modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall)
                    )
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            // 评论列表
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AdaptiveLoadingIndicator(size = AppSpacingTokens.DoubleExtraLarge)
                }
            } else if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            rememberAppCommentIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(AppSpacingTokens.TripleExtraLarge),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
                        )
                        Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
                        Text(
                            "暂无评论",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(AppSpacingTokens.Large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large)
                ) {
                    items(comments, key = { it.rpid }) { reply ->
                        CommentItem(
                            reply = reply,
                            onViewReplies = onViewReplies,
                            onImagePreview = { images, index, rect, textContent ->
                                previewImages = images
                                previewInitialIndex = index
                                previewSourceRect = rect
                                previewTextContent = textContent
                                showImagePreview = true
                            }
                        )
                    }
                    if (isLoadingMore) {
                        item(key = "dynamic_comment_loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppSpacingTokens.Small),
                                contentAlignment = Alignment.Center
                            ) {
                                AdaptiveLoadingIndicator(size = AppSpacingTokens.ExtraLarge)
                            }
                        }
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            // 评论输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacingTokens.Large),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = "发一条友善的评论",
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
                
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onPostComment(commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    shape = AppShapes.container(ContainerLevel.Sheet)
                ) {
                    Text("发送")
                }
            }
            
            // 底部安全区
            Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
        }
    }
}

@Composable
private fun DynamicCommentSortControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    if (items.isEmpty()) return
    val policy = rememberAppSegmentedControlPolicy()
    val safeSelectedIndex = selectedIndex.coerceIn(items.indices)
    Row(
        modifier = Modifier
            .width(66.dp * items.size)
            .height(40.dp)
            .clip(RoundedCornerShape(policy.pillCornerRadius))
            .background(AppSurfaceTokens.surfaceContainer())
            .padding(AppSpacingTokens.Micro),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, label ->
            val selected = index == safeSelectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(policy.pillCornerRadius))
                    .background(
                        if (selected) AppSurfaceTokens.secondaryContainer() else Color.Transparent
                    )
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (selected) {
                        AppSurfaceTokens.onSecondaryContainer()
                    } else {
                        AppSurfaceTokens.onSurfaceVariantActions()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Inline comments for dynamic detail, rendered by the detail screen's LazyColumn. */
@Composable
fun DynamicInlineCommentHeader(
    totalCount: Int,
    sortMode: CommentSortMode,
    onSortModeChange: (CommentSortMode) -> Unit,
) {
    val sortModes = remember { listOf(CommentSortMode.HOT, CommentSortMode.NEWEST) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacingTokens.Large, vertical = AppSpacingTokens.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "评论 ${if (totalCount > 0) "($totalCount)" else ""}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.weight(1f))
        DynamicCommentSortControl(
            items = sortModes.map { it.label },
            selectedIndex = sortModes.indexOf(sortMode).coerceAtLeast(0),
            onSelected = { index ->
                sortModes.getOrNull(index)?.let(onSortModeChange)
            },
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

fun LazyListScope.dynamicInlineCommentItems(
    comments: List<ReplyItem>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    onViewReplies: (ReplyItem) -> Unit,
    onImagePreview: (List<String>, Int, Rect?, ImagePreviewTextContent?) -> Unit,
) {
    when {
        isLoading -> item(key = "dynamic_inline_comment_loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacingTokens.DoubleExtraLarge),
                contentAlignment = Alignment.Center,
            ) {
                AdaptiveLoadingIndicator(size = AppSpacingTokens.DoubleExtraLarge)
            }
        }

        comments.isEmpty() -> item(key = "dynamic_inline_comment_empty") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacingTokens.DoubleExtraLarge),
                contentAlignment = Alignment.Center,
            ) {
                Text("暂无评论", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }

        else -> items(comments, key = { it.rpid }) { reply ->
            CommentItem(
                reply = reply,
                onViewReplies = onViewReplies,
                onImagePreview = onImagePreview,
                modifier = Modifier.padding(horizontal = AppSpacingTokens.Large, vertical = AppSpacingTokens.Small),
            )
        }
    }
    if (isLoadingMore) {
        item(key = "dynamic_inline_comment_loading_more") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacingTokens.Medium),
                contentAlignment = Alignment.Center,
            ) {
                AdaptiveLoadingIndicator(size = AppSpacingTokens.ExtraLarge)
            }
        }
    }
}

@Composable
fun DynamicInlineCommentComposer(
    onPostComment: (String) -> Unit,
) {
    var commentText by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacingTokens.Large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier.weight(1f),
            placeholder = "发一条友善的评论",
            singleLine = true,
        )
        Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
        Button(
            onClick = {
                onPostComment(commentText)
                commentText = ""
            },
            enabled = commentText.isNotBlank(),
            shape = AppShapes.container(ContainerLevel.Sheet),
        ) {
            Text("发送")
        }
    }
}

/**
 *  单条评论项
 */
@Composable
private fun CommentItem(
    reply: ReplyItem,
    onViewReplies: (ReplyItem) -> Unit,
    onImagePreview: (List<String>, Int, Rect?, ImagePreviewTextContent?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val member = reply.member
    var isSubPreviewExpanded by remember(reply.rpid) { mutableStateOf(false) }
    val visibleSubReplies = remember(reply.replies, isSubPreviewExpanded) {
        resolveVisibleSubReplies(
            replies = reply.replies,
            expanded = isSubPreviewExpanded
        )
    }
    val showInlineToggle = remember(reply.replies) {
        shouldShowInlineSubReplyToggle(reply.replies.orEmpty().size)
    }
    val fanGroupVisual = remember(member) {
        resolveFanGroupVisualFromMemberAndSailing(
            member = member,
            cardBgs = resolveFanGroupDecorationCardBgs(member)
        )
    }
    
    Row(modifier = modifier.fillMaxWidth()) {
        // 头像
        AsyncImage(
            model = member.avatar.let { 
                if (it.startsWith("http://")) it.replace("http://", "https://") else it 
            },
            contentDescription = null,
            modifier = Modifier
                .size(AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
        
        Column(modifier = Modifier.weight(1f)) {
            // 用户名 + 时间
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.uname,
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (fanGroupVisual != null) Modifier.weight(1f, fill = false) else Modifier
                )
                Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                Text(
                    text = formatTime(reply.ctime),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                )
                if (fanGroupVisual != null) {
                    Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                    FanGroupDecorationBadge(visual = fanGroupVisual)
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall))
            
            // 评论内容 - 使用 RichCommentText 渲染表情
            val emoteMap = remember(reply.content.emote) {
                reply.content.emote?.mapValues { it.value.url } ?: emptyMap()
            }
            RichCommentText(
                text = reply.content.message,
                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                color = MaterialTheme.colorScheme.onSurface,
                emoteMap = emoteMap
            )

            // 评论图片
            if (!reply.content.pictures.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                CommentPictures(
                    pictures = reply.content.pictures,
                    onImageClick = { images, index, rect ->
                        onImagePreview(
                            images,
                            index,
                            rect,
                            resolveReplyPreviewTextContent(reply)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
            
            // 点赞数
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    rememberAppLikeIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(AppSpacingTokens.Medium + AppSpacingTokens.Micro),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                )
                Spacer(modifier = Modifier.width(AppSpacingTokens.ExtraSmall))
                Text(
                    text = "${reply.like}",
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                )
            }

            if (com.android.purebilibili.feature.dynamic.canOpenDynamicSubReplies(reply)) {
                if (visibleSubReplies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacingTokens.Small + AppSpacingTokens.Micro))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.container(ContainerLevel.Dialog))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                            .padding(horizontal = AppSpacingTokens.Small + AppSpacingTokens.Micro, vertical = AppSpacingTokens.Small),
                        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)
                    ) {
                        visibleSubReplies.forEach { subReply ->
                            val subEmoteMap = remember(subReply.content.emote) {
                                subReply.content.emote?.mapValues { it.value.url } ?: emptyMap()
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)
                                ) {
                                    Text(
                                        text = "${subReply.member.uname}:",
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        RichCommentText(
                                            text = subReply.content.message,
                                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            emoteMap = subEmoteMap,
                                            maxLines = 2
                                        )
                                    }
                                }
                                if (!subReply.content.pictures.isNullOrEmpty()) {
                                    CommentPictures(
                                        pictures = subReply.content.pictures,
                                        onImageClick = { images, index, rect ->
                                            onImagePreview(
                                                images,
                                                index,
                                                rect,
                                                resolveReplyPreviewTextContent(subReply)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        if (showInlineToggle) {
                            Text(
                                text = resolveInlineSubReplyToggleLabel(expanded = isSubPreviewExpanded),
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { isSubPreviewExpanded = !isSubPreviewExpanded }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                TextButton(
                    onClick = { onViewReplies(reply) },
                    contentPadding = PaddingValues(horizontal = AppSpacingTokens.None, vertical = AppSpacingTokens.None)
                ) {
                    Text(
                        text = "查看回复(${com.android.purebilibili.feature.dynamic.resolveDynamicSubReplyCount(reply)})",
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 格式化时间戳
 */
private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis() / 1000
    val diff = now - timestamp
    return when {
        diff < 60 -> "刚刚"
        diff < 3600 -> "${diff / 60}分钟前"
        diff < 86400 -> "${diff / 3600}小时前"
        diff < 604800 -> "${diff / 86400}天前"
        else -> {
            val date = java.text.SimpleDateFormat("MM-dd", java.util.Locale.CHINA)
                .format(java.util.Date(timestamp * 1000))
            date
        }
    }
}
