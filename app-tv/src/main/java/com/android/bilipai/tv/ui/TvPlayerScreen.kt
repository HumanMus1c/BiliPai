@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.android.bilipai.tv.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.ui.PlayerView
import com.android.bilipai.tv.ui.components.TvAppButton
import com.android.bilipai.tv.ui.components.TvPlaybackProgress
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.bilipai.tv.TvRoute
import com.android.purebilibili.core.player.PlaybackStatus
import com.android.purebilibili.core.player.SharedPlaybackRequest
import com.android.purebilibili.core.player.SharedPlaybackSession
import com.android.purebilibili.core.player.SharedPlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private enum class PlayerDialog { Quality, Speed, Episodes }

@Composable
internal fun TvPlayerRoute(route: TvRoute, defaultQuality: Int, autoContinue: Boolean, danmakuEnabled: Boolean, onCheckpoint: (SharedPlaybackState) -> Unit, onBack: (SharedPlaybackState) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val session = remember { SharedPlaybackSession(context) }
    val playbackState = session.state.collectAsStateWithLifecycle()
    val state by playbackState
    var cid by remember { mutableLongStateOf(route.cid) }
    var quality by remember { mutableIntStateOf(defaultQuality) }
    var retry by remember { mutableIntStateOf(0) }
    var startPosition by remember { mutableStateOf<Long?>(null) }
    var playOnLoad by remember { mutableStateOf(true) }
    var controls by remember { mutableStateOf(false) }
    var pendingSeek by remember { mutableStateOf<Long?>(null) }
    var seekFocused by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<PlayerDialog?>(null) }
    var lastInteraction by remember { mutableLongStateOf(android.os.SystemClock.elapsedRealtime()) }
    val rootFocus = remember { FocusRequester() }
    val playFocus = remember { FocusRequester() }
    val seekFocus = remember { FocusRequester() }
    val qualityFocus = remember { FocusRequester() }
    val speedFocus = remember { FocusRequester() }
    val episodesFocus = remember { FocusRequester() }
    var restoreFocus by remember { mutableStateOf<FocusRequester?>(null) }

    val latestCheckpoint by rememberUpdatedState(onCheckpoint)
    DisposableEffect(session, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) {
            session.pause(); session.tick(); latestCheckpoint(session.state.value)
        } }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); session.close() }
    }
    LaunchedEffect(session, cid, quality, retry) {
        session.load(SharedPlaybackRequest(route.bvid, route.aid, cid, quality, startPosition),
            playOnLoad && owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    LaunchedEffect(session, owner) {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var ticks = 0
            while (isActive) {
                session.tick()
                if (++ticks % 10 == 0) session.persistPosition()
                if (ticks % 60 == 0) session.reportProgress()
                delay(500)
            }
        }
    }
    LaunchedEffect(state.status, autoContinue) {
        if (state.status != PlaybackStatus.Ended) return@LaunchedEffect
        val pages = state.info?.pages.orEmpty()
        val currentIndex = pages.indexOfFirst { it.cid == state.info?.cid }
        val next = if (currentIndex >= 0) pages.getOrNull(currentIndex + 1) else null
        if (autoContinue && next != null) {
            latestCheckpoint(state)
            startPosition = 0; cid = next.cid; playOnLoad = true
        } else controls = true
    }
    LaunchedEffect(cid, state.canSeek) {
        if (!state.canSeek && pendingSeek != null) {
            pendingSeek = null
            restoreFocus = playFocus
        }
    }
    LaunchedEffect(state.status) {
        if (state.status == PlaybackStatus.Failed) { controls = false; pendingSeek = null; dialog = null }
    }
    LaunchedEffect(controls, pendingSeek != null, dialog, state.status == PlaybackStatus.Failed) {
        if (dialog != null || state.status == PlaybackStatus.Failed) return@LaunchedEffect
        if (controls) {
            if (pendingSeek != null) seekFocus.requestFocus()
            else (restoreFocus ?: playFocus).requestFocus()
            restoreFocus = null
        } else rootFocus.requestFocus()
    }
    LaunchedEffect(controls, lastInteraction, dialog, pendingSeek, state.playing) {
        if (controls && dialog == null && pendingSeek == null && state.playing) {
            delay(8_000); controls = false
        }
    }

    fun leavePlayer() { session.pause(); session.tick(); onBack(session.state.value) }
    fun interact() { lastInteraction = android.os.SystemClock.elapsedRealtime() }
    fun back() {
        when (resolveTvPlayerBack(dialog != null, pendingSeek != null, controls)) {
            TvPlayerBackAction.CloseDialog -> dialog = null
            TvPlayerBackAction.CancelSeek -> { restoreFocus = seekFocus; pendingSeek = null; interact() }
            TvPlayerBackAction.HideControls -> controls = false
            TvPlayerBackAction.LeavePlayer -> leavePlayer()
        }
    }
    BackHandler(onBack = { back() })

    Box(Modifier.fillMaxSize().background(Color.Black).focusRequester(rootFocus)
        .onPreviewKeyEvent { event ->
            val key = event.nativeKeyEvent
            val code = key.keyCode
            if (key.action == KeyEvent.ACTION_DOWN) interact()
            if (state.status == PlaybackStatus.Failed) return@onPreviewKeyEvent false
            when (code) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    if (key.action == KeyEvent.ACTION_UP) when (code) {
                        KeyEvent.KEYCODE_MEDIA_PLAY -> if (state.status == PlaybackStatus.Ended) session.replay() else session.player.play()
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> session.pause()
                        else -> if (state.status == PlaybackStatus.Ended) session.replay() else session.togglePlayPause()
                    }
                    true
                }
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (dialog != null || !state.canSeek || state.durationMs <= 0 || controls && !seekFocused && pendingSeek == null) false
                    else {
                        if (key.action == KeyEvent.ACTION_DOWN) {
                            pendingSeek = tvSeekTarget(pendingSeek ?: state.positionMs,
                                if (code == KeyEvent.KEYCODE_DPAD_LEFT) -10_000 else 10_000, state.durationMs)
                            controls = true
                        }
                        true
                    }
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_BUTTON_A -> {
                    when {
                        dialog != null -> false
                        pendingSeek != null -> {
                            if (key.action == KeyEvent.ACTION_UP) {
                                pendingSeek?.let(session::seekTo)
                                restoreFocus = seekFocus; pendingSeek = null; interact()
                            }
                            true
                        }
                        !controls -> { if (key.action == KeyEvent.ACTION_UP) controls = true; true }
                        else -> false
                    }
                }
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                    when {
                        pendingSeek != null && dialog == null -> true
                        !controls && dialog == null -> { if (key.action == KeyEvent.ACTION_DOWN) controls = true; true }
                        else -> false
                    }
                }
                else -> false
            }
        }.focusable().testTag("tv-player")) {
        AndroidView(factory = { viewContext -> PlayerView(viewContext).apply {
            player = session.player; useController = false; isFocusable = false
            descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
            keepScreenOn = true
        } }, update = { it.player = session.player }, modifier = Modifier.fillMaxSize())
        if (danmakuEnabled && state.status != PlaybackStatus.Failed) {
            TvDanmakuOverlay(session = session, state = state, cid = cid, modifier = Modifier.fillMaxSize())
        }
        // A pointer has its own activation surface; the native video view never owns D-pad focus.
        if (!controls && state.status != PlaybackStatus.Failed) Box(Modifier.fillMaxSize().clickable { interact(); controls = true })
        if (state.status == PlaybackStatus.Loading || state.buffering) {
            Text("正在加载…", modifier = Modifier.align(Alignment.Center).background(Color(0xB010141F)).padding(20.dp))
        }
        if (state.status == PlaybackStatus.Failed) {
            val retryFocus = remember { FocusRequester() }
            LaunchedEffect(retryFocus, state.error) { retryFocus.requestFocus() }
            Column(Modifier.align(Alignment.Center).background(Color(0xDD10141F)).padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(state.error ?: "播放失败")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TvAppButton(onClick = { startPosition = state.positionMs.takeIf { state.durationMs > 0 }; retry++ }, modifier = Modifier.focusRequester(retryFocus)) { Text("重试") }
                    TvAppButton(onClick = { startPosition = state.positionMs.takeIf { state.durationMs > 0 }; quality = 32; retry++ }) { Text("以 480P 重试") }
                    TvAppButton(onClick = { leavePlayer() }) { Text("返回详情") }
                }
            }
        } else if (controls) {
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xB010141F), Color(0xE610141F), Color(0xF510141F))))
                .verticalScroll(rememberScrollState()).padding(TvUiTokens.pagePadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large)) {
                Text(state.info?.title ?: route.label, style = MaterialTheme.typography.titleLarge,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                TvPlaybackProgress(
                    positionMsProvider = { playbackState.value.positionMs },
                    bufferedPositionMsProvider = { playbackState.value.bufferedPositionMs },
                    durationMs = state.durationMs,
                    previewPositionMs = pendingSeek,
                    canSeek = state.canSeek,
                    onStartPreview = { pendingSeek = state.positionMs; interact() },
                    modifier = Modifier.fillMaxWidth().focusRequester(seekFocus)
                        .onFocusChanged { seekFocused = it.isFocused },
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
                    TvAppButton(onClick = { interact(); if (state.status == PlaybackStatus.Ended) session.replay() else session.togglePlayPause() },
                        modifier = Modifier.focusRequester(playFocus).testTag("tv-play-pause")) { Text(if (state.status == PlaybackStatus.Ended) "重新播放" else if (state.playing) "暂停" else "播放") }
                    TvAppButton(onClick = { interact(); restoreFocus = qualityFocus; dialog = PlayerDialog.Quality }, modifier = Modifier.focusRequester(qualityFocus)) { Text("画质") }
                    TvAppButton(onClick = { interact(); restoreFocus = speedFocus; dialog = PlayerDialog.Speed }, modifier = Modifier.focusRequester(speedFocus)) { Text("倍速") }
                    if (state.info?.pages.orEmpty().size > 1) TvAppButton(onClick = { interact(); restoreFocus = episodesFocus; dialog = PlayerDialog.Episodes }, modifier = Modifier.focusRequester(episodesFocus)) { Text("选集") }
                    TvAppButton(onClick = { controls = false }) { Text("收起") }
                    TvAppButton(onClick = { leavePlayer() }) { Text("返回详情") }
                }
            }
        }
    }
    when (dialog) {
        PlayerDialog.Quality -> TvChoiceDialog("画质", state.qualities, onDismiss = { dialog = null }, onChoose = {
            latestCheckpoint(state)
            startPosition = state.positionMs; playOnLoad = state.playing; quality = it; dialog = null
        }, selectedValue = state.actualQuality)
        PlayerDialog.Speed -> TvChoiceDialog("倍速", listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).map { it to "${it}x" },
            onDismiss = { dialog = null }, onChoose = { session.setSpeed(it); dialog = null },
            selectedValue = session.player.playbackParameters.speed)
        PlayerDialog.Episodes -> TvChoiceDialog("选集", state.info?.pages.orEmpty().map { it.cid to "P${it.page} · ${it.part}" },
            onDismiss = { dialog = null }, onChoose = {
                latestCheckpoint(state)
                cid = it; startPosition = null; playOnLoad = true; dialog = null
            }, selectedValue = state.info?.cid)
        null -> Unit
    }
}
