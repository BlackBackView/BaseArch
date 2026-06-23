package com.bbv.base.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bbv.base.R
import java.util.concurrent.atomic.AtomicBoolean

class ScrollDialog : BaseDialog() {

    companion object {
        private const val LINEAR_ID = 0x00
        private const val TAG = "ScrollDialog"
    }

    private var actionHeard: ((View, Dialog?) -> Unit)? = null

    private var actionFood: ((View, Dialog?) -> Unit)? = null

    private var actionCenter: ((View, Dialog?) -> Unit)? = null

    // 头部layoutId
    var heardLayoutId: Int? = null

    // 尾部layoutId
    var footLayoutId: Int? = null

    // 中间布局的ID
    var centerLayoutId: Int? = null

    private var headView: View? = null

    private var footView: View? = null

    private var centerView: View? = null

    private var scrollView: ScrollView? = null

    private lateinit var parent: ViewGroup

    private var mStyleAnim = R.style.SheetDialogAnimation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        parent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            id = LINEAR_ID
        }

        // 头部不可滑动
        heardLayoutId?.let {
            Log.v(TAG, "头部不为空")
            headView = inflater.inflate(it, container, false)
            parent.addView(headView)
        }

        // 中间的布局 可滑动
        centerLayoutId?.let {
            Log.v(TAG, "中间不为空")
            centerView = inflater.inflate(it, container, false)
            scrollView = ScrollView(context)
        }

        // 尾部不可滑动
        footLayoutId?.let {
            Log.v(TAG, "尾部不为空")
            footView = inflater.inflate(it, container, false)
            parent.addView(footView)
        }
        return parent
    }

    fun setHead(action: (View, Dialog?) -> Unit) {
        actionHeard = action
    }

    fun setFood(action: (View, Dialog?) -> Unit) {
        actionFood = action
    }

    fun setCenter(action: (View, Dialog?) -> Unit) {
        actionCenter = action
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setSweetDefault()
        super.onViewCreated(view, savedInstanceState)

        getHeadAndFootHeight { head, foot ->
            println("head:$head foot:$foot")
            // 中间的布局 可滑动
            centerLayoutId?.let {
                scrollView?.addView(centerView)
                parent.addView(scrollView, getAddPos())
                // 需要减去heard 和 foot 的高度
                scrollView?.layoutParams =
                    LinearLayout.LayoutParams(getWith(), getHeight() - foot - head)
            }
        }

        headView?.let { actionHeard?.invoke(it, dialog) }

        footView?.let { actionFood?.invoke(it, dialog) }

        centerView?.let { actionCenter?.invoke(it, dialog) }
    }

    private fun getAddPos(): Int {
        if (footView != null && headView != null) {
            return 1
        }
        if (footView != null) {
            return 0
        }
        return 1
    }

    private fun getHeadAndFootHeight(action: (Int, Int) -> Unit) {
        val isFoot = AtomicBoolean(false)
        val isHead = AtomicBoolean(false)
        var footHeight = 0
        var headHeight = 0
        if (footView == null) {
            isFoot.getAndSet(true)
        }
        if (headView == null) {
            isHead.getAndSet(true)
        }
        footView?.post {
            footHeight = footView?.height ?: 0
            isFoot.getAndSet(true)
            if (isFoot.get() && isHead.get()) {
                action.invoke(headHeight, footHeight)
            }
        }

        headView?.post {
            headHeight = headView?.height ?: 0
            isHead.getAndSet(true)
            if (isFoot.get() && isHead.get()) {
                action.invoke(headHeight, footHeight)
            }
        }
    }

    /**
     * 设置默认的属性
     */
    private fun setSweetDefault() {
        if (getGravity() == null) {
            setGravity(Gravity.BOTTOM)
        }
        if (getWidthScale() == null) {
            setWidthScale(0.8f)
        }
        setWindowAnimations(mStyleAnim)
    }
}
