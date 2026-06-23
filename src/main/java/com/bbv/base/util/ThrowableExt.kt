package com.bbv.base.util

import com.bbv.base.net.exception.ErrorKind
import com.bbv.base.net.exception.RetrofitException
import retrofit2.HttpException
import java.io.IOException

/**
 * 将异常转换为用户可读的错误消息
 *
 * 使用示例：
 * ```kotlin
 * try {
 *     val result = api.someRequest()
 * } catch (e: Exception) {
 *     toast(e.toShowMsg())
 * }
 * ```
 */
fun Throwable.toShowMsg(): String {
    return when (this) {
        is RetrofitException -> {
            when (errorKind) {
                ErrorKind.HTTP -> "遇到错误"
                ErrorKind.SERVER -> commonResponse?.msg ?: "服务器未知错误"
                ErrorKind.DATA_PARSE -> "数据解析错误"
                ErrorKind.NETWORK -> "网络IO错误"
                ErrorKind.UNEXPECTED -> "未知错误"
            }
        }
        is HttpException -> "HTTP错误，${this.code()}"
        is IOException -> "网络IO错误"
        else -> "请检查网络设置"
    }
}
