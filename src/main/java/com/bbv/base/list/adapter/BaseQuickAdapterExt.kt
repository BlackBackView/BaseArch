package com.bbv.base.list.adapter

import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * BaseQuickAdapter 扩展函数
 */

/**
 * 带 DiffUtil 的 setList，自动判断首次/增量更新
 */
fun <T, VH : BaseViewHolder> BaseQuickAdapter<T, VH>.setNewList(list: List<T>?) {
    // 如果没有设置 DiffCallback，设置一个默认的
    if (runCatching { getDiffer(); false }.getOrDefault(true)) {
        setDefDiffCallback()
    }

    val newData = if (list.isNullOrEmpty()) {
        mutableListOf()
    } else {
        list as MutableList<T>
    }
    if (data.isEmpty()) {
        setNewInstance(newData)
    } else {
        setDiffNewData(newData)
    }
}

/**
 * 简化 item 点击事件绑定
 */
fun <T, VH : BaseViewHolder> BaseQuickAdapter<T, VH>.setSimpleOnItemClick(
    onItemClickListener: (Int, T) -> Unit
) {
    this.setOnItemClickListener { _, _, position ->
        onItemClickListener.invoke(position, getItem(position))
    }
}

/**
 * 设置默认的 DiffCallback（基于 hashCode + toString）
 */
fun <T, VH : BaseViewHolder> BaseQuickAdapter<T, VH>.setDefDiffCallback() {
    setDiffCallback(object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return (oldItem.hashCode() == newItem.hashCode()) &&
                    (oldItem.toString() == newItem.toString())
        }

        override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return (oldItem.hashCode() == newItem.hashCode()) &&
                    (oldItem.toString() == newItem.toString())
        }
    })
}
