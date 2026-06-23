package com.bbv.base.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bbv.base.R
import com.bbv.base.dialog.util.radius

abstract class BaseDialog : DialogFragment() {

    companion object {
        private const val TAG = "BaseDialog"
    }

    private lateinit var mDisplay: Display

    private var mTag: String? = null

    private var mCanceledOnTouchOutside: Boolean? = null

    private var mCancelable: Boolean? = null

    private var mViewCreate: ((View, Dialog?) -> Unit)? = null

    private var mBackAction: (() -> Unit)? = null

    private var mActionDialogDismiss: (() -> Unit)? = null

    private var mWidth: Int? = null

    private var mHeight: Int? = null

    private var mAlpha: Float? = null

    private var mDimAmount: Float? = null

    private var mGravity: Int? = null

    private var mWidthScale: Float? = null

    private var mHeightScale: Float? = null

    private var mRadius: Float? = null

    private var mStyleAnim: Int? = null

    private var configChange: ((Configuration) -> Unit)? = null

    /**
     * 判断是根据比例设置宽高还是依据大小设置宽高
     * @see SizeFrom
     */
    private var mWidthFrom: Int? = null

    /**
     * 判断是根据比例设置宽高还是依据大小设置宽高
     * @see SizeFrom
     */
    private var mHeightFrom: Int? = null

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
        mStyleAnim?.let { dialog?.window?.setWindowAnimations(it) }
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

    fun setHeight(height: Int) {
        this.mHeight = height
        this.mHeightFrom = SizeFrom.SPECIFY
    }

    fun setSize(width: Int, height: Int) {
        setWith(width)
        setHeight(height)
    }

    /**
     * 设置宽度比例
     */
    fun setWidthScale(@FloatRange(from = 0.0, to = 1.0) scale: Float) {
        this.mWidthScale = scale
        this.mWidthFrom = SizeFrom.SCALE
    }

    /**
     * 设置高度比例
     */
    fun setHeightScale(@FloatRange(from = 0.0, to = 1.0) scale: Float) {
        this.mHeightScale = scale
        this.mHeightFrom = SizeFrom.SCALE
    }

    fun setScale(
        @FloatRange(from = 0.0, to = 1.0) widthScale: Float,
        @FloatRange(from = 0.0, to = 1.0) heightScale: Float
    ) {
        setHeightScale(heightScale)
        setWidthScale(widthScale)
    }

    fun setGravity(gravity: Int) {
        this.mGravity = gravity
    }

    fun doOnDismiss(action: () -> Unit) {
        mActionDialogDismiss = action
    }

    /**
     * 设置动画
     */
    fun setWindowAnimations(resId: Int) {
        mStyleAnim = resId
    }

    /**
     * 底部弹出动画
     */
    fun setButtonAnimation() {
        setWindowAnimations(R.style.SheetDialogAnimation)
        setGravity(Gravity.BOTTOM)
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
     * UI change
     */
    fun setConfigChange(action: (Configuration) -> Unit) = apply {
        this.configChange = action
    }

    /**
     * 设置Dialog 圆角
     */
    fun setRadius(radius: Float) {
        this.mRadius = radius
    }

    fun getGravity(): Int? {
        return mGravity
    }

    fun getWidthScale(): Float? {
        return mWidthScale
    }

    fun getWidthFrom(): Int? {
        return mWidthFrom
    }

    private fun addListener() {
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                mBackAction?.invoke()
            }
            false
        }
    }

    private fun setDialogTheme() {
        val window = dialog?.window ?: return
        window.apply {
            this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            this.attributes = this.attributes.apply {
                this.height = getHeight()
                this.width = getWith()
                mGravity?.let { this.gravity = it }
                mAlpha?.let { this.alpha = it }
                mDimAmount?.let { this.dimAmount = it }
            }
        }
    }

    fun getHeight(): Int {
        val scale = mHeightScale
        val height = mHeight
        return when {
            mHeightFrom == SizeFrom.SCALE && scale != null -> {
                (scale * mDisplay.height).toInt()
            }
            mHeightFrom == SizeFrom.SPECIFY && height != null -> {
                height
            }
            else -> ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    fun getWith(): Int {
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
