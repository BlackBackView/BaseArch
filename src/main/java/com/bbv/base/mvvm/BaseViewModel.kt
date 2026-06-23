package com.bbv.base.mvvm

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbv.base.coroutine.Coroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 基础 ViewModel
 *
 * 提供 [execute] / [executeWithPageState] 链式协程调用
 * 和 [mPageState] 页面状态管理
 */
open class BaseViewModel : ViewModel() {

    val mPageState: MutableLiveData<PageState> by lazy {
        MutableLiveData<PageState>()
    }

    open fun onPauseBundle(bundle: Bundle?) {}

    /**
     * 执行协程任务（无页面状态管理）
     * @param scope 协程作用域，默认 viewModelScope
     * @param context 执行上下文，默认 Dispatchers.IO
     * @param block 耗时任务代码块
     */
    fun <T> execute(
        scope: kotlinx.coroutines.CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend kotlinx.coroutines.CoroutineScope.() -> T
    ): Coroutine<T> {
        return Coroutine.async(scope, context) { block() }.apply {
            commonCoroutineCallback(this)
        }
    }

    /**
     * 执行协程任务（带页面状态管理）
     * 自动管理 LOADING / ERROR / NORMAL 页面状态
     * @param scope 协程作用域，默认 viewModelScope
     * @param context 执行上下文，默认 Dispatchers.IO
     * @param pageState 页面状态 LiveData
     * @param block 耗时任务代码块
     */
    fun <T> executeWithPageState(
        scope: kotlinx.coroutines.CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        pageState: MutableLiveData<PageState> = mPageState,
        block: suspend kotlinx.coroutines.CoroutineScope.() -> T
    ): Coroutine<T> {
        return Coroutine.async(scope, context, pageState) { block() }.apply {
            commonCoroutineCallback(this)
        }
    }

    /**
     * 公共协程回调，子类可重写统一处理
     */
    open fun <T> commonCoroutineCallback(coroutine: Coroutine<T>) {}

    // ============ EventBus ============

    /**
     * 当前页面是否需要注册 EventBus
     * 子类重写并返回 true 以启用 EventBus 事件接收
     */
    open fun isNeedEventBus(): Boolean = false

    @CallSuper
    override fun onCleared() {
        super.onCleared()
    }

    // ============ 页面状态管理 ============

    open fun onClickRefresh() {}

    open fun setStateLoading(msg: String? = null) {
        mPageState.value = PageState(PageStateType.LOADING).msg(msg)
    }

    open fun setStateError(throwable: Throwable? = null) {
        mPageState.value = PageState(PageStateType.ERROR).error(throwable)
    }

    open fun setStateEmpty() {
        mPageState.value = PageState(PageStateType.EMPTY)
    }

    open fun setStateNormal() {
        mPageState.value = PageState(PageStateType.NORMAL)
    }

    open fun updatePageState(pageState: PageState) {
        mPageState.value = pageState
    }
}
