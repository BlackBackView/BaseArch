package com.bbv.base.coroutine.cache

class LruCache(maxSize: Long? = null) : Cache {

    private var cacheSize = 1024 * 1024 * 5 // 5MB default

    private val lruCache = object : android.util.LruCache<Any, Any>((maxSize ?: cacheSize).toInt()) {
        override fun sizeOf(key: Any, value: Any): Int = value.toString().length
    }

    override var size: Int
        get() = lruCache.maxSize()
        set(value) { /* no-op for LruCache */ }

    override fun get(key: Any): Any? = lruCache.get(key)

    override fun set(key: Any, value: Any) {
        lruCache.put(key, value)
    }

    override fun remove(key: Any): Any = lruCache.remove(key) ?: Unit

    override fun clear() {
        lruCache.evictAll()
    }
}
