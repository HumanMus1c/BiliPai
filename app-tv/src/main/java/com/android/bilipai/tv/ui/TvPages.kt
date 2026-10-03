@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.android.bilipai.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import android.view.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.android.bilipai.tv.ui.components.TvAppButton
import com.android.bilipai.tv.ui.components.TvNavigationItem
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.util.FormatUtils
import com.android.bilipai.tv.QrPhase
import com.android.bilipai.tv.TvUiState
import com.android.purebilibili.data.model.VideoQuality
import kotlinx.coroutines.launch

@Composable
internal fun TvSearchInput(state: TvUiState, requester: FocusRequester, onSearch: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf(state.query) }
    val submitFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    fun submit() { if (draft.isNotBlank()) { keyboard?.hide(); onSearch(draft) } }
    LaunchedEffect(requester) { requester.requestFocus() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BasicTextField(value = draft, onValueChange = { draft = it }, singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit() }),
                modifier = Modifier.weight(1f).focusRequester(requester).testTag("tv-search-input")
                    .background(Color(0xFF242C3C), RoundedCornerShape(12.dp)).padding(18.dp)
                    .onPreviewKeyEvent { event ->
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER -> {
                                if (event.nativeKeyEvent.action == KeyEvent.ACTION_UP) keyboard?.show()
                                true
                            }
                            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                if (event.nativeKeyEvent.action == KeyEvent.ACTION_UP) submit()
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (draft.isBlank()) {
                                    keyboard?.hide()
                                    return@onPreviewKeyEvent false
                                }
                                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                    keyboard?.hide()
                                    submitFocus.requestFocus()
                                }
                                true
                            }
                            else -> false
                        }
                    },
                decorationBox = { field ->
                    if (draft.isEmpty()) Text("输入视频关键词", color = Color(0xFFABB3C5))
                    field()
                })
            TvAppButton(onClick = { submit() }, enabled = draft.isNotBlank(), modifier = Modifier.focusRequester(submitFocus)) { Text("搜索") }
        }
        val suggestions = (state.searchHistory.take(3) + state.trending.take(3)).distinct().take(5)
        if (suggestions.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                suggestions.forEach { word ->
                    TvAppButton(onClick = { draft = word; submit() }, modifier = Modifier.weight(1f)) {
                        Text(word, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
internal fun TvDetailContent(
    state: TvUiState,
    requester: FocusRequester,
    onPlay: (Long) -> Unit,
    onWatchLater: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.detailLoading) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraLarge)) {
            Text("正在加载视频详情…")
            FocusButton("返回列表", onBack, requester)
        }
        return
    }
    val info = state.detail
    if (info == null) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraLarge)) {
            Text(state.detailError ?: "未找到视频")
            FocusButton("重试", onRetry, requester)
        }
        return
    }
    var expanded by rememberSaveable(info.bvid) { mutableStateOf(false) }
    var focusedAction by rememberSaveable(info.bvid) { mutableStateOf("play") }
    var descriptionFocused by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val descriptionScroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val actionIds = listOf("play", "later", "expand", "description") +
        (if (info.pages.size > 1) info.pages.map { "part:${it.cid}" } else emptyList())
    val actionRequesters = remember(info.bvid, actionIds) {
        actionIds.associateWith { FocusRequester() }
    }
    val entryAction = focusedAction.takeIf { it in actionRequesters && (it != "description" || expanded) } ?: "play"
    fun actionModifier(id: String): Modifier = Modifier
        .then(if (id == entryAction) Modifier.focusRequester(requester) else Modifier)
        .focusRequester(actionRequesters.getValue(id))
        .onFocusChanged { if (it.isFocused) focusedAction = id }

    LaunchedEffect(requester, info.bvid) {
        actionRequesters.getValue(entryAction).requestFocus()
    }
    BackHandler(enabled = expanded) {
        expanded = false
        actionRequesters.getValue("expand").requestFocus()
    }
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val descriptionStep = with(density) { AppSpacingTokens.TripleExtraLarge.roundToPx() }
    BoxWithConstraints(modifier) {
        val wide = maxWidth >= 720.dp && fontScale <= 1.3f
        val coverWidth = (maxWidth * 0.4f).coerceAtMost(320.dp)
        val cover: @Composable () -> Unit = {
            AsyncImage(
                model = info.pic.let { if (it.startsWith("//")) "https:$it" else it },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .then(if (wide) Modifier.width(coverWidth) else Modifier.widthIn(max = 320.dp).fillMaxWidth())
                    .aspectRatio(16f / 9f)
                    .clip(TvUiTokens.shape(ContainerLevel.MediaCover)),
            )
        }
        val summary: @Composable () -> Unit = {
            Text(
                info.title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(info.owner.name, style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
            ) {
                TvAppButton(
                    onClick = { onPlay(info.cid) },
                    modifier = actionModifier("play").testTag("tv-play"),
                ) { Text(if (state.detailResumePositionMs > 0) "继续观看" else "播放") }
                TvAppButton(onClick = onWatchLater, modifier = actionModifier("later")) { Text("稍后再看") }
            }
            if (state.detailResumePositionMs > 0) {
                Text(
                    "上次看到 " + FormatUtils.formatDuration(state.detailResumePositionMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            state.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
        Column(
            Modifier.verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraLarge),
        ) {
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.DoubleExtraLarge)) {
                    cover()
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large)) { summary() }
                }
            } else {
                cover()
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large)) { summary() }
            }
            Text(
                info.desc.ifBlank { "暂无简介" },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = if (expanded) actionModifier("description")
                    .heightIn(max = 240.dp)
                    .onFocusChanged { descriptionFocused = it.isFocused }
                    .border(TvUiTokens.focusBorderWidth,
                        if (descriptionFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        TvUiTokens.shape(ContainerLevel.Card))
                    .padding(TvUiTokens.cardPadding)
                    .verticalScroll(descriptionScroll)
                    .onPreviewKeyEvent { event ->
                        val delta = when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_DOWN -> descriptionStep
                            KeyEvent.KEYCODE_DPAD_UP -> -descriptionStep
                            else -> 0
                        }
                        val canScroll = if (delta > 0) descriptionScroll.canScrollForward else descriptionScroll.canScrollBackward
                        if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN && delta != 0 && canScroll) {
                            scope.launch { descriptionScroll.scrollTo((descriptionScroll.value + delta).coerceIn(0, descriptionScroll.maxValue)) }
                            true
                        } else false
                    }
                    .focusable()
                    .testTag("tv-detail-description") else Modifier,
            )
            TvAppButton(
                onClick = { expanded = !expanded },
                modifier = actionModifier("expand").testTag("tv-detail-expand"),
            ) { Text(if (expanded) "收起详情" else "展开详情") }
            if (info.pages.size > 1) {
                Text("选集", style = MaterialTheme.typography.titleLarge)
                info.pages.forEach { page ->
                    TvNavigationItem(
                        selected = page.cid == info.cid,
                        onClick = { onPlay(page.cid) },
                        modifier = actionModifier("part:${page.cid}")
                            .fillMaxWidth()
                            .testTag("tv-part:${page.cid}"),
                    ) {
                        Text(
                            (if (page.cid == info.cid) "当前 · " else "") + "P${page.page} · ${page.part}",
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TvLoginContent(state: TvUiState, requester: FocusRequester, onRefresh: () -> Unit, onSignOut: () -> Unit) {
    LaunchedEffect(requester, state.qr.phase == QrPhase.Success || state.account != null) { requester.requestFocus() }
    Column(Modifier.fillMaxSize().padding(horizontal = TvUiTokens.pagePadding).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("账号", style = MaterialTheme.typography.headlineLarge)
        if (state.account != null || state.qr.phase == QrPhase.Success) {
            Text("已登录 · ${state.account?.uname ?: "正在读取账号信息"}", style = MaterialTheme.typography.titleLarge)
            Text("收藏、历史和稍后再看与当前账号同步。")
            state.accountError?.let { Text(it) }
            TvAppButton(onClick = onSignOut, modifier = Modifier.focusRequester(requester)) { Text("退出登录") }
        } else {
            Text("使用哔哩哔哩手机 App 扫码，并在手机上确认登录。", style = MaterialTheme.typography.bodyLarge)
            state.qr.bitmap?.let { bitmap ->
                Image(bitmap.asImageBitmap(), contentDescription = "扫码登录二维码",
                    modifier = Modifier.size(250.dp).background(Color.White).padding(8.dp))
            }
            Text(when (state.qr.phase) {
                QrPhase.Loading -> "正在生成二维码…"
                QrPhase.Waiting -> "等待扫码"
                QrPhase.Scanned -> "已扫码，请在手机上确认"
                QrPhase.Expired -> "二维码已过期，请刷新"
                QrPhase.Failed -> state.qr.error ?: "登录失败，请重试"
                QrPhase.Success -> "登录成功"
            })
            TvAppButton(onClick = onRefresh, modifier = Modifier.focusRequester(requester)) { Text("刷新二维码") }
            Text("返回即可取消本次登录。", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun TvSettingsContent(state: TvUiState, requester: FocusRequester, onQuality: (Int) -> Unit,
    onAutoContinue: () -> Unit, onDanmaku: () -> Unit, onPrivacy: () -> Unit, onClearSearchHistory: () -> Unit) {
    var chooseQuality by remember { mutableStateOf(false) }
    val rowModifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
    LaunchedEffect(requester, chooseQuality) { if (!chooseQuality) requester.requestFocus() }
    Column(Modifier.fillMaxSize().padding(horizontal = TvUiTokens.pagePadding).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("播放与隐私", style = MaterialTheme.typography.headlineMedium)
        Text("播放偏好", style = MaterialTheme.typography.titleMedium)
        TvAppButton(onClick = { chooseQuality = true }, modifier = rowModifier.focusRequester(requester)) {
            Text("默认画质：${VideoQuality.fromCode(state.quality)?.description ?: state.quality}")
        }
        TvNavigationItem(selected = state.autoContinue, onClick = onAutoContinue, modifier = rowModifier) { Text("播完自动播放下一 P：${if (state.autoContinue) "开启" else "关闭"}") }
        TvNavigationItem(selected = state.danmakuEnabled, onClick = onDanmaku, modifier = rowModifier) { Text("弹幕显示：${if (state.danmakuEnabled) "开启" else "关闭"}") }
        Text("画质可用性由账号权限、视频内容和设备能力决定。", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary, modifier = rowModifier)
        Text("隐私与记录", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = AppSpacingTokens.Small))
        TvNavigationItem(selected = state.privacyMode, onClick = onPrivacy, modifier = rowModifier) { Text("暂停上报观看历史：${if (state.privacyMode) "开启" else "关闭"}") }
        TvAppButton(onClick = onClearSearchHistory, modifier = rowModifier) { Text("清空搜索历史") }
    }
    if (chooseQuality) TvChoiceDialog("默认画质", listOf(32, 64, 80, 112, 116, 120).map {
        it to (VideoQuality.fromCode(it)?.description ?: "$it")
    }, onDismiss = { chooseQuality = false }, onChoose = { onQuality(it); chooseQuality = false }, selectedValue = state.quality)
}

@Composable
internal fun <T> TvChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    onDismiss: () -> Unit,
    onChoose: (T) -> Unit,
    selectedValue: T? = null,
    modifier: Modifier = Modifier,
) {
    val requester = remember { FocusRequester() }
    val selectedIndex = options.indexOfFirst { it.first == selectedValue }.coerceAtLeast(0)
    Dialog(onDismissRequest = onDismiss) {
        Column(modifier.widthIn(max = 520.dp).fillMaxWidth().heightIn(max = 440.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, TvUiTokens.shape(ContainerLevel.Dialog))
            .verticalScroll(rememberScrollState()).padding(TvUiTokens.pagePadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            options.forEachIndexed { index, (value, label) ->
                TvNavigationItem(selected = value == selectedValue, onClick = { onChoose(value) },
                    modifier = Modifier.fillMaxWidth()
                        .then(if (index == selectedIndex) Modifier.focusRequester(requester) else Modifier)) {
                    Text((if (value == selectedValue) "当前 · " else "") + label)
                }
            }
            TvAppButton(onClick = onDismiss,
                modifier = if (options.isEmpty()) Modifier.focusRequester(requester) else Modifier) { Text("取消") }
        }
        LaunchedEffect(requester) { requester.requestFocus() }
    }
}
