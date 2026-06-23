package com.bbv.base.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OkHttp 网络请求工具类
 *
 * ========== 基础使用 ==========
 *
 * 1. 设置调试模式（建议在 Application.onCreate 中配置）
 * ```kotlin
 * NetworkRequestUtil.debugMode = true // 启用日志
 * ```
 *
 * 2. 异步 GET（原始 Response）
 * ```kotlin
 * NetworkRequestUtil.getAsync("https://api.example.com/data",
 *     params = mapOf("page" to "1"),
 *     headers = mapOf("Authorization" to "Bearer xxx"),
 *     callback = object : Callback {
 *         override fun onFailure(call: Call, e: IOException) { }
 *         override fun onResponse(call: Call, response: Response) { }
 *     })
 * ```
 *
 * 3. 异步 GET + 自动 Gson 解析
 * ```kotlin
 * NetworkRequestUtil.getAsyncWithResult<User>(
 *     url = "https://api.example.com/user/1",
 *     onSuccess = { user -> /* user: User */ },
 *     onError = { e -> /* IOException */ }
 * )
 * ```
 *
 * 4. 异步 POST JSON
 * ```kotlin
 * NetworkRequestUtil.postJsonAsync("https://api.example.com/login",
 *     jsonBody = """{"name":"test","pwd":"123"}""",
 *     callback = ...)
 * ```
 *
 * 5. 同步 GET + 自动 Gson 解析 + 自定义异常处理
 * ```kotlin
 * try {
 *     val response = NetworkRequestUtil.getSync("https://api.example.com/data")
 *     val user: User = NetworkRequestUtil.handleResponse(response)
 * } catch (e: NetworkRequestUtil.UnauthorizedException) {
 *     // 401
 * } catch (e: NetworkRequestUtil.ServerException) {
 *     // 5xx
 * }
 * ```
 *
 * 6. 上传文件
 * ```kotlin
 * NetworkRequestUtil.uploadFileAsync("https://api.example.com/upload",
 *     paramName = "file",
 *     file = File("/path/to/image.jpg"),
 *     formParams = mapOf("type" to "avatar"),
 *     callback = ...)
 * ```
 *
 * ========== 可用方法一览 ==========
 *
 * ┌──────────────────────┬──────────┬──────────────┬──────────────────┐
 * │ 方法                 │ 请求类型  │ 同步/异步     │ 自动解析         │
 * ├──────────────────────┼──────────┼──────────────┼──────────────────┤
 * │ getAsync             │ GET      │ 异步          │ ❌ 返回 Response │
 * │ getAsyncWithResult   │ GET      │ 异步          │ ✅ Gson → T     │
 * │ getSync              │ GET      │ 同步          │ ❌ 返回 Response │
 * │ getSyncWithResult    │ GET      │ 同步          │ ✅ Gson → T     │
 * │ postJsonAsync        │ POST JSON│ 异步          │ ❌ 返回 Response │
 * │ postJsonAsyncWithResult│ POST  │ 异步          │ ✅ Gson → T     │
 * │ postJsonSync         │ POST JSON│ 同步          │ ❌ 返回 Response │
 * │ postJsonSyncWithResult│ POST   │ 同步          │ ✅ Gson → T     │
 * │ postFormAsync        │ POST FORM│ 异步          │ ❌ 返回 Response │
 * │ postFormAsyncWithResult│ POST  │ 异步          │ ✅ Gson → T     │
 * │ uploadFileAsync      │ POST FILE│ 异步          │ ❌ 返回 Response │
 * │ uploadFileAsyncWithResult│ POST│ 异步          │ ✅ Gson → T     │
 * └──────────────────────┴──────────┴──────────────┴──────────────────┘
 */
object NetworkRequestUtil {

    /** 调试模式开关 — 启用后打印请求/响应日志 */
    var debugMode: Boolean = false

    /** 日志拦截器 */
    private val loggingInterceptor = object : okhttp3.Interceptor {
        override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            android.util.Log.d(TAG, "→ ${request.method} ${request.url}")
            val response = chain.proceed(request)
            android.util.Log.d(TAG, "← ${response.code} ${response.message}")
            return response
        }
    }

    /** 全局 OkHttpClient（单例） */
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .apply { if (debugMode) addInterceptor(loggingInterceptor) }
        .build()

    // ======================== GET ========================

    /** 异步 GET */
    fun getAsync(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        callback: Callback
    ) {
        val request = buildGetRequest(url, params, headers)
        client.newCall(request).enqueue(callback)
    }

    /** 异步 GET + 自动 Gson 解析 */
    inline fun <reified T> getAsyncWithResult(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        crossinline onSuccess: (T) -> Unit,
        crossinline onError: (IOException) -> Unit = {}
    ) {
        getAsync(url, params, headers, object : Callback {
            override fun onFailure(call: Call, e: IOException) { onError(e) }

            override fun onResponse(call: Call, response: Response) {
                parseResponseBody(response, onSuccess, onError)
            }
        })
    }

    /** 同步 GET */
    @Throws(IOException::class)
    fun getSync(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null
    ): Response {
        val request = buildGetRequest(url, params, headers)
        return client.newCall(request).execute()
    }

    /** 同步 GET + 自动 Gson 解析 */
    inline fun <reified T> getSyncWithResult(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        crossinline onSuccess: (T) -> Unit,
        crossinline onError: (IOException) -> Unit = {}
    ) {
        try {
            val response = getSync(url, params, headers)
            parseResponseBody(response, onSuccess, onError)
        } catch (e: IOException) {
            onError(e)
        }
    }

    // ======================== POST JSON ========================

    /** 异步 POST JSON */
    fun postJsonAsync(
        url: String,
        jsonBody: String,
        headers: Map<String, String>? = null,
        callback: Callback
    ) {
        val request = buildPostRequest(url, jsonBody.toRequestBody(JSON_MEDIA_TYPE), headers)
        client.newCall(request).enqueue(callback)
    }

    /** 异步 POST JSON + 自动 Gson 解析 */
    inline fun <reified T> postJsonAsyncWithResult(
        url: String,
        jsonBody: String,
        headers: Map<String, String>? = null,
        crossinline onSuccess: (T) -> Unit,
        crossinline onError: (IOException) -> Unit = {}
    ) {
        postJsonAsync(url, jsonBody, headers, object : Callback {
            override fun onFailure(call: Call, e: IOException) { onError(e) }

            override fun onResponse(call: Call, response: Response) {
                parseResponseBody(response, onSuccess, onError)
            }
        })
    }

    /** 同步 POST JSON */
    @Throws(IOException::class)
    fun postJsonSync(
        url: String,
        jsonBody: String,
        headers: Map<String, String>? = null
    ): Response {
        val request = buildPostRequest(url, jsonBody.toRequestBody(JSON_MEDIA_TYPE), headers)
        return client.newCall(request).execute()
    }

    /** 同步 POST JSON + 自动 Gson 解析 */
    inline fun <reified T> postJsonSyncWithResult(
        url: String,
        jsonBody: String,
        headers: Map<String, String>? = null,
        crossinline onSuccess: (T) -> Unit,
        crossinline onError: (IOException) -> Unit = {}
    ) {
        try {
            val response = postJsonSync(url, jsonBody, headers)
            parseResponseBody(response, onSuccess, onError)
        } catch (e: IOException) {
            onError(e)
        }
    }

    // ======================== POST FORM ========================

    /** 异步 POST 表单 */
    fun postFormAsync(
        url: String,
        formParams: Map<String, String>,
        headers: Map<String, String>? = null,
        callback: Callback
    ) {
        val formBody = buildFormBody(formParams)
        val request = buildPostRequest(url, formBody, headers)
        client.newCall(request).enqueue(callback)
    }

    /** 异步 POST 表单 + 自动 Gson 解析 */
    inline fun <reified T> postFormAsyncWithResult(
        url: String,
        formParams: Map<String, String>,
        headers: Map<String, String>? = null,
        crossinline onSuccess: (T) -> Unit,
        crossinline onError: (IOException) -> Unit = {}
    ) {
        postFormAsync(url, formParams, headers, object : Callback {
            override fun onFailure(call: Call, e: IOException) { onError(e) }

            override fun onResponse(call: Call, response: Response) {
                parseResponseBody(response, onSuccess, onError)
            }
        })
    }

    /** 同步 POST 表单 */
    @Throws(IOException::class)
    fun postFormSync(
        url: String,
        formParams: Map<String, String>,
        headers: Map<String, String>? = null
    ): Response {
        val formBody = buildFormBody(formParams)
        val request = buildPostRequest(url, formBody, headers)
        return client.newCall(request).execute()
    }

    // ======================== 上传文件 ========================

    /** 异步上传文件 */
    fun uploadFileAsync(
        url: String,
        paramName: String,
        file: File,
        formParams: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        callback: Callback
    ) {
        val requestBody = buildMultipartBody(paramName, file, formParams)
        val request = buildPostRequest(url, requestBody, headers)
        client.newCall(request).enqueue(callback)
    }

    /** 异步上传文件 + 自动 Gson 解析 */
    inline fun <reified T> uploadFileAsyncWithResult(
        url: String,
        paramName: String,
        file: File,
        formParams: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        crossinline onSuccess: (T) -> Unit,
        crossinline onError: (IOException) -> Unit = {}
    ) {
        uploadFileAsync(url, paramName, file, formParams, headers, object : Callback {
            override fun onFailure(call: Call, e: IOException) { onError(e) }

            override fun onResponse(call: Call, response: Response) {
                parseResponseBody(response, onSuccess, onError)
            }
        })
    }

    // ======================== 通用方法 ========================

    /**
     * 同步解析 Response（配合 [handleResponse] 使用）
     *
     * 示例：
     * ```kotlin
     * val response = NetworkRequestUtil.getSync(url)
     * val user: User = NetworkRequestUtil.handleResponse(response)
     * ```
     *
     * @throws UnauthorizedException 401
     * @throws ForbiddenException 403
     * @throws NotFoundException 404
     * @throws ServerException 5xx
     * @throws IOException 其他网络错误
     */
    inline fun <reified T> handleResponse(response: Response): T {
        if (!response.isSuccessful) {
            throw when (response.code) {
                401 -> UnauthorizedException("Unauthorized")
                403 -> ForbiddenException("Forbidden")
                404 -> NotFoundException("Not Found")
                in 500..599 -> ServerException("Server Error ${response.code}")
                else -> IOException("Request failed with code ${response.code}")
            }
        }
        val responseBody = response.body?.string() ?: ""
        return Gson().fromJson(responseBody, object : TypeToken<T>() {}.type)
    }

    // ======================== 请求构建 ========================

    private fun buildGetRequest(
        url: String,
        params: Map<String, String>?,
        headers: Map<String, String>?
    ): Request {
        val requestBuilder = Request.Builder()
        if (params != null) {
            val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
            for ((key, value) in params) urlBuilder.addQueryParameter(key, value)
            requestBuilder.url(urlBuilder.build())
        } else {
            requestBuilder.url(url)
        }
        headers?.forEach { (key, value) -> requestBuilder.addHeader(key, value) }
        return requestBuilder.build()
    }

    private fun buildPostRequest(
        url: String,
        requestBody: RequestBody,
        headers: Map<String, String>?
    ): Request {
        val requestBuilder = Request.Builder().url(url).post(requestBody)
        headers?.forEach { (key, value) -> requestBuilder.addHeader(key, value) }
        return requestBuilder.build()
    }

    private fun buildFormBody(formParams: Map<String, String>): RequestBody {
        val formBodyBuilder = FormBody.Builder()
        formParams.forEach { (key, value) -> formBodyBuilder.add(key, value) }
        return formBodyBuilder.build()
    }

    private fun buildMultipartBody(
        fileParamName: String,
        file: File,
        formParams: Map<String, String>?
    ): RequestBody {
        val multipartBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(fileParamName, file.name, file.asRequestBody(FILE_MEDIA_TYPE))

        formParams?.forEach { (key, value) ->
            multipartBodyBuilder.addFormDataPart(key, value)
        }
        return multipartBodyBuilder.build()
    }

    // ======================== 内部工具 ========================

    /** 统一处理 Response 体（Gson 解析 + 回调） */
    inline fun <reified T> parseResponseBody(
        response: Response,
        onSuccess: (T) -> Unit,
        onError: (IOException) -> Unit
    ) {
        try {
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val result: T = Gson().fromJson(responseBody, object : TypeToken<T>() {}.type)
                onSuccess(result)
            } else {
                onError(IOException("Request failed with code ${response.code}"))
            }
        } catch (e: IOException) {
            onError(e)
        }
    }

    // ======================== 自定义异常 ========================

    class UnauthorizedException(message: String) : IOException(message)
    class ForbiddenException(message: String) : IOException(message)
    class NotFoundException(message: String) : IOException(message)
    class ServerException(message: String) : IOException(message)

    // ======================== 常量 ========================

    private const val TAG = "NetworkRequestUtil"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val FILE_MEDIA_TYPE = "multipart/form-data".toMediaTypeOrNull()
}
