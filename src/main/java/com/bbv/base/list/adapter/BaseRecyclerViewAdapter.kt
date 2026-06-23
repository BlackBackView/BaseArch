package com.bbv.base.list.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 * 基于 BRVAH 的 DataBinding 适配器基类
 *
 * 使用方式：
 * ```
 * object : BaseRecyclerViewAdapter<OrderBean, ItemOrderListBinding>(R.layout.item_order_list) {
 *     override fun convert(holder: BaseDataBindingHolder<ItemOrderListBinding>, item: OrderBean) {
 *         holder.dataBinding?.setVariable(BR.viewModel, item)
 *     }
 * }
 * ```
 */
abstract class BaseRecyclerViewAdapter<T, VDB : ViewDataBinding>(
    @LayoutRes private val layoutResId: Int
) : BaseQuickAdapter<T, BaseDataBindingHolder<VDB>>(layoutResId) {

    private var lifecycleOwner: LifecycleOwner? = null

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseDataBindingHolder<VDB> {
        if (lifecycleOwner == null) {
            lifecycleOwner = parent.findViewTreeLifecycleOwner()
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(
        holder: BaseDataBindingHolder<VDB>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        holder.dataBinding?.lifecycleOwner = lifecycleOwner
    }
}
