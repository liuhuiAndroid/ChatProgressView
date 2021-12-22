package com.example.telegram.views

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible

/**
 * 抽象 View，负责动画的准备、启动和停止。
 */
abstract class InfiniteAnimateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var animation: Animator? = null

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) startAnimation() else stopAnimation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    private fun startAnimation() {
        if (!isVisible || windowVisibility != VISIBLE) return
        if (animation == null) animation = createAnimation().apply { start() }
    }

    protected abstract fun createAnimation(): Animator

    private fun stopAnimation() {
        animation?.cancel()
        animation = null
    }

}
