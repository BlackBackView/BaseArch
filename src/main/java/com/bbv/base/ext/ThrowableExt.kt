package com.bbv.base.ext

import java.io.IOException

fun Throwable.toShowMsg(): String {
    return when (this) {
        is IOException -> "网络连接失败，请检查网络设置"
        else -> this.localizedMessage ?: "未知错误"
    }
}
