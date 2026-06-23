package com.bbv.base.coroutine.cache

import java.lang.reflect.Type

sealed class RequestCacheStrategy {
    abstract suspend fun <T> execute(
        key: String,
        type: Type,
        netBlock: suspend () -> T?,
        cacheBlock: suspend (T?) -> Unit,
        successBlock: suspend (T?) -> Unit
    )

    /** 只读缓存，不请求网络 */
    object RequestCacheOnly : RequestCacheStrategy() {
        override suspend fun <T> execute(
            key: String, type: Type,
            netBlock: suspend () -> T?,
            cacheBlock: suspend (T?) -> Unit,
            successBlock: suspend (T?) -> Unit
        ) {
            val cache = RequestCacheManager.get<T>(key, type)
            cache?.let { cacheBlock(it) }
        }
    }

    /** 请求网络 → 成功回调 → 保存缓存 */
    object RequestNetAndSaveCache : RequestCacheStrategy() {
        override suspend fun <T> execute(
            key: String, type: Type,
            netBlock: suspend () -> T?,
            cacheBlock: suspend (T?) -> Unit,
            successBlock: suspend (T?) -> Unit
        ) {
            val value = netBlock()
            RequestCacheManager.save(key, value, type)
            successBlock(value)
        }
    }

    /** 请求网络 → 失败则读缓存 */
    object RequestNetAndIfFailCache : RequestCacheStrategy() {
        override suspend fun <T> execute(
            key: String, type: Type,
            netBlock: suspend () -> T?,
            cacheBlock: suspend (T?) -> Unit,
            successBlock: suspend (T?) -> Unit
        ) {
            try {
                val value = netBlock()
                RequestCacheManager.save(key, value, type)
                successBlock(value)
            } catch (e: Exception) {
                val cache = RequestCacheManager.get<T>(key, type)
                cache?.let { cacheBlock(it) }
            }
        }
    }

    /** 先读缓存 → 请求网络并保存缓存 */
    object RequestCacheAndSaveCache : RequestCacheStrategy() {
        override suspend fun <T> execute(
            key: String, type: Type,
            netBlock: suspend () -> T?,
            cacheBlock: suspend (T?) -> Unit,
            successBlock: suspend (T?) -> Unit
        ) {
            val cache = RequestCacheManager.get<T>(key, type)
            if (cache != null) {
                cacheBlock(cache)
            }
            val value = netBlock()
            RequestCacheManager.save(key, value, type)
            successBlock(value)
        }
    }

    /** 有缓存走缓存，无缓存走网络 */
    object RequestCacheOrNet : RequestCacheStrategy() {
        override suspend fun <T> execute(
            key: String, type: Type,
            netBlock: suspend () -> T?,
            cacheBlock: suspend (T?) -> Unit,
            successBlock: suspend (T?) -> Unit
        ) {
            val cache = RequestCacheManager.get<T>(key, type)
            if (cache != null) {
                cacheBlock(cache)
            } else {
                val value = netBlock()
                successBlock(value)
            }
        }
    }

    /** 先走缓存回调，再走网络回调（刷新） */
    object RequestCacheAndNet : RequestCacheStrategy() {
        override suspend fun <T> execute(
            key: String, type: Type,
            netBlock: suspend () -> T?,
            cacheBlock: suspend (T?) -> Unit,
            successBlock: suspend (T?) -> Unit
        ) {
            val cache = RequestCacheManager.get<T>(key, type)
            if (cache != null) {
                cacheBlock(cache)
            }
            val value = netBlock()
            RequestCacheManager.save(key, value, type)
            successBlock(value)
        }
    }
}
