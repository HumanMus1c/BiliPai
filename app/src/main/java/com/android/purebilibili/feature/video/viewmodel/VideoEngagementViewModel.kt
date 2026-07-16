package com.android.purebilibili.feature.video.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VideoEngagementSeed(
    val isLoggedIn: Boolean = false,
    val isVip: Boolean = false,
    val isFollowing: Boolean = false,
    val isFavorited: Boolean = false,
    val isLiked: Boolean = false,
    val coinCount: Int = 0,
    val isInWatchLater: Boolean = false,
    val followingMids: Set<Long> = emptySet()
)

data class VideoEngagementUiState(
    val subject: VideoSubjectSnapshot? = null,
    val isLoggedIn: Boolean = false,
    val isVip: Boolean = false,
    val isFollowing: Boolean = false,
    val isFavorited: Boolean = false,
    val isLiked: Boolean = false,
    val coinCount: Int = 0,
    val isInWatchLater: Boolean = false,
    val followingMids: Set<Long> = emptySet(),
    val coinDialogVisible: Boolean = false,
    val likeBurstVisible: Boolean = false,
    val tripleCelebrationVisible: Boolean = false
)

sealed interface VideoEngagementEvent {
    data class Message(val text: String) : VideoEngagementEvent
}

class VideoEngagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VideoEngagementUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<VideoEngagementEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun bindSubject(subject: VideoSubjectSnapshot, seed: VideoEngagementSeed) {
        if (!shouldRebindVideoDomain(_uiState.value.subject, subject)) {
            sync(seed)
            return
        }
        _uiState.value = VideoEngagementUiState(
            subject = subject,
            isLoggedIn = seed.isLoggedIn,
            isVip = seed.isVip,
            isFollowing = seed.isFollowing,
            isFavorited = seed.isFavorited,
            isLiked = seed.isLiked,
            coinCount = seed.coinCount,
            isInWatchLater = seed.isInWatchLater,
            followingMids = seed.followingMids
        )
    }

    fun sync(seed: VideoEngagementSeed) {
        _uiState.update { current ->
            current.copy(
                isLoggedIn = seed.isLoggedIn,
                isVip = seed.isVip,
                isFollowing = seed.isFollowing,
                isFavorited = seed.isFavorited,
                isLiked = seed.isLiked,
                coinCount = seed.coinCount,
                isInWatchLater = seed.isInWatchLater,
                followingMids = seed.followingMids
            )
        }
    }

    fun setCoinDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(coinDialogVisible = visible) }
    }

    fun dismissLikeBurst() {
        _uiState.update { it.copy(likeBurstVisible = false) }
    }

    fun dismissTripleCelebration() {
        _uiState.update { it.copy(tripleCelebrationVisible = false) }
    }

    internal fun emitMessage(message: String) {
        viewModelScope.launch { _events.send(VideoEngagementEvent.Message(message)) }
    }
}

internal fun VideoPlaybackUiState.Success.toEngagementSeed(): VideoEngagementSeed =
    VideoEngagementSeed(
        isLoggedIn = isLoggedIn,
        isVip = isVip,
        isFollowing = isFollowing,
        isFavorited = isFavorited,
        isLiked = isLiked,
        coinCount = coinCount,
        isInWatchLater = isInWatchLater,
        followingMids = followingMids
    )
