package com.bbv.base.dialog

import androidx.databinding.ViewDataBinding

/**
 * PopupWindow DataBinding 弹窗 DSL
 * 对应旧项目的 bindPPDialog
 */
fun <T : ViewDataBinding> bindPPDialog(
    dialog: CommonPPDialog<T>.() -> Unit
): CommonPPDialog<T> {
    return CommonPPDialog<T>().apply(dialog)
}
