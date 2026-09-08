package com.android.purebilibili.core.ui.motion

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.SinkFeedback
import top.yukonga.miuix.kmp.utils.TiltFeedback
import top.yukonga.miuix.kmp.utils.pressable

/**
 * 依据当前系统主题自适应应用 Miuix 官方触控按压反馈。
 *
 * 当处于 [AppUiStyle.MIUIX] 模式时，按压控件会触发 0.94x 下沉微缩与 Folme 物理弹簧回弹；
 * 在 Material 3 模式下保持 No-op，遵循该风格的标准交互指引。
 *
 * @param enabled 是否开启按压反馈
 * @param feedbackType 反馈效果类型，默认 [PressFeedbackType.Sink]
 * @param interactionSource 交互事件流；若不传入则在内部自动创建并维持
 */
@Composable
fun Modifier.adaptiveMiuixPressFeedback(
    enabled: Boolean = true,
    feedbackType: PressFeedbackType = PressFeedbackType.Sink,
    interactionSource: MutableInteractionSource? = null,
): Modifier {
    if (!enabled || feedbackType == PressFeedbackType.None) return this
    val uiStyle = LocalAppUiStyle.current
    if (uiStyle != AppUiStyle.MIUIX) return this

    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val feedback = remember(feedbackType) {
        when (feedbackType) {
            PressFeedbackType.None -> null
            PressFeedbackType.Sink -> SinkFeedback()
            PressFeedbackType.Tilt -> TiltFeedback()
        }
    }

    return this.pressable(
        interactionSource = resolvedInteractionSource,
        indication = feedback,
        delay = null,
    )
}
