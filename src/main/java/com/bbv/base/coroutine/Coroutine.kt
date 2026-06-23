package com.bbv.base.coroutine

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bbv.base.coroutine.cache.RequestCacheStrategy
import com.bbv.base.mvvm.PageState
import com.bbv.base.mvvm.PageStateType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.lang.reflect.Type
import kotlin.coroutines.CoroutineContext

/**
 * 链式协程封装
 *
 * 使用方式：
 * ```
 * execute {
 *     repository.fetchData()
 * }.onStart {
 *     // 开始
 * }.onSuccess { data ->
 *     // 成功
 * }.onError { e ->
 *     // 错误
 * }.onFinally {
 *     // 最终执行
 * }
 * ```
 */
class Coroutine<T>(
    private val scope: CoroutineScope,
    context: CoroutineContext = Dispatchers.IO,
    val pageState: MutableLiveData<PageState>?,
    private val block: suspend CoroutineScope.() -> T
) {

    companion object {
        val DEFAULT = MainScope()

        fun <T> async(
            scope: CoroutineScope = DEFAULT,
            context: CoroutineContext = Dispatchers.IO,
            state: MutableLiveData<PageState>? = null,
            block: suspend CoroutineScope.() -> T
        ): Coroutine<T> {
            return Coroutine(scope, context, state, block)
        }
    }

    private val job: Job

    private var start: VoidCallback? = null
    private var success: Callback<T?>? = null
    private var cache: CacheCallback<T?>? = null
    private var error: Callback<Throwable>? = null
    private var globalError: BooleanCallback<Throwable>? = null
    private var httpError: BooleanCallback<Throwable>? = null
    private var finally: VoidCallback? = null

    private var timeMillis: Long? = null
    private var errorReturn: Result<T>? = null

    val isCancelled: Boolean get() = job.isCancelled
    val isActive: Boolean get() = job.isActive
    val isCompleted: Boolean get() = job.isCompleted

    init {
        this.job = executeInternal(scope, context, block)
    }

    // ============ 链式方法 ============

    fun timeout(timeMillis: () -> Long): Coroutine<T> {
        this.timeMillis = timeMillis()
        return this@Coroutine
    }

    fun timeout(timeMillis: Long): Coroutine<T> {
        this.timeMillis = timeMillis
        return this@Coroutine
    }

    fun onErrorReturn(value: () -> T?): Coroutine<T> {
        this.errorReturn = Result(value())
        return this@Coroutine
    }

    fun onErrorReturn(value: T?): Coroutine<T> {
        this.errorReturn = Result(value)
        return this@Coroutine
    }

    fun onStart(
        context: CoroutineContext? = null,
        block: (suspend CoroutineScope.() -> Unit)
    ): Coroutine<T> {
        this.start = VoidCallback(context, block)
        return this@Coroutine
    }

    fun onSuccess(
        context: CoroutineContext? = null,
        block: suspend CoroutineScope.(T?) -> Unit
    ): Coroutine<T> {
        this.success = Callback(context, block)
        return this@Coroutine
    }

    inline fun <reified V> onCache(
        requestCacheStrategy: RequestCacheStrategy,
        key: String
    ): Coroutine<T> {
        return onCache(requestCacheStrategy, object : TypeToken<V>() {}.type, key)
    }

    fun onCache(
        requestCacheStrategy: RequestCacheStrategy,
        type: Type,
        key: String,
        context: CoroutineContext? = null,
        block: (suspend CoroutineScope.(T?) -> Unit)? = null
    ): Coroutine<T> {
        val finalBlock = block ?: this.success?.block
            ?: throw RuntimeException("must set onCache block or set onSuccess first")
        this.cache = CacheCallback(requestCacheStrategy, type, key, context, finalBlock)
        return this@Coroutine
    }

    fun onError(
        context: CoroutineContext? = null,
        block: suspend CoroutineScope.(Throwable) -> Unit
    ): Coroutine<T> {
        this.error = Callback(context, block)
        return this@Coroutine
    }

    fun onGlobalError(
        context: CoroutineContext? = null,
        block: suspend CoroutineScope.(Throwable) -> Boolean
    ): Coroutine<T> {
        this.globalError = BooleanCallback(context, block)
        return this@Coroutine
    }

    fun onHttpError(
        context: CoroutineContext? = null,
        block: suspend CoroutineScope.(Throwable) -> Boolean
    ): Coroutine<T> {
        this.httpError = BooleanCallback(context, block)
        return this@Coroutine
    }

    fun onFinally(
        context: CoroutineContext? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Coroutine<T> {
        this.finally = VoidCallback(context, block)
        return this@Coroutine
    }

    fun cancel(cause: CancellationException? = null) {
        job.cancel(cause)
    }

    fun invokeOnCompletion(handler: CompletionHandler): DisposableHandle {
        return job.invokeOnCompletion(handler)
    }

    suspend fun executeRetry() {
        try {
            val value = if (timeMillis ?: 0L > 0L) withTimeout(timeMillis!!) {
                block.invoke(scope.plus(Dispatchers.IO))
            } else block.invoke(scope.plus(Dispatchers.IO))
            success?.let {
                dispatchCallback(scope.plus(Dispatchers.Main), value, it)
            }
        } catch (e: Exception) {
            Log.e("Coroutine", "executeRetry fail", e)
        }
    }

    // ============ 内部执行逻辑 ============

    private fun executeInternal(
        scope: CoroutineScope,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> T
    ): Job {
        return scope.plus(Dispatchers.Main).launch {
            try {
                // onStart 回调
                start?.let { dispatchVoidCallback(this, it) }

                // Loading 状态
                pageState?.apply {
                    value = PageState(pageStateType = PageStateType.LOADING)
                }

                // 执行核心逻辑（带缓存或不带缓存）
                cache?.let {
                    it.requestCacheStrategy.execute(it.key, it.type,
                        suspend { executeBlock(scope, context, timeMillis ?: 0L, block) },
                        { value: T? ->
                            withContext(Dispatchers.Main) {
                                dispatchCallback(this, value, it)
                            }
                        },
                        { value: T? ->
                            withContext(Dispatchers.Main) {
                                success?.let { dispatchCallback(this, value, it) }
                            }
                        }
                    )
                } ?: let {
                    val value = executeBlock(scope, context, timeMillis ?: 0L, block)
                    success?.let { dispatchCallback(this, value, it) }
                }

                // Normal 状态
                pageState?.apply {
                    this.value = PageState(pageStateType = PageStateType.NORMAL)
                }
            } catch (e: Throwable) {
                var consume = errorReturn?.value?.let { value ->
                    success?.let { dispatchCallback(this, value, it) }
                    true
                } ?: false

                // HTTP 错误处理
                if (!consume) {
                    try {
                        httpError?.let {
                            consume = dispatchBooleanCallback(this, e, it)
                        }
                    } catch (ex: Exception) {
                        Log.d("Coroutine", "httpError callback failed: ${ex.message}")
                    }
                }

                // 全局错误处理
                if (!consume) {
                    try {
                        globalError?.let {
                            consume = dispatchBooleanCallback(this, e, it)
                        }
                    } catch (ex: Exception) {
                        Log.d("Coroutine", "globalError callback failed: ${ex.message}")
                    }
                }

                // 普通错误回调
                if (!consume) {
                    error?.let { dispatchCallback(this, e, it) }
                    pageState?.apply {
                        value = PageState(pageStateType = PageStateType.ERROR, throwable = e)
                    }
                } else {
                    pageState?.apply {
                        value = PageState(pageStateType = PageStateType.NORMAL)
                    }
                }
            } finally {
                finally?.let { dispatchVoidCallback(this, it) }
            }
        }
    }

    private suspend inline fun dispatchVoidCallback(scope: CoroutineScope, callback: VoidCallback) {
        if (null == callback.context) {
            callback.block.invoke(scope)
        } else {
            withContext(scope.coroutineContext.plus(callback.context)) {
                callback.block.invoke(this)
            }
        }
    }

    private suspend inline fun <R> dispatchCallback(
        scope: CoroutineScope,
        value: R,
        callback: Callback<R>
    ) {
        if (null == callback.context) {
            callback.block.invoke(scope, value)
        } else {
            withContext(scope.coroutineContext.plus(callback.context)) {
                callback.block.invoke(this, value)
            }
        }
    }

    private suspend inline fun <R> dispatchBooleanCallback(
        scope: CoroutineScope,
        value: R,
        callback: BooleanCallback<R>
    ): Boolean {
        return if (null == callback.context) {
            callback.block.invoke(scope, value)
        } else {
            runBlocking(scope.coroutineContext.plus(callback.context)) {
                callback.block.invoke(this, value)
            }
        }
    }

    private suspend inline fun executeBlock(
        scope: CoroutineScope,
        context: CoroutineContext,
        timeMillis: Long,
        noinline block: suspend CoroutineScope.() -> T
    ): T? {
        return withContext(scope.coroutineContext.plus(context)) {
            if (timeMillis > 0L) withTimeout(timeMillis) { block() }
            else block()
        }
    }

    // ============ 内部类 ============

    private data class Result<out T>(val value: T?)

    private open inner class Callback<VALUE>(
        val context: CoroutineContext?,
        val block: suspend CoroutineScope.(VALUE) -> Unit
    )

    private inner class VoidCallback(
        val context: CoroutineContext?,
        val block: suspend CoroutineScope.() -> Unit
    )

    private open inner class BooleanCallback<VALUE>(
        val context: CoroutineContext?,
        val block: suspend CoroutineScope.(VALUE) -> Boolean
    )

    private inner class CacheCallback<T>(
        val requestCacheStrategy: RequestCacheStrategy,
        val type: Type,
        val key: String,
        context: CoroutineContext?,
        block: suspend CoroutineScope.(T) -> Unit
    ) : Callback<T>(context, block)
}

/** 用于获取泛型类型的辅助类 */
abstract class TypeToken<T> {
    val type: Type = (javaClass.genericSuperclass as java.lang.reflect.ParameterizedType)
        .actualTypeArguments[0]
}
