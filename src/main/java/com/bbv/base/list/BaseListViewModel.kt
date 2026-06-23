package com.bbv.base.list

import androidx.lifecycle.MutableLiveData
import com.bbv.base.coroutine.Coroutine
import com.bbv.base.mvvm.BaseViewModel
import com.bbv.base.mvvm.LoadType
import com.bbv.base.mvvm.PageState
import com.bbv.base.mvvm.PageStateType

/**
 * 列表 ViewModel 基类
 *
 * 提供分页数据加载能力，配合 [BaseListFragment] / [BaseListActivity] 使用。
 *
 * 使用方式：
 * ```
 * class MyListViewModel : BaseListViewModel<MyBean>() {
 *     override suspend fun loadList(): List<MyBean> {
 *         return repository.fetchList(pageIndex, pageSize)
 *     }
 * }
 * ```
 *
 * 核心特性：
 * - 使用 [execute] / [executeWithPageState] 链式协程
 * - 自动管理 [dataList] 和 [liveDataList]
 * - 支持 FETCH / REFRESH / MORE 三种加载模式
 * - 自动处理空数据、错误、加载完成等状态
 */
abstract class BaseListViewModel<T> : BaseViewModel(), ListPresenter<T> {

    final override val dataList: MutableList<T> = mutableListOf()

    /** 列表数据 LiveData（通过 DiffUtil 提交到 Adapter） */
    val liveDataList: MutableLiveData<MutableList<T>> = MutableLiveData()

    /** 下拉刷新信号 */
    val mRefresh: MutableLiveData<Boolean> = MutableLiveData(false)

    /** 是否正在加载 */
    override var isLoading = false

    /** 是否已无更多数据 */
    private var mDataEndFlag = false

    /**
     * 初始页码，有些 API 从 0 开始，有些从 1 开始。
     * 子类可重写，如 `override val initialPageIndex: Int = 1`
     */
    open val initialPageIndex: Int = 0

    /** 当前页码 */
    var pageIndex = initialPageIndex
        private set

    /** 每页大小，默认 20 */
    open val pageSize: Int = 20

    // ============ 列表数据接口实现 ============

    override fun onFetchData() {
        pageIndex = initialPageIndex
        loadData(LoadType.FETCH)
    }

    override fun onLoadMore() {
        if (isLoading) return
        loadData(LoadType.MORE)
    }

    override fun onLoadPre() {
        // 暂不支持向上加载
    }

    open fun onRefresh() {
        pageIndex = initialPageIndex
        loadData(LoadType.REFRESH)
    }

    override fun onClickRefresh() {
        onFetchData()
    }

    override fun canLoadMoreOrPre(): Boolean {
        synchronized(this) {
            return !mDataEndFlag && !isLoading
        }
    }

    // ============ 数据加载核心 ============

    protected open fun loadData(loadType: LoadType): Coroutine<List<T>> {
        return execute {
            loadList()
        }.onStart {
            synchronized(this) { isLoading = true }
            onLoadStart(loadType)
        }.onSuccess { items ->
            onLoadSuccess(items ?: emptyList(), loadType)
        }.onError { e ->
            onLoadFail(e, loadType)
        }.onFinally {
            synchronized(this) { isLoading = false }
        }
    }

    /** 子类实现：从数据源加载列表数据 */
    override abstract suspend fun loadList(): List<T>

    // ============ 加载回调 ============

    protected open fun onLoadStart(loadType: LoadType) {
        val pageState = when (loadType) {
            LoadType.FETCH -> PageState(PageStateType.LOADING)
            LoadType.REFRESH -> PageState(PageStateType.LOADING)
            LoadType.MORE -> PageState(PageStateType.LOADING)
            LoadType.PRE -> PageState(PageStateType.LOADING)
        }
        mPageState.value = pageState.loadType(loadType)
    }

    protected open fun onLoadSuccess(items: List<T>, loadType: LoadType) {
        if (items.isEmpty()) {
            when (loadType) {
                LoadType.MORE -> {
                    // 加载更多没数据了 → 只标记结束，不清现有数据
                    setEndStatus(true)
                    mPageState.value = PageState(PageStateType.NORMAL).loadType(loadType)
                }
                else -> {
                    // FETCH / REFRESH / PRE 没数据 → 清空列表，显示空状态
                    dataList.clear()
                    sendData()
                    setEndStatus(true)
                    mPageState.value = PageState(PageStateType.EMPTY).loadType(loadType)
                }
            }
        } else {
            when (loadType) {
                LoadType.FETCH -> {
                    dataList.clear()
                    dataList.addAll(items)
                    setEndStatus(items.size < pageSize)
                    pageIndex++
                    mPageState.value = PageState(PageStateType.NORMAL).loadType(loadType)
                }
                LoadType.REFRESH -> {
                    dataList.clear()
                    dataList.addAll(items)
                    setEndStatus(items.size < pageSize)
                    pageIndex++
                    mRefresh.value = false
                    mPageState.value = PageState(PageStateType.NORMAL).loadType(loadType)
                }
                LoadType.MORE -> {
                    dataList.addAll(items)
                    setEndStatus(items.size < pageSize)
                    pageIndex++
                    mPageState.value = PageState(PageStateType.NORMAL).loadType(loadType)
                }
                LoadType.PRE -> {
                    dataList.addAll(0, items)
                    sendData()
                    mPageState.value = PageState(PageStateType.NORMAL).loadType(loadType)
                }
            }
            sendData()
        }
    }

    protected open fun onLoadFail(e: Throwable?, loadType: LoadType) {
        val pageState = PageState(PageStateType.ERROR, throwable = e)
        mPageState.value = pageState.loadType(loadType)
    }

    // ============ 内部方法 ============

    protected open fun sendData() {
        liveDataList.value = dataList.toMutableList()
    }

    private fun setEndStatus(end: Boolean) {
        mDataEndFlag = end
    }

    fun onCurrentListChanged(previousList: List<T>, currentList: List<T>) {
        // 子类可重写
    }
}
