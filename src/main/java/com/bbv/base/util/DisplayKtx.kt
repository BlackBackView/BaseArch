package com.bbv.base.util

import android.content.res.Resources

/**
 * @Description:
 * @Author: 
 * @Date: 2025/1/8 23:59
 */

val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.dp: Float get() = this * Resources.getSystem().displayMetrics.density
val Double.dp: Double get() = this * Resources.getSystem().displayMetrics.density
