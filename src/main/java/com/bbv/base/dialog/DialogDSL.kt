package com.bbv.base.dialog

import android.view.Gravity
import androidx.databinding.ViewDataBinding
import com.bbv.base.dialog.bottom.CommonBottomDialog

/** 底部可滚动弹窗（头+中+尾三段式） */
fun scrollDialog(dialog: ScrollDialog.() -> Unit): ScrollDialog {
    val scrollDialog = ScrollDialog().apply {
        setRadius(10f)
        setAlpha(1f)
        setDimAmount(0.2f)
        setButtonAnimation()
        setWidthScale(1.0f)
        setHeightScale(0.7f)
        setGravity(Gravity.BOTTOM)
        setCanceledOnBackPressed(true)
        setCanceledOnTouchOutside(true)
    }
    return scrollDialog.apply(dialog)
}

/** 通用居中弹窗 */
fun commonDialog(dialog: CommonDialog.() -> Unit): CommonDialog {
    val commonDialog = CommonDialog().apply {
        setRadius(10f)
        setDimAmount(0.2f)
        setAlpha(1f)
        setWidthScale(0.8f)
        setGravity(Gravity.CENTER)
        setCanceledOnBackPressed(true)
        setCanceledOnTouchOutside(true)
        setTag(System.currentTimeMillis().toString())
    }
    return commonDialog.apply(dialog)
}

/** 底部选项弹窗（类似 ActionSheet） */
fun sweetDialog(dialog: SweetDialog.() -> Unit): SweetDialog {
    val sweetDialog = SweetDialog().apply {
        setRadius(10f)
        setAlpha(1f)
        setDimAmount(0.2f)
        setButtonAnimation()
        setWidthScale(0.8f)
        setGravity(Gravity.BOTTOM)
        setCanceledOnBackPressed(true)
        setCanceledOnTouchOutside(true)
        setTag(System.currentTimeMillis().toString())
    }
    return sweetDialog.apply(dialog)
}

/** DataBinding 通用居中弹窗 */
fun <T : ViewDataBinding> bindDialog(
    dialog: CommonBindDialog<T>.() -> Unit
): CommonBindDialog<T> {
    val bindDialog = CommonBindDialog<T>()
    bindDialog.apply {
        setRadius(10f)
        setAlpha(1f)
        setDimAmount(0.2f)
        setWidthScale(0.8f)
        setCanceledOnBackPressed(true)
        setCanceledOnTouchOutside(true)
        setTag(System.currentTimeMillis().toString())
    }
    return bindDialog.apply(dialog)
}

/** DataBinding 底部弹窗 */
fun <T : ViewDataBinding> bottomDialog(
    dialog: CommonBottomDialog<T>.() -> Unit
): CommonBottomDialog<T> {
    val bindDialog = CommonBottomDialog<T>()
    bindDialog.apply {
        setRadius(10f)
        setAlpha(1f)
        setDimAmount(0.2f)
        setWidthScale(1f)
        setCanceledOnBackPressed(true)
        setCanceledOnTouchOutside(true)
        setTag(System.currentTimeMillis().toString())
    }
    return bindDialog.apply(dialog)
}
