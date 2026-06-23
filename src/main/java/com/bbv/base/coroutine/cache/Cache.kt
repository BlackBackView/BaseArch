package com.bbv.base.coroutine.cache

interface Cache {
    var size: Int
    operator fun get(key: Any): Any?
    operator fun set(key: Any, value: Any)
    fun remove(key: Any): Any
    fun clear()
}
