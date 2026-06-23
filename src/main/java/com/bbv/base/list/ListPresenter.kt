package com.bbv.base.list

/**
 * 列表数据加载接口
 */
interface ListPresenter<T> {
    val isLoading: Boolean
    val dataList: List<T>
    fun onFetchData()
    fun onLoadMore()
    fun onLoadPre()
    suspend fun loadList(): List<T>
    fun canLoadMoreOrPre(): Boolean
}
