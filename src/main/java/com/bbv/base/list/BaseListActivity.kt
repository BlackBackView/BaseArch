package com.bbv.base.list

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bbv.base.R
import com.bbv.base.list.adapter.BaseRecyclerViewAdapter
import com.bbv.base.list.adapter.setNewList
import com.bbv.base.list.adapter.setSimpleOnItemClick
import com.bbv.base.mvvm.BaseVMActivity
import com.bbv.base.mvvm.LoadType
import com.bbv.base.mvvm.PageState
import com.bbv.base.mvvm.PageStateType
import com.scwang.smart.refresh.layout.SmartRefreshLayout

/**
 * 列表 Activity 基类
 *
 * 基于 SmartRefreshLayout + RecyclerView + StateView 的列表页（Activity 版本）。
 * 适配器使用 [BaseRecyclerViewAdapter]（基于 BRVAH），支持 DataBinding + DiffUtil。
 *
 * 配合 [BaseListViewModel] 使用，自动处理：
 * - dataList → Adapter（带 DiffUtil 增量更新）
 * - refresh/loadMore 信号 → 结束 SmartRefreshLayout 动画
 * - 页面状态 → StateView 添加/移除视图
 *
 * ## 使用方式
 * ```
 * class OrderListActivity :
 *     BaseListActivity<ActivityOrderListBinding, OrderListViewModel, OrderBean, ItemOrderListBinding>() {
 *
 *     override val mViewModel: OrderListViewModel by lazy { OrderListViewModel() }
 *
 *     override fun getAdapter() = object :
 *         BaseRecyclerViewAdapter<OrderBean, ItemOrderListBinding>(R.layout.item_order_list) {
 *         override fun convert(holder: BaseDataBindingHolder<ItemOrderListBinding>, item: OrderBean) {
 *             holder.dataBinding?.setVariable(BR.viewModel, item)
 *         }
 *     }
 *
 *     override fun setupRecyclerView() {
 *         mRecyclerView.layoutManager = LinearLayoutManager(this)
 *     }
 *
 *     override fun setupRefreshLayout() {
 *         mRefreshLayout.setOnRefreshListener { mViewModel.onRefresh() }
 *         mRefreshLayout.setOnLoadMoreListener { mViewModel.onLoadMore() }
 *     }
 * }
 * ```
 */
abstract class BaseListActivity<VB : ViewBinding, VM : BaseListViewModel<T>, T, VDB : ViewDataBinding> :
    BaseVMActivity<VB, VM>() {

    // ======================== 子类必须实现 ========================

    abstract fun getAdapter(): BaseRecyclerViewAdapter<T, VDB>
    abstract fun setupRecyclerView()
    abstract fun setupRefreshLayout()

    // ======================== 可选重写 ========================

    open fun onItemClick(position: Int, item: T) {}
    open fun onAdapterReady(adapter: BaseRecyclerViewAdapter<T, VDB>) {}
    open fun isEnableRefresh(): Boolean = true
    open fun isEnableLoadMore(): Boolean = true

    open fun setGridSpanSizeLookup(gridLayoutManager: GridLayoutManager) {
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < mViewModel.dataList.size) 1 else gridLayoutManager.spanCount
            }
        }
    }

    // ======================== 内部引用 ========================

    protected lateinit var mRecyclerView: RecyclerView
    protected lateinit var mRefreshLayout: SmartRefreshLayout
    protected lateinit var mAdapter: BaseRecyclerViewAdapter<T, VDB>
    protected lateinit var mStateView: StateView

    // ======================== 生命周期 ========================

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 1. 查找 RecyclerView 和 RefreshLayout（通过命名 ID）
        mRecyclerView = findViewById(R.id.recyclerView)
        mRefreshLayout = findViewById(R.id.refreshLayoutView)

        // 2. 初始化 StateView
        mStateView = StateView.Builder(this)
            .setOnRetryClickListener { mViewModel.onFetchData() }
            .build()

        // 3. 子类配置 RecyclerView
        setupRecyclerView()

        // 4. 创建适配器并绑定点击事件
        mAdapter = getAdapter().apply {
            setSimpleOnItemClick { position, item ->
                onItemClick(position, item)
            }
        }
        onAdapterReady(mAdapter)
        mRecyclerView.adapter = mAdapter

        // 5. 配置 SmartRefreshLayout
        setupRefreshLayout()
        mRefreshLayout.setEnableRefresh(isEnableRefresh())
        mRefreshLayout.setEnableLoadMore(isEnableLoadMore())

        // 6. GridLayout span 适配
        (mRecyclerView.layoutManager as? GridLayoutManager)?.let {
            setGridSpanSizeLookup(it)
        }

        // 7. 监听 ViewModel
        observeViewModel()

        // 8. 首次加载数据
        mViewModel.onFetchData()
    }

    // ======================== 数据观察 ========================

    private fun observeViewModel() {
        // 列表数据 → Adapter（带 DiffUtil 增量更新）
        mViewModel.liveDataList.observe(this) { list ->
            mAdapter.setNewList(list.toMutableList())
        }

        // 刷新信号 → 结束下拉动画 + 重置无更多数据状态
        mViewModel.mRefresh.observe(this) {
            if (::mRefreshLayout.isInitialized) {
                mRefreshLayout.finishRefresh()
                mRefreshLayout.resetNoMoreData()
            }
        }

        // 注意：mPageState 由父类 BaseVMActivity 通过 dispatchPageState 自动观察
    }

    // ======================== 页面状态处理 ========================

    override fun dispatchPageState(pageState: PageState?) {
        pageState?.let { handlePageState(it) }
    }

    private fun handlePageState(pageState: PageState) {
        with(pageState) {
            when (pageStateType) {
                PageStateType.LOADING -> {
                    when (loadType) {
                        LoadType.FETCH -> mStateView.handlePageState(pageState)
                        LoadType.REFRESH -> { /* SmartRefreshLayout 动画已足够 */ }
                        LoadType.MORE -> { /* SmartRefreshLayout 动画已足够 */ }
                        LoadType.PRE -> {}
                    }
                }
                PageStateType.ERROR -> {
                    when (loadType) {
                        LoadType.FETCH -> mStateView.handlePageState(pageState)
                        LoadType.REFRESH -> {
                            mRefreshLayout.finishRefresh(false)
                            mStateView.handlePageState(pageState)
                        }
                        LoadType.MORE -> mRefreshLayout.finishLoadMore(false)
                        LoadType.PRE -> {}
                    }
                }
                PageStateType.EMPTY -> {
                    when (loadType) {
                        LoadType.FETCH -> mStateView.handlePageState(pageState)
                        LoadType.REFRESH -> {
                            mRefreshLayout.finishRefresh()
                            mStateView.handlePageState(pageState)
                        }
                        LoadType.MORE -> mRefreshLayout.finishLoadMoreWithNoMoreData()
                        LoadType.PRE -> {}
                    }
                }
                PageStateType.NORMAL -> {
                    mStateView.handlePageState(pageState) // 移除状态视图
                    when (loadType) {
                        LoadType.FETCH -> { /* 正常显示 */ }
                        LoadType.REFRESH -> {
                            mRefreshLayout.finishRefresh()
                            mRefreshLayout.resetNoMoreData()
                        }
                        LoadType.MORE -> mRefreshLayout.finishLoadMore()
                        LoadType.PRE -> {}
                    }
                }
            }
        }
    }
}
