# RecyclerView 列表 — BaseArch Skill

## 功能概述
提供列表页面的快速开发支持，包含基础列表 Activity/Fragment/ViewModel、状态视图（加载中/空数据/错误）、RecyclerView 适配器封装。

## 文件清单

### 1. StateView.kt
```kotlin
package com.bbv.base.list

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bbv.base.R

/**
 * 多状态视图：加载中、空数据、错误、正常内容
 */
class StateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class State { LOADING, EMPTY, ERROR, CONTENT }

    private var currentState = State.CONTENT

    fun setState(state: State) {
        currentState = state
        when (state) {
            State.LOADING -> showLoading()
            State.EMPTY -> showEmpty()
            State.ERROR -> showError()
            State.CONTENT -> showContent()
        }
    }

    private fun showLoading() { /* 显示加载布局 */ }
    private fun showEmpty() { /* 显示空数据布局 */ }
    private fun showError() { /* 显示错误布局 */ }
    private fun showContent() { /* 显示内容布局 */ }

    var onRetry: (() -> Unit)? = null
}
```

### 2. BaseListViewModel.kt
```kotlin
package com.bbv.base.list

import androidx.lifecycle.MutableLiveData
import com.bbv.base.mvvm.BaseViewModel
import com.bbv.base.mvvm.PageState

/**
 * 列表页 ViewModel 基类
 */
open class BaseListViewModel : BaseViewModel() {

    val pageState = MutableLiveData<PageState>()
    val refreshState = MutableLiveData<Boolean>()
    val loadMoreState = MutableLiveData<Boolean>()

    var page: Int = 1
    var hasMore: Boolean = true

    fun refresh() {
        page = 1
        hasMore = true
        refreshState.postValue(true)
        loadData()
    }

    open fun loadData() { }

    fun loadMore() {
        if (!hasMore || loadMoreState.value == true) return
        page++
        loadMoreState.postValue(true)
        loadData()
    }

    protected fun onRefreshSuccess() {
        refreshState.postValue(false)
        pageState.postValue(PageState.Success)
    }

    protected fun onLoadMoreSuccess(hasMoreData: Boolean) {
        hasMore = hasMoreData
        loadMoreState.postValue(false)
    }

    protected fun onError(msg: String) {
        refreshState.postValue(false)
        loadMoreState.postValue(false)
        pageState.postValue(PageState.Error(-1, msg))
    }
}
```

### 3. BaseRecyclerViewAdapter.kt
```kotlin
package com.bbv.base.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * 通用 RecyclerView 适配器基类
 */
abstract class BaseRecyclerViewAdapter<T, VB : ViewBinding>(
    private var items: MutableList<T> = mutableListOf()
) : RecyclerView.Adapter<BaseRecyclerViewAdapter.VH<VB>>() {

    class VH<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    abstract fun getBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun bind(binding: VB, item: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<VB> {
        val binding = getBinding(LayoutInflater.from(parent.context), parent)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH<VB>, position: Int) {
        bind(holder.binding, items[position], position)
    }

    override fun getItemCount(): Int = items.size

    fun setList(list: List<T>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun addList(list: List<T>) {
        val start = items.size
        items.addAll(list)
        notifyItemRangeInserted(start, list.size)
    }
}
```

## 依赖要求
```gradle
dependencies {
    implementation 'io.github.cymchad:BaseRecyclerViewAdapterHelper:3.0.14'
    implementation 'io.github.scwang90:refresh-layout-kernel:2.1.0'
    implementation 'io.github.scwang90:refresh-header-classics:2.1.0'
    implementation 'io.github.scwang90:refresh-footer-classics:2.1.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
}
```

## 使用方法
```kotlin
class MyAdapter : BaseRecyclerViewAdapter<String, ItemUserBinding>() {
    override fun getBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemUserBinding.inflate(inflater, parent, false)
    override fun bind(binding: ItemUserBinding, item: String, position: Int) {
        binding.tvName.text = item
    }
}

// 在 Activity/Fragment 中
val adapter = MyAdapter()
recyclerView.adapter = adapter
viewModel.dataList.observe(this) { adapter.setList(it) }
```