package com.android.purebilibili.core.ui.transition.native

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

internal class NativeVideoCardTransitionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private val cardRect = RectF()

    private var frame: NativeVideoCardTransitionFrame? = null

    init {
        visibility = GONE
        isClickable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }

    fun showFrame(frame: NativeVideoCardTransitionFrame) {
        this.frame = frame
        visibility = VISIBLE
        isClickable = true
        invalidate()
    }

    fun clearFrame() {
        frame = null
        visibility = GONE
        isClickable = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentFrame = frame ?: return

        if (currentFrame.scrimAlpha > 0f) {
            scrimPaint.color = Color.argb((currentFrame.scrimAlpha * 255).toInt(), 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        }

        val rect = currentFrame.cardRect
        cardRect.set(rect.left, rect.top, rect.right, rect.bottom)
        canvas.drawRoundRect(
            cardRect,
            currentFrame.cornerRadiusPx,
            currentFrame.cornerRadiusPx,
            cardPaint
        )
    }
}
