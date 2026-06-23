package com.bbv.base.util.ext

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import java.io.File


/**
 * Description:
 * author: wangwu on 2018/10/10 10:22.
 */


fun Context.px2dp(dpValue: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, resources.displayMetrics).toInt()
}

fun Context.dp2px(spValue: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, resources.displayMetrics).toInt()
}

fun Context.scanFile(file: File?) {
    if (file == null) return

    val files = arrayOf(file.absolutePath)
    MediaScannerConnection.scanFile(this, files, null) { path, uri ->
        Log.d("SCANNER", "scanFile() called with: path = [$path], uri = [$uri]")
    }
}


/**
 * 调用拨号界面
 * @param phone 电话号码
 */
fun Context.call(phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}
