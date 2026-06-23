package com.bbv.base.coroutine.cache

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type

object RequestCacheManager {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val memoryCache: MemoryCache by lazy { MemoryCache() }

    fun cacheConfig(memoryCacheMaxSize: Int? = null) {
        // 可扩展配置
    }

    fun <T> save(key: String, value: T?, type: Type) {
        if (value == null) return
        try {
            val json = moshi.adapter<T>(type).toJson(value)
            memoryCache[key] = json
        } catch (_: Exception) {
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, type: Type): T? {
        return try {
            val json = memoryCache[key] as? String ?: return null
            moshi.adapter<T>(type).fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    inline fun <reified T> get(key: String): T? {
        val type = Types.newParameterizedType(T::class.java)
        return get(key, type)
    }
}
