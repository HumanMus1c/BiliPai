// 文件路径: core/ui/LottieComponents.kt
package com.android.purebilibili.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

/**
 *  Lottie 动画加载器
 * 使用在线 Lottie 动画 URL
 */
object LottieUrls {
    //  精选免费 Lottie 动画资源
    const val LOADING_DOTS = "https://lottie.host/5d9d2c7c-d7f4-4f3e-9e9f-f8a6e1f8c3a1/loading.json"
    const val LOADING_CIRCLE = "https://assets2.lottiefiles.com/packages/lf20_p8bfn5to.json"
    const val LOADING_BILIBILI = "https://assets10.lottiefiles.com/packages/lf20_jcikwtux.json"
    const val LIKE_HEART = "https://assets4.lottiefiles.com/packages/lf20_hc7rwmvb.json"
    const val STAR = "https://assets9.lottiefiles.com/packages/lf20_c50nklxn.json"
    const val CONFETTI = "https://assets10.lottiefiles.com/packages/lf20_u4yrau.json"
    const val SUCCESS = "https://assets4.lottiefiles.com/packages/lf20_jbrw3hcz.json"
    const val ERROR = "https://assets1.lottiefiles.com/packages/lf20_cr9slsdh.json"
    const val EMPTY = "https://raw.githubusercontent.com/DrKLO/Telegram/master/TMessagesProj/src/main/res/raw/utyan_empty2.json"
    const val REFRESH = "https://assets3.lottiefiles.com/packages/lf20_ykzaax7v.json"
    
    //  新手引导页面动画
    const val WELCOME = "https://assets10.lottiefiles.com/packages/lf20_u4yrau.json"  // 庆祝彩屑
    const val THEME_COLORS = "https://assets5.lottiefiles.com/packages/lf20_jtbfg2nb.json"  // 彩虹渐变
    const val VIDEO_PLAY = "https://assets8.lottiefiles.com/packages/lf20_khzniaya.json"  // 播放按钮
    
    //  Telegram 风格设置页面动画 (使用 Telegram Android 开源项目的官方动画)
    // 来源: https://github.com/DrKLO/Telegram/tree/master/TMessagesProj/src/main/res/raw
    
    private const val TELEGRAM_RAW_BASE = "https://raw.githubusercontent.com/DrKLO/Telegram/master/TMessagesProj/src/main/res/raw"
    
    //  插件中心 - 使用添加图标动画
    const val SETTINGS_PLUGINS = "$TELEGRAM_RAW_BASE/addone_icon.json"
    
    //  外观设置 - 使用相机/图库动画
    const val SETTINGS_APPEARANCE = "$TELEGRAM_RAW_BASE/attach_gallery.json"
    
    //  主题设置 - 使用夜间模式切换动画
    const val SETTINGS_THEME = "$TELEGRAM_RAW_BASE/auto_night_off.json"
    
    // ✨ 动画与效果 - 使用火焰开启动画
    const val SETTINGS_ANIMATION = "$TELEGRAM_RAW_BASE/fire_on.json"
    
    //  播放设置 - 使用相机动画
    const val SETTINGS_PLAYBACK = "$TELEGRAM_RAW_BASE/camera.json"
    
    // 🛡️ 隐私/权限 - 使用消息锁动画
    const val SETTINGS_PRIVACY = "$TELEGRAM_RAW_BASE/large_message_lock.json"
    
    //  设备 - 使用 iPhone 设备动画
    const val SETTINGS_DEVICE = "$TELEGRAM_RAW_BASE/iphone_30.json"
    // 🔔 通知 - 使用静音/取消静音动画
    const val SETTINGS_NOTIFICATION = "$TELEGRAM_RAW_BASE/ic_unmute.json"
    //  数据 - 使用下载动画
    const val SETTINGS_DATA = "$TELEGRAM_RAW_BASE/ic_download.json"
    //  聊天 - 使用气泡动画
    const val SETTINGS_CHAT = "$TELEGRAM_RAW_BASE/bubble.json"
    // 📁 文件夹 - 使用文件夹导入动画
    const val SETTINGS_FOLDER = "$TELEGRAM_RAW_BASE/folder_in.json"
    //  手势滑动 - 使用滑动回复提示动画
    const val SETTINGS_SWIPE = "$TELEGRAM_RAW_BASE/hint_swipe_reply.json"
    
    // ➕ 更多 Telegram 设置动画
    //  同步/联系人
    const val SETTINGS_SYNC = "$TELEGRAM_RAW_BASE/contacts_sync_on.json"
    // 📤 转发
    const val SETTINGS_FORWARD = "$TELEGRAM_RAW_BASE/forward.json"
    //  归档
    const val SETTINGS_ARCHIVE = "$TELEGRAM_RAW_BASE/chats_archive.json"
    //  复制
    const val SETTINGS_COPY = "$TELEGRAM_RAW_BASE/copy.json"
    // 🎁 礼物/打赏
    const val SETTINGS_GIFT = "$TELEGRAM_RAW_BASE/gift.json"
    // ℹ️ 信息/关于
    const val SETTINGS_INFO = "$TELEGRAM_RAW_BASE/info.json"
    //  筛选/过滤
    const val SETTINGS_FILTER = "$TELEGRAM_RAW_BASE/filters.json"
    //  完成/成功
    const val SETTINGS_DONE = "$TELEGRAM_RAW_BASE/done.json"
    //  错误
    const val SETTINGS_ERROR = "$TELEGRAM_RAW_BASE/error.json"
    // 🌐 翻译
    const val SETTINGS_TRANSLATE = "$TELEGRAM_RAW_BASE/msg_translate.json"
    // 📞 来电
    const val SETTINGS_CALLS = "$TELEGRAM_RAW_BASE/incoming_calls.json"
    // 👋 手势
    const val SETTINGS_GESTURE = "$TELEGRAM_RAW_BASE/hand_1.json"
}

/**
 *  通用 Lottie 动画组件
 */
@Composable
fun LottieAnimation(
    url: String,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    iterations: Int = LottieConstants.IterateForever,
    autoPlay: Boolean = true
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Url(url)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = autoPlay
    )
    
    com.airbnb.lottie.compose.LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}
/**
 *  加载动画组件（按 UI 预设分发：iOS 吉祥物 / MD3 LoadingIndicator / Miuix 进度环）
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    text: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AdaptiveLoadingIndicator(
            size = size,
            strokeWidth = 2.4.dp,
        )
        if (text != null) {
            Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Theme-aware loading indicator entry used across feature screens.
 *
 * Historically iOS-only cute person; now routes through [AdaptiveLoadingIndicator]
 * so MD3 uses the official morphing [androidx.compose.material3.LoadingIndicator]
 * (dynamic primary) and Miuix uses native progress chrome. iOS keeps the mascot.
 *
 * @param size optional visual size. Prefer this over [Modifier.size] so compact
 *   slots (≤ 32.dp) can select the compact circular recipe on MD3/Miuix.
 */
@Composable
fun CutePersonLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = 2.dp,
    size: Dp? = null,
) {
    AdaptiveLoadingIndicator(
        modifier = modifier,
        size = size,
        color = color,
        strokeWidth = strokeWidth,
    )
}

/**
 *  点赞动画按钮
 */
@Composable
fun LikeButton(
    isLiked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    var isPlaying by remember { mutableStateOf(false) }
    
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Url(LottieUrls.LIKE_HEART)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = true,
        iterations = 1
    )
    
    // 动画播放完毕后重置状态
    LaunchedEffect(progress) {
        if (progress == 1f) {
            isPlaying = false
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clickable {
                if (!isLiked) {
                    isPlaying = true
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLiked || isPlaying) {
            com.airbnb.lottie.compose.LottieAnimation(
                composition = composition,
                progress = { if (isLiked && !isPlaying) 1f else progress },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 未点赞状态显示静态图标
            com.airbnb.lottie.compose.LottieAnimation(
                composition = composition,
                progress = { 0f },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 *  收藏动画按钮
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    var isPlaying by remember { mutableStateOf(false) }
    
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Url(LottieUrls.STAR)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = true,
        iterations = 1
    )
    
    LaunchedEffect(progress) {
        if (progress == 1f) {
            isPlaying = false
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clickable {
                if (!isFavorite) {
                    isPlaying = true
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        com.airbnb.lottie.compose.LottieAnimation(
            composition = composition,
            progress = { if (isFavorite && !isPlaying) 1f else progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 *  空状态组件
 *  支持点击动画彩蛋：连续点击会触发有趣的提示
 */
@Composable
fun EmptyState(
    message: String = "暂无内容",
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    //  [彩蛋] 是否启用点击彩蛋
    enableEasterEgg: Boolean = true
) {
    //  点击计数器触发彩蛋
    var clickCount by remember { mutableIntStateOf(0) }
    var easterEggMessage by remember { mutableStateOf<String?>(null) }
    
    //  点击彩蛋消息列表
    val easterEggMessages = remember {
        listOf(
            "别戳我啦～ 😆",
            "我只是个空状态... 🥺",
            "再点也不会有内容的！",
            "你在找什么？🔍",
            "好无聊啊～ 去看点视频吧！",
            "点点点，你可真会点！",
            "咚咚咚！有人在家吗？🚪"
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacingTokens.DoubleExtraLarge)
            .then(
                if (enableEasterEgg) {
                    Modifier.clickable {
                        clickCount++
                        if (clickCount >= 3) {
                            easterEggMessage = easterEggMessages.random()
                        }
                        if (clickCount >= 7) {
                            clickCount = 0  // 重置
                        }
                    }
                } else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            url = LottieUrls.EMPTY,
            size = 150.dp
        )
        Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
        
        //  显示彩蛋消息或默认消息（使用柔和的主题色）
        Text(
            text = easterEggMessage ?: message,
            style = MaterialTheme.typography.bodyLarge,
            color = if (easterEggMessage != null) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

/**
 *  错误状态组件
 *  支持点击彩蛋：连续点击会显示鼓励消息
 */
@Composable
fun ErrorState(
    message: String = "加载失败",
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    //  [彩蛋] 是否启用点击彩蛋
    enableEasterEgg: Boolean = true
) {
    //  点击计数器触发彩蛋
    var clickCount by remember { mutableIntStateOf(0) }
    var showEncouragement by remember { mutableStateOf(false) }
    
    //  鼓励消息列表
    val encouragements = remember {
        listOf(
            "别灰心！再试一次～ 💪",
            "网络可能在打盹... 😴",
            "加载失败也要保持微笑！😊",
            "休息一下再试试？☕",
            "服务器正在努力中... 🏃",
            "别担心，问题不大！👌"
        )
    }
    
    val displayMessage = if (showEncouragement) {
        encouragements.random()
    } else message
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacingTokens.DoubleExtraLarge)
            .then(
                if (enableEasterEgg) {
                    Modifier.clickable {
                        clickCount++
                        if (clickCount >= 3) {
                            showEncouragement = true
                        }
                        if (clickCount >= 5) {
                            clickCount = 0
                            showEncouragement = false
                        }
                    }
                } else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            url = LottieUrls.ERROR,
            size = 120.dp,
            iterations = 1
        )
        Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = if (showEncouragement)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
            Text(
                text = if (showEncouragement) "冲鸭！" else "点击重试",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRetry() }
            )
        }
    }
}

/**
 *  成功动画
 */
@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    onFinished: () -> Unit = {}
) {
    var finished by remember { mutableStateOf(false) }
    
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Url(LottieUrls.SUCCESS)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )
    
    LaunchedEffect(progress) {
        if (progress == 1f && !finished) {
            finished = true
            onFinished()
        }
    }
    
    com.airbnb.lottie.compose.LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}
