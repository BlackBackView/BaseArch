package com.bbv.base.util

import android.util.Log
import android.webkit.MimeTypeMap
import java.util.Locale

/**
 * @Description:
 * @Author: yang liv
 * @Date: 2025/5/10 18:50
 */


/**
 * 隐藏号码中间四位
 */
fun mobileEncrypt(mobile: String, isHidden: Boolean = true): String? {
    val PHONE_BLUR_REGEX = "(\\d{3})\\d{4}(\\d{4})"
    /** * 手机号脱敏替换正则  */
    val PHONE_BLUR_REPLACE_REGEX = "$1****$2"
    /** * 手机号格式校验 * @param phone * @return  */
    return if (mobile.isEmpty() || mobile.length != 11 || !isHidden) {
        mobile
    } else mobile.replace(PHONE_BLUR_REGEX.toRegex(), PHONE_BLUR_REPLACE_REGEX)
}


/**
 * @param url
 * @return
 * 从下载连接中解析出文件名
 */
fun getNameFromUrl(url: String): String {
    return url.substring(url.lastIndexOf("/") + 1)
}

/**
 * @param separator
 * @return
 * 取末尾分隔符后面的字段
 */
fun getValueByLastedSep(data: String, separator: String): String {
    return data.substring(data.lastIndexOf(separator) + 1)
}


fun byte2FitMemorySize(byteNum: Long): String? {
    return if (byteNum < 0) {
        ""
    } else if (byteNum < 1024) {
        String.format(Locale.getDefault(), "%.1fB", byteNum.toDouble())
    } else if (byteNum < 1048576) {
        String.format(Locale.getDefault(), "%.1fKB", byteNum.toDouble() / 1024)
    } else if (byteNum < 1073741824) {
        String.format(Locale.getDefault(), "%.1fMB", byteNum.toDouble() / 1048576)
    } else {
        String.format(Locale.getDefault(), "%.1fGB", byteNum.toDouble() / 1073741824)
    }
}


/**
 * 从 Url查找扩展名
 * */
fun findExtensionFromUri(url: String, TAG: String = "Ext"): String? {
    var end = -1
    for (i in 5 until url.length) {
        val c = url[i].toString()
        if (c.equals(";", ignoreCase = true)) {
            end = i
        } else if (c.equals(",", ignoreCase = true)) {
            break
        }
    }
    if (end > 5) {
        val mimeType = url.substring(5, end)
        Log.i(TAG, "mimeType:$mimeType")
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }
    return ""
}
