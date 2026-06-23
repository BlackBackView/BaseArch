package com.bbv.base.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * PopupWindow DataBinding 弹窗
 * 对应旧项目的 bindPPDialog
 */
class CommonPPDialog<T : ViewDataBinding> {

    private var context: Context? = null
    private var layoutId: Int = 0
    private var animationStyle: Int = 0
    private var action: ((T, CommonPPDialog<T>?) -> Unit)? = null
    private var binding: T? = null
    private var popupWindow: PopupWindow? = null
    private var width: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    private var height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    private var focusable: Boolean = true
    private var outsideTouchable: Boolean = true

    fun setContext(context: Context) {
        this.context = context
    }

    fun setContentView(@LayoutRes layoutId: Int) {
        this.layoutId = layoutId
    }

    fun setAnimationStyle(@StyleRes animationStyle: Int) {
        this.animationStyle = animationStyle
    }

    fun setAction(action: (T, CommonPPDialog<T>?) -> Unit) {
        this.action = action
    }

    fun setWidth(width: Int) {
        this.width = width
    }

    fun setHeight(height: Int) {
        this.height = height
    }

    fun setFocusable(focusable: Boolean) {
        this.focusable = focusable
    }

    fun setOutsideTouchable(outsideTouchable: Boolean) {
        this.outsideTouchable = outsideTouchable
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    fun showAsDropDown(anchor: View) {
        val ctx = context ?: return
        val inflater = LayoutInflater.from(ctx)
        binding = DataBindingUtil.inflate(inflater, layoutId, null, false)
        val rootView = binding?.root ?: return

        action?.invoke(binding!!, this)

        popupWindow = PopupWindow(rootView, width, height, focusable).apply {
            isOutsideTouchable = outsideTouchable
            if (animationStyle != 0) {
                setAnimationStyle(animationStyle)
            }
            setBackgroundDrawable(ColorDrawable(0))
            showAsDropDown(anchor)
        }
    }

    fun showAtLocation(parent: View, gravity: Int, x: Int, y: Int) {
        val ctx = context ?: return
        val inflater = LayoutInflater.from(ctx)
        binding = DataBindingUtil.inflate(inflater, layoutId, null, false)
        val rootView = binding?.root ?: return

        action?.invoke(binding!!, this)

        popupWindow = PopupWindow(rootView, width, height, focusable).apply {
            isOutsideTouchable = outsideTouchable
            if (animationStyle != 0) {
                setAnimationStyle(animationStyle)
            }
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(parent, gravity, x, y)
        }
    }
}
