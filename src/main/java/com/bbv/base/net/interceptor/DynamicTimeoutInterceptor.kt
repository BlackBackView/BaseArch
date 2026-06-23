package com.bbv.base.net.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 动态超时拦截器
 *
 * 支持通过请求头动态设置单个接口的超时时间：
 * - DYNAMIC-CONNECT-TIMEOUT: 连接超时（秒）
 * - DYNAMIC-READ-TIMEOUT: 读取超时（秒）
 * - DYNAMIC-WRITE-TIMEOUT: 写入超时（秒）
 *
 * 使用示例：
 * ```kotlin
 * val request = Request.Builder()
 *     .url(url)
 *     .header(DynamicTimeoutInterceptor.DYNAMIC_READ_TIMEOUT, "30")
 *     .build()
 * ```
 */
class DynamicTimeoutInterceptor : Interceptor {

    companion object {
        const val DYNAMIC_CONNECT_TIMEOUT = "DYNAMIC-CONNECT-TIMEOUT"
        const val DYNAMIC_READ_TIMEOUT = "DYNAMIC-READ-TIMEOUT"
        const val DYNAMIC_WRITE_TIMEOUT = "DYNAMIC-WRITE-TIMEOUT"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val headers = request.headers
        var newChain = chain

        headers[DYNAMIC_CONNECT_TIMEOUT]?.let {
            newChain = newChain.withConnectTimeout(it.toInt(), TimeUnit.SECONDS)
        }
        headers[DYNAMIC_READ_TIMEOUT]?.let {
            newChain = newChain.withReadTimeout(it.toInt(), TimeUnit.SECONDS)
        }
        headers[DYNAMIC_WRITE_TIMEOUT]?.let {
            newChain = newChain.withWriteTimeout(it.toInt(), TimeUnit.SECONDS)
        }

        return newChain.proceed(request)
    }
}
