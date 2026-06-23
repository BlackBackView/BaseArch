package com.bbv.base.binding

import android.view.View

/**
 * @Description:防抖点击
 */
class ThrottleClickHandler(
    private val interval: Int? = DEFAULT_INTERVAL_TIME,
    private val callBack: View.OnClickListener
) : View.OnClickListener {
    companion object {
        /**
         * 默认的防抖时间
         */
        const val DEFAULT_INTERVAL_TIME: Int = 500
    }

    private var mLastClickTime: Long = 0L

    private fun isFastDoubleClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - mLastClickTime > (interval ?: DEFAULT_INTERVAL_TIME)) {
            mLastClickTime = currentTime
            return true
        }
        return false
    }

    override fun onClick(v: View) {
        if (isFastDoubleClick()) {
            callBack.onClick(v)
        }
    }
}
