package com.bbv.base.util.ext

import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Keep


/***
 * 设置延迟时间的View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @return T
 */
fun <T : View> T.withTrigger(delay: Long = 600): T {
    triggerDelay = delay
    return this
}

/***
 * 点击事件的View扩展
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener {

    if (clickEnable()) {
        block(it as T)
    }
}

/***
 * 带延迟过滤的点击事件View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
@Keep
fun <T : View> T.clickWithTrigger(time: Long = 600, block: (T) -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (clickEnable()) {
            block(it as T)
        }
    }
}

private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(1123460103) != null) getTag(1123460103) as Long else 0
    set(value) {
        setTag(1123460103, value)
    }

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(1123461123) != null) getTag(1123461123) as Long else -1
    set(value) {
        setTag(1123461123, value)
    }

private fun <T : View> T.clickEnable(): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        flag = true
    }
    triggerLastTime = currentClickTime
    return flag
}


fun <T : TextView> T.isEmpty(): Boolean {
    return TextUtils.isEmpty(text.toString().trim())
}

fun <T : TextView> T.isNotEmpty(): Boolean {
    return !isEmpty()
}

fun <T : TextView> T.getStr(): String {
    return text.toString().trim()
}

fun <T : TextView> T.getStrOrNull(): String? {
    val toString = text.toString()
    if (toString.isEmpty()) {
        return null
    } else {
        return toString.trim()
    }
}

fun TextView.onEditorActionListener(actionIdMode: Int = EditorInfo.IME_ACTION_SEARCH, block: (String?) -> Unit) {
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == actionIdMode) {
            val str = v?.text?.toString()
            block(if (str.isNullOrEmpty()) null else str)
            true
        }
        false
    }
}

/**
 * 显示隐藏输入框文字
 * @param editText
 * 输入框
 * @param isShow
 * true：显示，false：隐藏
 */
fun EditText.showHidPass(isShow: Boolean) {
    if (isShow) {
        //如果选中，显示密码
        transformationMethod = HideReturnsTransformationMethod.getInstance()
        setSelection(text.length)
    } else {
        //否则隐藏密码
        transformationMethod = PasswordTransformationMethod.getInstance()
        setSelection(text.length)
    }
}
