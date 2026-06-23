package com.bbv.base.coroutine.cache

class MemoryCache(maxSize: Long? = null) : Cache by LruCache(maxSize)
