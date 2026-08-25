// 聊天详情页面
package com.android.purebilibili.feature.message
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.AppTopBar
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppCard
import com.android.purebilibili.core.ui.components.AppCardDefaults
import com.android.purebilibili.core.ui.components.AppCardShape
import com.android.purebilibili.core.ui.components.AppCardVariant
import com.android.purebilibili.core.ui.components.AppDropdownMenu
import com.android.purebilibili.core.ui.components.AppDropdownMenuItem
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppOutlinedTextField
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppSnackbar
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.EmoteInfo
import com.android.purebilibili.data.model.response.PrivateMessageItem
import com.android.purebilibili.data.repository.MessageSessionControlInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    talkerId: Long,
    sessionType: Int,
    userName: String,
    onBack: () -> Unit,
    onNavigateToVideo: (String) -> Unit,
    onOpenBilibiliLink: (String) -> Unit = {},
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(talkerId, sessionType))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var pendingWithdrawMessage by remember { mutableStateOf<PrivateMessageItem?>(null) }
    var showSessionMenu by remember { mutableStateOf(false) }
    var showInterceptConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendImageMessage(context, uri)
        }
    }
    
    // 滚动到底部
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    AppScaffold(
        topBar = {
            AppTopBar(
                title = userName,
                navigationIcon = {
                    AppIconButton(onClick = onBack) {
                        AppIcon(rememberAppBackIcon(), contentDescription = "返回")
                    }
                },
                actions = {
                    Box {
                        AppIconButton(onClick = { showSessionMenu = true }) {
                            AppIcon(Icons.Default.MoreVert, contentDescription = "会话设置")
                        }
                        ChatSessionControlMenu(
                            expanded = showSessionMenu,
                            sessionType = sessionType,
                            controlInfo = uiState.sessionControlInfo,
                            isUpdating = uiState.isSessionControlUpdating,
                            onDismiss = { showSessionMenu = false },
                            onToggleDnd = {
                                showSessionMenu = false
                                viewModel.toggleDnd()
                            },
                            onTogglePush = {
                                showSessionMenu = false
                                viewModel.togglePushMuted()
                            },
                            onToggleIntercept = {
                                showSessionMenu = false
                                if (uiState.sessionControlInfo.isIntercept == true) {
                                    viewModel.toggleIntercept()
                                } else {
                                    showInterceptConfirm = true
                                }
                            },
                            onRefresh = {
                                showSessionMenu = false
                                viewModel.loadSessionControlInfo()
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                onPickImage = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                isSending = uiState.isSending,
                isUploadingImage = uiState.isUploadingImage
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    com.android.purebilibili.core.ui.CutePersonLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppText(uiState.error ?: "加载失败")
                        Spacer(modifier = Modifier.height(8.dp))
                        AppButton(onClick = { viewModel.loadMessages() }) {
                            AppText("重试")
                        }
                    }
                }
                uiState.messages.isEmpty() -> {
                    AppText(
                        text = "暂无消息",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 加载更多按钮
                        if (uiState.hasMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isLoadingMore) {
                                        com.android.purebilibili.core.ui.CutePersonLoadingIndicator(
                                            size = 24.dp
                                        )
                                    } else {
                                        AppTextButton(onClick = { viewModel.loadMoreMessages() }) {
                                            AppText("加载更多")
                                        }
                                    }
                                }
                            }
                        }
                        
                        items(
                            items = uiState.messages,
                            key = { it.msg_key }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                isOwnMessage = message.sender_uid == viewModel.currentUserMid,
                                emoteInfos = uiState.emoteInfos,
                                videoPreviews = uiState.videoPreviews,
                                canWithdraw = message.sender_uid == viewModel.currentUserMid && message.msg_status != 1,
                                onLongPress = {
                                    pendingWithdrawMessage = message
                                },
                                onVideoClick = { bvid ->
                                    onNavigateToVideo(bvid)
                                },
                                onLinkClick = { link ->
                                    onOpenBilibiliLink(link)
                                }
                            )
                        }
                    }
                }
            }
            
            // 发送错误提示
            uiState.sendError?.let { error ->
                AppSnackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        AppTextButton(onClick = { viewModel.clearSendError() }) {
                            AppText("知道了")
                        }
                    }
                ) {
                    AppText(error)
                }
            }
        }
    }

    pendingWithdrawMessage?.let { targetMessage ->
        AppAlertDialog(
            onDismissRequest = {
                if (uiState.withdrawingMessageKey == null) {
                    pendingWithdrawMessage = null
                }
            },
            title = { AppText("撤回消息") },
            text = { AppText("要撤回这条消息吗？") },
            confirmButton = {
                AppTextButton(
                    enabled = uiState.withdrawingMessageKey == null,
                    onClick = {
                        viewModel.withdrawMessage(targetMessage)
                    }
                ) {
                    AppText(if (uiState.withdrawingMessageKey == targetMessage.msg_key) "撤回中..." else "确认")
                }
            },
            dismissButton = {
                AppTextButton(
                    enabled = uiState.withdrawingMessageKey == null,
                    onClick = {
                        pendingWithdrawMessage = null
                    }
                ) {
                    AppText("取消")
                }
            }
        )
    }

    LaunchedEffect(uiState.withdrawingMessageKey) {
        if (uiState.withdrawingMessageKey == null) {
            pendingWithdrawMessage = null
        }
    }

    if (showInterceptConfirm) {
        AppAlertDialog(
            onDismissRequest = { showInterceptConfirm = false },
            title = { AppText("移入拦截") },
            text = { AppText("后续这类会话会进入拦截分类，仍可在拦截列表中查看和恢复。") },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        viewModel.toggleIntercept()
                        showInterceptConfirm = false
                    }
                ) {
                    AppText("移入")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showInterceptConfirm = false }) {
                    AppText("取消")
                }
            }
        )
    }
}

@Composable
private fun ChatSessionControlMenu(
    expanded: Boolean,
    sessionType: Int,
    controlInfo: MessageSessionControlInfo,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onToggleDnd: () -> Unit,
    onTogglePush: () -> Unit,
    onToggleIntercept: () -> Unit,
    onRefresh: () -> Unit
) {
    AppDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        AppDropdownMenuItem(
            text = {
                AppText(if (controlInfo.isDnd == true) "关闭免打扰" else "开启免打扰")
            },
            enabled = !isUpdating,
            onClick = onToggleDnd
        )

        if (sessionType == 1 && controlInfo.showPushSetting) {
            AppDropdownMenuItem(
                text = {
                    AppText(if (controlInfo.pushMuted == true) "接收推送" else "关闭推送")
                },
                enabled = !isUpdating,
                onClick = onTogglePush
            )
        }

        if (sessionType == 1) {
            AppDropdownMenuItem(
                text = {
                    AppText(if (controlInfo.isIntercept == true) "移出拦截" else "移入拦截")
                },
                enabled = !isUpdating,
                onClick = onToggleIntercept
            )
        }

        if (controlInfo.isLimit == true || controlInfo.reportLimit == true) {
            AppDropdownMenuItem(
                text = {
                    val text = when {
                        controlInfo.isLimit == true && controlInfo.reportLimit == true -> "会话受限，举报也受限"
                        controlInfo.isLimit == true -> "会话受限"
                        else -> "举报受限"
                    }
                    AppText(text)
                },
                enabled = false,
                onClick = {}
            )
        }

        AppDropdownMenuItem(
            text = { AppText("刷新状态") },
            enabled = !isUpdating,
            onClick = onRefresh
        )
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onPickImage: () -> Unit,
    isSending: Boolean,
    isUploadingImage: Boolean
) {
    val showSendAction = text.isNotBlank()
    val isBusy = isSending || isUploadingImage

    AppSurface(
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppOutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { AppText("输入消息...") },
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                shape = AppShapes.container(ContainerLevel.Floating)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            AppIconButton(
                onClick = {
                    if (showSendAction) {
                        onSend()
                    } else {
                        onPickImage()
                    }
                },
                enabled = !isBusy
            ) {
                if (isBusy) {
                    com.android.purebilibili.core.ui.CutePersonLoadingIndicator(
                        size = 24.dp,
                        strokeWidth = 2.dp
                    )
                } else {
                    AppIcon(
                        imageVector = if (showSendAction) Icons.AutoMirrored.Filled.Send else Icons.Filled.AddCircle,
                        contentDescription = if (showSendAction) "发送" else "图片",
                        tint = if (showSendAction) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: PrivateMessageItem,
    isOwnMessage: Boolean,
    emoteInfos: List<EmoteInfo> = emptyList(),
    videoPreviews: Map<String, VideoPreviewInfo> = emptyMap(),
    canWithdraw: Boolean = false,
    onLongPress: (() -> Unit)? = null,
    onVideoClick: ((String) -> Unit)? = null,
    onLinkClick: ((String) -> Unit)? = null
) {
    val bubbleColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    // BV号正则匹配
    val bvPattern = remember { Regex("BV[a-zA-Z0-9]{10}") }
    
    // 从消息内容中提取BV号
    val detectedBvids = remember(message.content) {
        if (message.msg_type == 1) {
            val content = parseTextContent(message.content)
            bvPattern.findAll(content).map { it.value }.toList()
        } else {
            emptyList()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        // 消息气泡
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .then(
                    if (canWithdraw && onLongPress != null) {
                        Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = onLongPress
                        )
                    } else {
                        Modifier
                    }
                )
                .clip(AppShapes.messageBubble(isOutgoing = isOwnMessage))
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when {
                message.msg_status == 1 -> {
                    // 已撤回消息
                    AppText(
                        text = "[消息已撤回]",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
                message.msg_type == 1 -> {
                    // 文字消息 - 支持表情渲染
                    val content = parseTextContent(message.content)
                    EmoteText(
                        text = content,
                        emoteInfos = emoteInfos,
                        color = textColor,
                        fontSize = 15.sp,
                        onLinkClick = onLinkClick
                    )
                }
                message.msg_type == 2 -> {
                    // 图片消息
                    val imageUrl = parseImageUrl(message.content)
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "图片",
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .heightIn(max = 300.dp)
                                .clip(AppShapes.container(ContainerLevel.Chip)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AppText(
                            text = "[图片]",
                            color = textColor,
                            fontSize = 15.sp
                        )
                    }
                }
                message.msg_type == 6 -> {
                    // 表情消息 (大表情)
                    val emoteUrl = parseEmoteUrl(message.content)
                    if (emoteUrl.isNotEmpty()) {
                        AsyncImage(
                            model = emoteUrl,
                            contentDescription = "表情",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(AppShapes.container(ContainerLevel.Chip)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AppText(
                            text = "[表情]",
                            color = textColor,
                            fontSize = 15.sp
                        )
                    }
                }
                message.msg_type == 10 -> {
                    // 通知消息
                    AppText(
                        text = parseNotificationContent(message.content),
                        color = textColor,
                        fontSize = 14.sp
                    )
                }
                message.msg_type == 11 -> {
                    MessagePreviewParser.parseMessageCard(message.content, message.msg_type)?.let { card ->
                        MessageCardPreviewCard(
                            preview = card,
                            onClick = {
                                when {
                                    card.bvid.isNotBlank() -> onVideoClick?.invoke(card.bvid)
                                    card.targetUrl.isNotBlank() -> onLinkClick?.invoke(card.targetUrl)
                                }
                            }
                        )
                    } ?: AppText(
                        text = "[视频]",
                        color = textColor,
                        fontSize = 15.sp
                    )
                }
                message.msg_type in setOf(7, 12, 13, 14) -> {
                    MessagePreviewParser.parseMessageCard(message.content, message.msg_type)?.let { card ->
                        MessageCardPreviewCard(
                            preview = card,
                            onClick = {
                                when {
                                    card.bvid.isNotBlank() -> onVideoClick?.invoke(card.bvid)
                                    card.targetUrl.isNotBlank() -> onLinkClick?.invoke(card.targetUrl)
                                }
                            }
                        )
                    } ?: AppText(
                        text = "[${getMessageTypeName(message.msg_type)}]",
                        color = textColor,
                        fontSize = 15.sp
                    )
                }
                else -> {
                    AppText(
                        text = "[${getMessageTypeName(message.msg_type)}]",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // 视频链接预览卡片
        detectedBvids.forEach { bvid ->
            videoPreviews[bvid]?.let { preview ->
                Spacer(modifier = Modifier.height(4.dp))
                VideoLinkPreviewCard(
                    preview = preview,
                    onClick = { onVideoClick?.invoke(bvid) }
                )
            }
        }
        
        // 时间
        Spacer(modifier = Modifier.height(2.dp))
        AppText(
            text = formatMessageTime(message.timestamp),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * 视频链接预览卡片
 */
@Composable
fun VideoLinkPreviewCard(
    preview: VideoPreviewInfo,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .clickable { onClick() },
        shape = AppCardShape.Semantic(ContainerLevel.Card),
        colors = AppCardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        variant = AppCardVariant.Elevated,
    ) {
        Column {
            // 封面图
            Box {
                AsyncImage(
                    model = preview.cover,
                    contentDescription = preview.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.Crop
                )
                
                // 播放图标
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = "▶",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                
                // 时长
                if (preview.duration > 0) {
                    AppText(
                        text = formatDuration(preview.duration),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                AppShapes.container(ContainerLevel.Tag)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
            
            // 信息
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // 标题
                AppText(
                    text = preview.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (preview.ownerName.isNotBlank() || preview.viewCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (preview.ownerName.isNotBlank()) {
                            AppText(
                                text = preview.ownerName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (preview.viewCount > 0) {
                            AppText(
                                text = if (preview.ownerName.isNotBlank()) {
                                    " · ${formatViewCount(preview.viewCount)}播放"
                                } else {
                                    "${formatViewCount(preview.viewCount)}播放"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageCardPreviewCard(
    preview: MessageCardPreview,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .clickable { onClick() },
        shape = AppCardShape.Semantic(ContainerLevel.Card),
        colors = AppCardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        variant = AppCardVariant.Elevated,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (preview.cover.isNotBlank()) {
                AsyncImage(
                    model = preview.cover,
                    contentDescription = preview.title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(AppShapes.container(ContainerLevel.Chip))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = preview.kind.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(3.dp))
                AppText(
                    text = preview.title.ifBlank { preview.kind.label },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (preview.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AppText(
                        text = preview.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 格式化时长
 */
private fun formatDuration(seconds: Long): String {
    return FormatUtils.formatDuration(seconds.coerceAtLeast(0L).toInt())
}

/**
 * 格式化播放量
 */
private fun formatViewCount(count: Long): String {
    return when {
        count >= 100_000_000 -> String.format("%.1f亿", count / 100_000_000.0)
        count >= 10_000 -> String.format("%.1f万", count / 10_000.0)
        else -> count.toString()
    }
}

/**
 * 支持表情和链接渲染的富文本组件
 */
@Composable
fun RichMessageText(
    text: String,
    emoteInfos: List<EmoteInfo>,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    linkColor: Color = MaterialTheme.colorScheme.primary,  // 使用主题色
    onLinkClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // 构建表情映射 (text -> EmoteInfo)
    val emoteMap = remember(emoteInfos) {
        emoteInfos.associateBy { it.text }
    }
    
    // 匹配模式
    val emotePattern = remember { "\\[([^\\[\\]]+)\\]".toRegex() }
    val urlPattern = remember { 
        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)".toRegex() 
    }
    
    // 扫描所有特殊内容 (表情和URL)
    data class ContentMatch(val range: IntRange, val type: String, val value: String)
    
    val allMatches = remember(text) {
        val matches = mutableListOf<ContentMatch>()
        
        // 收集表情
        emotePattern.findAll(text).forEach { match ->
            matches.add(ContentMatch(match.range, "emote", match.value))
        }
        
        // 收集 URL
        urlPattern.findAll(text).forEach { match ->
            matches.add(ContentMatch(match.range, "url", match.value))
        }
        
        // 按位置排序
        matches.sortedBy { it.range.first }
    }
    
    // 如果没有特殊内容，直接显示文本
    if (allMatches.isEmpty()) {
        AppText(text = text, color = color, fontSize = fontSize)
        return
    }
    
    // 构建 AnnotatedString
    val annotatedString = buildAnnotatedString {
        var lastEnd = 0
        
        allMatches.forEach { match ->
            // 添加前面的普通文本
            if (match.range.first > lastEnd) {
                append(text.substring(lastEnd, match.range.first))
            }
            
            when (match.type) {
                "emote" -> {
                    val emote = emoteMap[match.value]
                    if (emote != null && emote.url.isNotEmpty()) {
                        appendInlineContent(match.value, match.value)
                    } else {
                        append(match.value)
                    }
                }
                "url" -> {
                    // 添加链接样式和注解
                    pushStringAnnotation(tag = "URL", annotation = match.value)
                    withStyle(SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )) {
                        append(match.value)
                    }
                    pop()
                }
            }
            
            lastEnd = match.range.last + 1
        }
        
        // 添加剩余文本
        if (lastEnd < text.length) {
            append(text.substring(lastEnd))
        }
    }
    
    // 构建 InlineContent 映射 (表情图片)
    val inlineContentMap = remember(emoteInfos) {
        emoteInfos.filter { it.url.isNotEmpty() }.associate { emote ->
            emote.text to InlineTextContent(
                placeholder = Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AsyncImage(
                    model = emote.url,
                    contentDescription = emote.text,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
    
    // 用于检测点击位置
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    
    AppText(
        text = annotatedString,
        color = color,
        fontSize = fontSize,
        inlineContent = inlineContentMap,
        onTextLayout = { layoutResult = it },
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                layoutResult?.let { layout ->
                    val position = layout.getOffsetForPosition(offset)
                    annotatedString.getStringAnnotations(
                        tag = "URL",
                        start = position,
                        end = position
                    ).firstOrNull()?.let { annotation ->
                        val url = annotation.item
                        if (onLinkClick != null) {
                            onLinkClick(url)
                        } else {
                            // 默认: 用浏览器打开
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("RichMessageText", "Failed to open URL: $url", e)
                            }
                        }
                    }
                }
            }
        }
    )
}

// 保留旧函数名的兼容性别名
@Composable
fun EmoteText(
    text: String,
    emoteInfos: List<EmoteInfo>,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onLinkClick: ((String) -> Unit)? = null
) {
    RichMessageText(
        text = text,
        emoteInfos = emoteInfos,
        color = color,
        fontSize = fontSize,
        onLinkClick = onLinkClick
    )
}


/**
 * 解析文字消息内容
 */
private fun parseTextContent(content: String): String {
    if (content.isBlank()) return ""
    if (!content.trim().startsWith("{")) return content

    return try {
        val json = Json.parseToJsonElement(content)
        json.jsonObject["content"]?.jsonPrimitive?.content ?: content
    } catch (e: Exception) {
        content
    }
}

/**
 * 解析图片URL
 */
private fun parseImageUrl(content: String): String {
    return try {
        val json = Json.parseToJsonElement(content)
        json.jsonObject["url"]?.jsonPrimitive?.content ?: ""
    } catch (e: Exception) {
        ""
    }
}

/**
 * 解析表情URL
 */
private fun parseEmoteUrl(content: String): String {
    return try {
        val json = Json.parseToJsonElement(content)
        json.jsonObject["url"]?.jsonPrimitive?.content ?: ""
    } catch (e: Exception) {
        ""
    }
}

/**
 * 解析通知消息
 */
private fun parseNotificationContent(content: String): String {
    return try {
        val json = Json.parseToJsonElement(content)
        val title = json.jsonObject["title"]?.jsonPrimitive?.content ?: ""
        val text = json.jsonObject["text"]?.jsonPrimitive?.content ?: ""
        if (title.isNotEmpty()) "$title\n$text" else text
    } catch (e: Exception) {
        "[通知]"
    }
}

/**
 * 获取消息类型名称
 */
private fun getMessageTypeName(msgType: Int): String {
    return when (msgType) {
        1 -> "文字"
        2 -> "图片"
        5 -> "撤回"
        6 -> "表情"
        7 -> "分享"
        10 -> "通知"
        11 -> "视频"
        12 -> "专栏"
        13 -> "图片卡片"
        14 -> "分享"
        else -> "消息"
    }
}

/**
 * 格式化消息时间
 */
private fun formatMessageTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp * 1000 }
    
    val sameDay = now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
    
    val pattern = if (sameDay) "HH:mm" else "MM-dd HH:mm"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp * 1000))
}
