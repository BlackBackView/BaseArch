package com.bbv.base.list

import android.content.Context
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.bbv.base.R
import com.bbv.base.mvvm.BaseActivity
import com.bbv.base.mvvm.PageState
import com.bbv.base.mvvm.PageStateType

/**
 * 状态视图管理器
 *
 * 核心能力：将 Loading / Empty / Error 视图 **添加** 到容器中，
 * 而不是简单地隐藏/显示已有视图。
 * 这确保了状态视图独立于列表内容，不会受到布局嵌套影响。
 *
 * 使用方式：
 * ```
 * val stateView = StateView.Builder(activity)
 *     .setOnRetryClickListener { mViewModel.onFetchData() }
 *     .build()
 *
 * stateView.handlePageState(pageState)
 * ```
 */
class StateView(private val builder: Builder) {

    private val cache: SparseArray<View> = SparseArray(3)

    /**
     * 根据页面状态，向容器添加对应的状态视图
     */
    fun handlePageState(pageState: PageState) {
        val pair = findContainer() ?: return
        val (host, context) = pair

        if (PageStateType.NORMAL == pageState.pageStateType) {
            removeStateView(host)
            return
        }

        removeStateView(host)
        val layoutId = when (pageState.pageStateType) {
            PageStateType.LOADING -> builder.layoutLoadingId
            PageStateType.ERROR -> builder.layoutErrorId
            PageStateType.EMPTY -> builder.layoutEmptyId
            else -> builder.layoutEmptyId
        }

        var stateContent: View? = cache.get(layoutId)
        if (stateContent == null) {
            stateContent = LayoutInflater.from(context).inflate(layoutId, host, false)
            stateContent.id = R.id.state_view
            cache.put(layoutId, stateContent)
        }

        setupStateContent(stateContent!!, host, pageState)
    }

    private fun removeStateView(host: ViewGroup) {
        host.findViewById<View>(R.id.state_view)?.let { host.removeView(it) }
    }

    private fun setupStateContent(stateContent: View, host: ViewGroup, pageState: PageState) {
        host.addView(stateContent)

        val msgView: TextView? = stateContent.findViewById(R.id.state_view_msg)
        val msg = pageState.msg ?: pageState.throwable?.message
        if (msgView != null) {
            msgView.text = msg ?: ""
        }

        if (pageState.pageStateType == PageStateType.ERROR) {
            stateContent.findViewById<View>(R.id.state_view_bt_retry)?.setOnClickListener(
                builder.onRetryClickListener
            )
        }
    }

    /** 查找合适的父容器 */
    private fun findContainer(): Pair<ViewGroup, Context>? {
        var host: ViewGroup? = null
        var context: Context? = null

        when (val component = builder.component) {
            is BaseActivity<*> -> {
                host = component.findViewById(builder.stateContainerId)
                    ?: component.findViewById(R.id.appPageStateContainer)
                context = component
            }
            is Fragment -> {
                val rootView = component.view
                if (rootView != null) {
                    host = rootView.findViewById(builder.stateContainerId)
                        ?: rootView.findViewById(R.id.appPageStateContainer)
                        ?: (rootView as? ViewGroup)
                }
                if (host != null) context = component.requireActivity()
            }
            is View -> {
                host = component.findViewById(builder.stateContainerId)
                    ?: component.findViewById(R.id.appPageStateContainer)
                context = host?.context
            }
        }

        if (host == null || context == null) return null

        if (host !is FrameLayout && host !is ConstraintLayout && host !is RelativeLayout) {
            Log.w("StateView", "容器布局建议使用 FrameLayout / ConstraintLayout / RelativeLayout")
        }

        return Pair(host, context)
    }

    class Builder(
        val component: Any,
        var onRetryClickListener: View.OnClickListener = View.OnClickListener { },
        var layoutLoadingId: Int = R.layout.base_layout_loading,
        var layoutEmptyId: Int = R.layout.base_layout_empty,
        var layoutErrorId: Int = R.layout.base_layout_error,
        var stateContainerId: Int = 0
    ) {
        fun setOnRetryClickListener(listener: View.OnClickListener): Builder {
            onRetryClickListener = listener
            return this
        }

        fun build(): StateView = StateView(this)
    }
}
