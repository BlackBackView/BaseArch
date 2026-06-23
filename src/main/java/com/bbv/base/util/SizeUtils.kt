package com.bbv.base.util

import android.content.res.Resources

fun Float.dp2px(): Float {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f)
}

fun Int.dp2px(): Int {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

fun Float.px2dp(): Float {
    return (this / Resources.getSystem().displayMetrics.density + 0.5f)
}

fun Int.px2dp(): Int {
    return (this / Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

fun Float.sp2px(): Float {
    return (this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f)
}

fun Int.sp2px(): Int {
    return (this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f).toInt()
}

fun Float.px2sp(): Float {
    return (this / Resources.getSystem().displayMetrics.scaledDensity + 0.5f)
}

fun Int.px2sp(): Int {
    return (this / Resources.getSystem().displayMetrics.scaledDensity + 0.5f).toInt()
}
