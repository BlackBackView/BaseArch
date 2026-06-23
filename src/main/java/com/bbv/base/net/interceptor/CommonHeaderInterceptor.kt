package com.bbv.base.net.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 通用请求头拦截器
 *
 * 用于自动注入全局请求头（如 token、设备信息等）。
 * 具体 header 可通过 [NetConfig.HEADERS][com.bbv.base.net.NetConfig.HEADERS] 配置。
 *
 * TODO: 在 buildRequest() 中实现实际的 header 注入逻辑
 */
class CommonHeaderInterceptor(private val netParams: Map<String, Any> = emptyMap()) :
    Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = processHeader(request)
        return chain.proceed(request)
    }

    @Throws(IOException::class)
    private fun processHeader(request: Request): Request {
        val headerBuilder = request.newBuilder()
        return try {
            buildRequest(headerBuilder)
        } catch (e: Exception) {
            throw IOException("make header fail --> ${e.message}")
        }
    }

    private fun buildRequest(builder: Request.Builder): Request {
        // TODO: 在这里添加通用 header
        // builder.addHeader("Authorization", "Bearer ${token}")
        // builder.addHeader("Device-Id", deviceId)
        return builder.build()
    }
}
