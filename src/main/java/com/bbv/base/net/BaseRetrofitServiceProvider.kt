package com.bbv.base.net

import com.bbv.base.net.convert.CustomizeJsonAdapters
import com.bbv.base.net.convert.FancyNumbersAdapter
import com.bbv.base.net.interceptor.DynamicTimeoutInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Retrofit 服务提供者基类
 *
 * 使用示例：
 * ```kotlin
 * // 1. 定义 API 接口
 * interface UserApi {
 *     @GET("user/info")
 *     suspend fun getUserInfo(): User
 * }
 *
 * // 2. 创建 ServiceProvider 子类
 * class UserServiceProvider : BaseRetrofitServiceProvider() {
 *     val api: UserApi by lazy {
 *         genRetrofitClient("https://api.example.com/")
 *             .create(UserApi::class.java)
 *     }
 * }
 *
 * // 3. 使用
 * val user = UserServiceProvider().api.getUserInfo()
 * ```
 *
 * 如果不需要自动解析 CommonResponse 包装，可在 API 方法的参数或返回值上添加 {@link com.bbv.base.net.convert.WithoutStepParse} 注解。
 */
open class BaseRetrofitServiceProvider {

    companion object {
        /** 全局 Moshi 实例（包含自定义适配器） */
        val moshi: Moshi = Moshi.Builder()
            .add(FancyNumbersAdapter.FACTORY)
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(CustomizeJsonAdapters())
            .build()

        const val CONNECT_TIMEOUT = 10000L
        const val READ_TIMEOUT = 10000L
        const val WRITE_TIMEOUT = 10000L
    }

    /**
     * 生成 Retrofit 客户端
     */
    protected fun genRetrofitClient(
        baseUrl: String,
        connectTimeout: Long = CONNECT_TIMEOUT,
        readTimeout: Long = READ_TIMEOUT,
        writeTimeout: Long = WRITE_TIMEOUT
    ): Retrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .callFactory(genOkHttpClient(connectTimeout, readTimeout, writeTimeout))
            .apply {
                addCallAdapterFactories(this)
            }
            .build()
        return retrofit
    }

    /**
     * 生成 OkHttpClient
     */
    protected fun genOkHttpClient(
        connectTimeout: Long,
        readTimeout: Long,
        writeTimeout: Long
    ) = OkHttpClient.Builder()
        .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
        .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
        .apply {
            addInterceptors(this)
        }
        .build()

    /**
     * 添加 Retrofit CallAdapter（子类可覆写以自定义）
     */
    open fun addCallAdapterFactories(builder: Retrofit.Builder) {
        builder.addConverterFactory(MoshiConverterFactory.create(moshi))
    }

    /**
     * 添加 OkHttp 拦截器（子类可覆写以自定义）
     */
    open fun addInterceptors(builder: OkHttpClient.Builder) {
        // 动态超时
        builder.addInterceptor(DynamicTimeoutInterceptor())
        // 日志（RELEASE 模式自动关闭）
        builder.addInterceptor(HttpLoggingInterceptor().apply {
            level = if (com.bbv.base.util.Lg.isDebug)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        })
    }
}
