package com.bbv.base.dialog

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import com.bbv.base.R
import com.bbv.base.dialog.util.dp2px
import com.bbv.base.dialog.util.radius

class SweetDialog : BaseDialog() {

    companion object {
        private const val LINEAR_ID = 0x00
    }

    /**
     * 默认的Item 字体大小
     */
    private var mTvDefaultTextSize = 14f

    /**
     * 默认的Item 字体高度
     */
    private var mTvHeight = 45

    /**
     * 默认的Item 圆角
     */
    private var mTvRadius = 10f

    private lateinit var mLinearLayout: LinearLayout

    private var mItems: MutableList<Wrapper>? = null

    private var mMarginTop: Int = 40

    private var mMarginBottom: Int = 40

    private var mStyleAnim = R.style.SheetDialogAnimation

    /**
     * 默认的Item 分割线
     */
    private var mLine: View? = null

    /**
     * 是否显示分割线
     */
    private var mShowLine: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val scrollView = ScrollView(context)
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            id = LINEAR_ID
        }
        scrollView.addView(linearLayout)
        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setSweetDefault()
        super.onViewCreated(view, savedInstanceState)
        mLinearLayout = view.findViewById(LINEAR_ID)
        preShowView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        preShowView()
    }

    private fun createDefaultLine(): View {
        return View(context).apply {
            setBackgroundColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
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

    private fun getCustomView(wrapper: Wrapper): View? {
        return wrapper.view.apply {
            this?.setOnClickListener {
                if (wrapper.dismiss) {
                    dialog?.dismiss()
                }
                wrapper.singleClickListener?.invoke()
            }
        }
    }

    private fun getView(wrapper: Wrapper, type: Int): View {
        return getCustomView(wrapper) ?: createItemTextView(
            wrapper = wrapper, type = type
        )
    }

    private fun preShowView() {
        mLinearLayout.removeAllViews()
        // 将Cancel 过滤掉
        val filter = mItems?.filter { it.type != DialogType.CANCEL }
        val size = filter?.size ?: 0
        if (size == 1) {
            filter?.forEach { wrapper ->
                mLinearLayout.addView(getView(wrapper, ItemType.SINGLE))
            }
        } else {
            filter?.forEachIndexed { index, wrapper ->
                val view = when (index) {
                    0 -> getView(wrapper, ItemType.TOP)
                    size - 1 -> getView(wrapper, ItemType.BOTTOM)
                    else -> getView(wrapper, ItemType.NORMAL)
                }
                mLinearLayout.addView(view)
                if (index < size - 1 && mShowLine) {
                    mLinearLayout.addView(mLine ?: createDefaultLine())
                }
            }
        }

        // 添加Cancel
        mItems?.filter { it.type == DialogType.CANCEL }?.forEach {
            mLinearLayout.addView(
                createItemTextView(
                    wrapper = it,
                    type = ItemType.SINGLE,
                    marginTop = mMarginTop,
                    marginBottom = mMarginBottom,
                )
            )
        }
    }

    private fun createItemTextView(
        wrapper: Wrapper,
        @ItemType type: Int = ItemType.NORMAL,
        marginTop: Int = 0,
        marginBottom: Int = 0
    ): TextView {
        return TextView(context).apply {
            this.text = wrapper.info
            this.gravity = Gravity.CENTER
            wrapper.textSize?.let { this.textSize = it }
            wrapper.height?.let { this.height = it }
            setTextColor(wrapper.color ?: resources.getColor(R.color.dialog_sweet_tv_color))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER
                setMargins(0, marginTop, 0, marginBottom)
            }
            this.setOnClickListener {
                if (wrapper.dismiss) {
                    dialog?.dismiss()
                }
                wrapper.singleClickListener?.invoke()
            }
            this.setOnLongClickListener {
                wrapper.longClickListener?.invoke()
                false
            }
            val backgroundColor =
                wrapper.backgroundColor ?: resources.getColor(R.color.dialog_sweet_bg_color)
            when (type) {
                ItemType.NORMAL -> this.radius(color = backgroundColor)
                ItemType.TOP -> this.radius(tl = mTvRadius, tr = mTvRadius, color = backgroundColor)
                ItemType.SINGLE -> this.radius(radius = mTvRadius, color = backgroundColor)
                ItemType.BOTTOM -> this.radius(
                    bl = mTvRadius,
                    br = mTvRadius,
                    color = backgroundColor
                )
            }
            // 自定义属性
            wrapper.theme?.invoke(this)
        }
    }

    private fun addWrapper(wrapper: Wrapper) {
        if (mItems == null) mItems = mutableListOf()
        mItems?.add(wrapper)
    }

    /**
     * 添加一个TextView作为Item
     *
     * [info]               名称
     * [textSize]           字体大小 sp
     * [color]              字体颜色
     * [backgroundColor]    背景颜色
     * [height]             item高度 dp
     * [dismiss]            点击是否消失
     * [singleClick]        点击事件
     * [longClick]          长按事件
     * [theme]              TextView的属性
     */
    fun addItem(
        info: String,
        textSize: Float = mTvDefaultTextSize,
        @ColorRes color: Int? = null,
        @ColorRes backgroundColor: Int? = null,
        height: Int = mTvHeight,
        longClick: (() -> Unit)? = null,
        dismiss: Boolean = true,
        theme: ((TextView) -> Unit)? = null,
        singleClick: (() -> Unit)? = null,
    ) = apply {
        addWrapper(
            Wrapper(
                info = info,
                textSize = textSize,
                color = color,
                dismiss = dismiss,
                backgroundColor = backgroundColor,
                singleClickListener = singleClick,
                longClickListener = longClick,
                type = DialogType.ITEM,
                height = height.dp2px(),
                theme = theme
            )
        )
    }

    /**
     * 添加一个View作为Item
     */
    fun addItem(view: View, dismiss: Boolean = true) = apply {
        addWrapper(wrapper = Wrapper(view = view, dismiss = dismiss, type = DialogType.ITEM))
    }

    /**
     * 添加Title
     */
    fun addTitle(
        title: String,
        textSize: Float = mTvDefaultTextSize,
        @ColorRes color: Int? = null,
        @ColorRes backgroundColor: Int? = null,
        height: Int = mTvHeight,
        theme: ((TextView) -> Unit)? = null,
    ) = apply {
        addWrapper(
            Wrapper(
                info = title,
                textSize = textSize,
                color = color,
                type = DialogType.TITLE,
                backgroundColor = backgroundColor,
                height = height.dp2px(),
                dismiss = false,
                theme = theme
            )
        )
    }

    fun addTitle(view: View) = apply {
        addWrapper(
            wrapper = Wrapper(view = view, dismiss = false)
        )
    }

    /**
     * 设置分割线
     */
    fun setLine(view: View) = apply { this.mLine = view }

    /**
     * 设置是否显示分割线
     */
    fun setShowLine(show: Boolean) = apply { this.mShowLine = show }

    /**
     * 设置底部的取消按钮距离顶部内容的margin
     */
    fun setMarginTop(margin: Int) = apply { this.mMarginTop = margin }

    /**
     * 设置底部的取消按钮距离底部内容的margin
     */
    fun setMarginBottom(margin: Int) = apply { this.mMarginBottom = margin }

    fun addCancel(
        title: String,
        textSize: Float = mTvDefaultTextSize,
        @ColorRes color: Int? = null,
        @ColorRes backgroundColor: Int? = null,
        height: Int = mTvHeight,
        theme: ((TextView) -> Unit)? = null,
        singleClick: (() -> Unit)? = null
    ) = apply {
        addWrapper(
            Wrapper(
                info = title,
                textSize = textSize,
                color = color,
                type = DialogType.CANCEL,
                backgroundColor = backgroundColor,
                height = height.dp2px(),
                theme = theme,
                singleClickListener = singleClick,
            )
        )
    }

    fun addCancel(view: View) = apply {
        addWrapper(
            wrapper = Wrapper(view = view)
        )
    }

    data class Wrapper(
        var info: String? = null,
        @DialogType var type: Int = DialogType.ITEM,
        var textSize: Float? = null,
        @ColorRes var color: Int? = null,
        @ColorRes var backgroundColor: Int? = null,
        var height: Int? = null,
        var view: View? = null,
        var dismiss: Boolean = true,
        var singleClickListener: (() -> Unit)? = null,
        var longClickListener: (() -> Unit)? = null,
        var theme: ((TextView) -> Unit)? = null
    )

    @IntDef(ItemType.TOP, ItemType.BOTTOM, ItemType.NORMAL, ItemType.SINGLE)
    private annotation class ItemType {
        companion object {
            const val TOP = 1
            const val BOTTOM = 2
            const val NORMAL = 3
            const val SINGLE = 4
        }
    }

    @IntDef(DialogType.TITLE, DialogType.CANCEL, DialogType.ITEM)
    private annotation class DialogType {
        companion object {
            const val TITLE = 1
            const val CANCEL = 2
            const val ITEM = 3
        }
    }
}
