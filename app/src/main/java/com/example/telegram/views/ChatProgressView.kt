package com.example.telegram.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import com.example.telegram.R
import kotlin.math.min
import kotlin.properties.Delegates.observable

/**
 * 负责绘制所需的进度 View
 */
class ChatProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : InfiniteAnimateView(context, attrs, defStyleAttr) {

    private val defaultBgColor: Int = ContextCompat.getColor(context, R.color.chat_progress_bg)
    private val defaultBgStrokeColor: Int =
        ContextCompat.getColor(context, R.color.chat_progress_bg_stroke)
    private val defaultProgressColor: Int =
        ContextCompat.getColor(context, R.color.chat_progress_stroke)

    private val progressPadding = context.resources.getDimension(R.dimen.chat_progress_padding)

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = defaultBgColor
    }
    private val bgStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = defaultBgStrokeColor
        strokeWidth = context.resources.getDimension(R.dimen.chat_progress_bg_stroke_width)
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen.chat_progress_stroke_width)
        color = defaultProgressColor
    }

    // in degrees [0, 360)
    private var currentAngle: Float by observable(0f) { _, _, _ -> invalidate() }
    private var sweepAngle: Float = MIN_SWEEP_ANGLE

    private val progressRect: RectF = RectF()
    private var bgRadius: Float = 0f

    private val typedArray =
        context.obtainStyledAttributes(attrs, R.styleable.ChatProgressView, defStyleAttr, 0)

    init {
        try {
            progressPaint.color = typedArray.getColor(
                R.styleable.ChatProgressView_progress_color,
                defaultProgressColor
            )
            bgStrokePaint.color = typedArray.getColor(
                R.styleable.ChatProgressView_progress_stroke_color,
                defaultBgStrokeColor
            )
            progressPaint.color = typedArray.getColor(
                R.styleable.ChatProgressView_progress_background_color,
                defaultProgressColor
            )
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val verticalHalf = (measuredHeight - paddingTop - paddingBottom) / 2f
        val horizontalHalf = (measuredWidth - paddingStart - paddingEnd) / 2f
        val progressOffset = progressPadding + progressPaint.strokeWidth / 2f

        // since the stroke it drawn on center of the line, we need to safe space for half of it,
        // or it will be truncated by the bounds
        // 由于笔画在线的中心，我们需要为它留出一半的安全空间，否则它将被截断的界限
        bgRadius = min(horizontalHalf, verticalHalf) - bgStrokePaint.strokeWidth / 2f

        val progressRectMinSize = 2 * (min(horizontalHalf, verticalHalf) - progressOffset)
        progressRect.apply {
            left = (measuredWidth - progressRectMinSize) / 2f
            top = (measuredHeight - progressRectMinSize) / 2f
            right = (measuredWidth + progressRectMinSize) / 2f
            bottom = (measuredHeight + progressRectMinSize) / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        with(canvas) {
            // (radius - strokeWidth) - because we don't want to overlap colors (since they by default translucent)
            drawCircle(
                progressRect.centerX(),
                progressRect.centerY(),
                bgRadius - bgStrokePaint.strokeWidth / 2f,
                bgPaint
            )
            sweepAngle
            drawCircle(progressRect.centerX(), progressRect.centerY(), bgRadius, bgStrokePaint)
            // 1. 在这种情况下，弧线只在一个方向上 "增加"
            drawArc(progressRect, currentAngle, sweepAngle, false, progressPaint)
            // 2. 在这种情况下，弧线在两个方向上 "增加"
            // drawArc(progressRect, currentAngle - sweepAngle / 2f, sweepAngle, false, progressPaint)
            // 3. 在这种情况下，弧线向另一个方向 "增加"
            // drawArc(progressRect, currentAngle - sweepAngle, sweepAngle, false, progressPaint)
        }
    }

    override fun createAnimation(): Animator =
        ValueAnimator.ofFloat(currentAngle, currentAngle + MAX_ANGLE).apply {
            interpolator = LinearInterpolator()
            duration = SPIN_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                currentAngle = normalize(it.animatedValue as Float)
                sweepAngle = convertToSweepAngle(
                    if ((it.animatedValue as Float) / 360F >= 0.5) {
                        (it.animatedValue as Float) / 360F
                    } else {
                        1 - (it.animatedValue as Float) / 360F
                    }
                )
            }
        }

    /**
     * converts (shifts) the angle to be from 0 to 360.
     * For instance: if angle = 400.54, the normalized version will be 40.54
     * Note: angle = 360 will be normalized to 0
     *
     * 将任意角缩小回 [0, 2π) 区间内
     */
    private fun normalize(angle: Float): Float {
        val decimal = angle - angle.toInt()
        return angle.toInt() % MAX_ANGLE + decimal
    }

    private fun convertToSweepAngle(progress: Float): Float =
        MIN_SWEEP_ANGLE + progress * (MAX_ANGLE - MIN_SWEEP_ANGLE)

    private companion object {
        // 旋转一圈的时间
        const val SPIN_DURATION_MS = 1_000L

        // 最小扫过角度
        const val MIN_SWEEP_ANGLE = 36f

        // 最大角度
        const val MAX_ANGLE = 360
    }
}
