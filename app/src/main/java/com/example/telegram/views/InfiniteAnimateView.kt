package com.example.telegram.views

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
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

    /**
     * We can not use `onVisibilityAggregated` since it is introduced from sdk 24, but we have min = 21
     */
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (isDeepVisible()) startAnimation() else stopAnimation()
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
        if (!isAttachedToWindow || !isDeepVisible()) return
        if (animation == null) animation = createAnimation().apply { start() }
    }

    protected abstract fun createAnimation(): Animator

    private fun stopAnimation() {
        animation?.cancel()
        animation = null
    }

    /**
     * Probably this function implements View.isShown, but I read that there are some issues with it
     * And I also faced with those issues in Lottie lib. Since we have as always no time to completelly
     * investigate this, I decided to put this small and simple method just to be sure it does,
     * what exactly I need :)
     *
     * Upd: tried to use isShown instead of this method, and it didn't work out. So if you know
     * how to improve that, you most welcome :)
     */
    private fun isDeepVisible(): Boolean {
        var isVisible = isVisible
        var parent = parentView
        while (parent != null && isVisible) {
            isVisible = isVisible && parent.isVisible
            parent = parent.parentView
        }
        return isVisible
    }

    private val View.parentView: ViewGroup? get() = parent as? ViewGroup
}
