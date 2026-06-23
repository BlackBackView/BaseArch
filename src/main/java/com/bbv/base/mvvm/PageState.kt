package com.bbv.base.mvvm

import com.bbv.base.ext.toShowMsg

/**
 * 当前页面状态
 */
data class PageState(
    val pageStateType: PageStateType,
    var msg: String? = null,
    var throwable: Throwable? = null,
    var loadType: LoadType = LoadType.FETCH
) {
    fun msg(string: String?): PageState {
        msg = string
        return this
    }

    fun error(throwable: Throwable?): PageState {
        this.throwable = throwable
        return this
    }

    fun loadType(loadType: LoadType): PageState {
        this.loadType = loadType
        return this
    }

    fun getShowMsg(): String? {
        return msg ?: (throwable?.toShowMsg() ?: "未知错误")
    }
}

/** 当前页面状态类型 */
enum class PageStateType {
    LOADING,  // 正在加载
    ERROR,    // 错误
    EMPTY,    // 空状态
    NORMAL,   // 正常显示
}

/** 当前加载类型 */
enum class LoadType {
    FETCH,    // 首次加载
    REFRESH,  // 刷新
    MORE,     // 加载下一页
    PRE,      // 加载上一页
}
