package com.bbv.base.util

import android.view.MotionEvent
import android.view.View
import androidx.databinding.BindingAdapter

/**
 * 带按压缩放动画的点击效果
 */
fun View.setOnScaleClickListener(
    smallScale: Float = 0.9f,
    originalScale: Float = 1.0f,
    duration: Long = 150,
    onClick: (View) -> Unit
) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(smallScale)
                    .scaleY(smallScale)
                    .setDuration(duration)
                    .start()
            }
            MotionEvent.ACTION_UP -> {
                val isInside = event.x in 0f..v.width.toFloat() &&
                        event.y in 0f..v.height.toFloat()
                v.animate()
                    .scaleX(originalScale)
                    .scaleY(originalScale)
                    .setDuration(duration)
                    .withEndAction {
                        if (isInside) {
                            onClick(v)
                        }
                    }
                    .start()
            }
            MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(originalScale)
                    .scaleY(originalScale)
                    .setDuration(duration)
                    .start()
            }
        }
        true
    }
}

object ScaleClickBindingAdapters {
    @JvmStatic
    @BindingAdapter("onScaleClick")
    fun onScaleClick(view: View, click: View.OnClickListener?) {
        click ?: return
        view.setOnScaleClickListener { click.onClick(it) }
    }
}
