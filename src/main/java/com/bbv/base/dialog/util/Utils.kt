package com.bbv.base.dialog.util

import android.content.res.Resources
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewOutlineProvider

internal fun View.radius(
    color: Int? = null,
    radius: Float
) {
    this.background = GradientDrawable().apply {
        cornerRadius = radius.dp2px().toFloat()
        color?.let { this.setColor(it) }
    }
}

internal fun View.radius(
    color: Int? = null,
    tl: Float = 0f,
    tr: Float = 0f,
    bl: Float = 0f,
    br: Float = 0f,
) {
    this.background = GradientDrawable().apply {
        cornerRadii = floatArrayOf(
            tl.dp2px().toFloat(), tl.dp2px().toFloat(),
            tr.dp2px().toFloat(), tr.dp2px().toFloat(),
            bl.dp2px().toFloat(), bl.dp2px().toFloat(),
            br.dp2px().toFloat(), br.dp2px().toFloat()
        )
        color?.let { this.setColor(it) }
    }
}

internal fun View.radius(radius: Float) {
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius.dp2px().toFloat())
        }
    }
    clipToOutline = true
}

internal fun Float.dp2px(): Int {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

internal fun Int.dp2px(): Int {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}
