package com.bbv.base.dialog.bottom

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bbv.base.dialog.util.radius
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "BaseBottomSheetDialog"
    }

    private lateinit var mDisplay: Display

    private var mTag: String? = null

    private var mCanceledOnTouchOutside: Boolean? = null

    private var mCancelable: Boolean? = null

    private var mViewCreate: ((View, Dialog?) -> Unit)? = null

    private var mBackAction: (() -> Unit)? = null

    private var mActionDialogDismiss: (() -> Unit)? = null

    private var mWidth: Int? = null

    private var mAlpha: Float? = null

    private var mDimAmount: Float? = null

    private var mWidthScale: Float? = null

    private var mRadius: Float? = null

    /**
     * 判断是根据比例设置宽高还是依据大小设置宽高
     * @see SizeFrom
     */
    private var mWidthFrom: Int? = null

    private var configChange: ((Configuration) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDisplay =
            (activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addListener()
        mRadius?.let { view.radius(radius = it) }
        mViewCreate?.invoke(view, dialog)
        mCancelable?.let { dialog?.setCancelable(it) }
        mCanceledOnTouchOutside?.let { dialog?.setCanceledOnTouchOutside(it) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configChange?.invoke(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionDialogDismiss?.invoke()
    }

    override fun onStart() {
        super.onStart()
        setDialogTheme()
    }

    open fun show(context: FragmentActivity) {
        if (dialog?.isShowing == true) return
        kotlin.runCatching {
            show(context.supportFragmentManager, mTag)
        }.onFailure {
            Log.e(TAG, "show dialog failed:$it")
        }
    }

    open fun show(context: Fragment) {
        if (dialog?.isShowing == true) return
        kotlin.runCatching {
            show(context.childFragmentManager, mTag)
        }.onFailure {
            Log.e(TAG, "show dialog failed:$it")
        }
    }

    fun isShowing(): Boolean = dialog?.isShowing == true

    fun setTag(tag: String) {
        this.mTag = tag
    }

    fun setCanceledOnTouchOutside(cancel: Boolean) {
        this.mCanceledOnTouchOutside = cancel
    }

    fun setCanceledOnBackPressed(cancel: Boolean) {
        this.mCancelable = cancel
    }

    internal fun setViewCreate(action: (View, Dialog?) -> Unit) {
        this.mViewCreate = action
    }

    fun setBackPressed(action: () -> Unit) {
        this.mBackAction = action
    }

    fun setWith(width: Int) {
        this.mWidth = width
        this.mWidthFrom = SizeFrom.SPECIFY
    }

    /**
     * 设置宽度比例
     */
    fun setWidthScale(@FloatRange(from = 0.0, to = 1.0) scale: Float) {
        this.mWidthScale = scale
        this.mWidthFrom = SizeFrom.SCALE
    }

    /**
     * UI change
     */
    fun setConfigChange(action: (Configuration) -> Unit) = apply {
        this.configChange = action
    }

    fun doOnDismiss(action: () -> Unit) {
        mActionDialogDismiss = action
    }

    /**
     * 设置透明度
     * 在0.0f到1.0f之间
     * 1.0f完全不透明
     * 0.0f完全透明
     */
    fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        this.mAlpha = alpha
    }

    /**
     * 设置黑暗度（Dialog 窗口背景的黑暗度）
     */
    fun setDimAmount(@FloatRange(from = 0.0, to = 1.0) dmAmount: Float) {
        this.mDimAmount = dmAmount
    }

    /**
     * 设置Dialog 圆角
     */
    fun setRadius(radius: Float) {
        this.mRadius = radius
    }

    fun getWidthScale(): Float? {
        return mWidthScale
    }

    /**
     * 添加监听器
     */
    private fun addListener() {
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                mBackAction?.invoke()
            }
            false
        }
    }

    /**
     * 设置Dialog样式
     */
    private fun setDialogTheme() {
        val window = dialog?.window ?: return
        window.apply {
            findViewById<View>(R.id.design_bottom_sheet).apply {
                background = (ColorDrawable(Color.TRANSPARENT))
            }
            this.attributes = this.attributes.apply {
                this.width = getWith()
                mAlpha?.let { this.alpha = it }
                mDimAmount?.let { this.dimAmount = it }
            }
        }
    }

    private fun getWith(): Int {
        val scale = mWidthScale
        val width = mWidth
        return when {
            mWidthFrom == SizeFrom.SCALE && scale != null -> {
                (scale * mDisplay.width).toInt()
            }
            mWidthFrom == SizeFrom.SPECIFY && width != null -> {
                width
            }
            else -> ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    @IntDef(SizeFrom.SCALE, SizeFrom.SPECIFY)
    private annotation class SizeFrom {
        companion object {
            const val SCALE = 1
            const val SPECIFY = 2
        }
    }
}
