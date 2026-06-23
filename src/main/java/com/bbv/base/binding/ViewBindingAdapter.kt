package com.bbv.base.binding

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toColorInt
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bumptech.glide.Glide
import com.bbv.base.util.toDateStr
import java.io.File

// ==================== 工具扩展函数 ====================

private fun Float.dp2px(): Float {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f)
}

private fun Int.dp2px(): Int {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

// ==================== 防抖点击 ====================

/**
 * 防抖点击（传统 OnClickListener）
 */
@BindingAdapter("android:onClick", "interval", requireAll = false)
fun View.throttleClick(callback: View.OnClickListener?, interval: Int?) {
    callback?.let {
        setOnClickListener(ThrottleClickHandler(callBack = callback, interval = interval))
    }
}

/**
 * 防抖点击（lambda 方式，移除 onClick 的 View 参数）
 */
@BindingAdapter("android:onBindClick", "interval", requireAll = false)
fun bindThrottleClick(view: View, callback: (() -> Unit)?, interval: Int?) {
    callback?.let {
        view.setOnClickListener(BindThrottleClickHandler(callBack = callback, interval = interval))
    }
}

// ==================== 显隐控制 ====================

/**
 * 是否隐藏（GONE）
 */
@BindingAdapter("gone", requireAll = false)
fun View.gone(gone: Boolean) {
    this.visibility = if (gone) View.GONE else View.VISIBLE
}

/**
 * 是否可见（INVISIBLE）
 */
@BindingAdapter("visible")
fun View.visible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter(value = ["isShow", "showAnim", "hideAnim", "duration"], requireAll = false)
fun isShow(view: View, isShow: Boolean, showAnim: Animator? = null, hideAnim: Animator? = null, duration: Long? = null) {
    view.toggleVisibleWithAnim(isShow, showAnim, hideAnim, duration, View.GONE)
}

@BindingAdapter(value = ["isVisible", "showAnim", "hideAnim", "duration"], requireAll = false)
fun isVisible(view: View, isVisible: Boolean, showAnim: Animator? = null, hideAnim: Animator? = null, duration: Long? = null) {
    view.toggleVisibleWithAnim(isVisible, showAnim, hideAnim, duration, View.INVISIBLE)
}

private fun View.toggleVisibleWithAnim(isShow: Boolean, showAnim: Animator?, hideAnim: Animator?, duration: Long?, goneOrInvisible: Int) {
    if (showAnim == null || hideAnim == null) {
        visibility = if (isShow) View.VISIBLE else goneOrInvisible
    } else {
        if (isShow && visibility == goneOrInvisible) {
            visibility = View.VISIBLE
            showAnim.setTarget(this)
            showAnim.duration = duration ?: 300
            showAnim.start()
        } else if (!isShow && visibility == View.VISIBLE) {
            hideAnim.setTarget(this)
            hideAnim.duration = duration ?: 300
            hideAnim.doOnEnd {
                visibility = goneOrInvisible
            }
            hideAnim.start()
        }
    }
}

// ==================== 背景设置 ====================

/**
 * 设置背景资源
 */
@BindingAdapter("bg_res", requireAll = false)
fun View.setBgRes(@DrawableRes res: Int) {
    setBackgroundResource(res)
}

/**
 * 设置背景颜色
 */
@BindingAdapter("bg_color", requireAll = false)
fun View.setBgColor(res: Any) {
    if (res is String) {
        setBackgroundColor(res.toColorInt())
    } else if (res is Int) {
        setBackgroundColor(res)
    }
}

/**
 * 画边框方法
 */
@BindingAdapter("stroke_bg_color", "stroke_radius", "stroke_width", "stroke_color", requireAll = false)
fun View.stroke(color: Any? = null, radius: Float, stockWidth: Int = 1, stockColor: Any) {
    this.background = GradientDrawable().apply {
        cornerRadius = radius.dp2px()
        color?.let {
            if (color is String) {
                setColor(color.toColorInt())
            } else if (color is Int) {
                setColor(color)
            }
        }
        if (stockColor is String) {
            setStroke(stockWidth.dp2px(), stockColor.toColorInt())
        } else if (stockColor is Int) {
            setStroke(stockWidth.dp2px(), stockColor)
        }
    }
}

/**
 * 设置渐变色
 */
fun View.setBgColors(
    colors: IntArray,
    orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM,
    radius: Float = 0f
) {
    background = GradientDrawable(orientation, colors).apply {
        cornerRadius = radius
    }
}

// ==================== 圆角 ====================

/**
 * 统一圆角
 */
@BindingAdapter("radius")
fun View.radius(radius: Float) {
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius.dp2px())
        }
    }
    clipToOutline = true
}

/**
 * 单独设置四个角的圆角
 */
@BindingAdapter("radius_color", "radius_tl", "radius_tr", "radius_bl", "radius_br", requireAll = false)
fun View.radiusAll(color: Any, tl: Float = 0f, tr: Float = 0f, bl: Float = 0f, br: Float = 0f) {
    this.background = GradientDrawable().apply {
        cornerRadii = floatArrayOf(
            tl.dp2px(), tl.dp2px(),
            tr.dp2px(), tr.dp2px(),
            br.dp2px(), br.dp2px(),
            bl.dp2px(), bl.dp2px(),
        )
        color.let {
            if (color is Int) {
                setColor(color)
            } else if (color is String) {
                setColor(color.toColorInt())
            }
        }
    }
}

// ==================== 动画 ====================

/**
 * 隐藏view，带有渐隐动画效果
 */
fun View.goneAlphaAnimation(duration: Long = 500L) {
    this.animate().alpha(0f).setDuration(duration).withEndAction {
        this.visibility = View.GONE
    }.start()
}

fun View.goneAlphaWithHeightAnimation(duration: Long = 500L) {
    if (this.isGone) return
    val currentFull = height
    this.animate().setDuration(duration).alpha(0f).setUpdateListener {
        val value = it.animatedValue as Float
        val params = this.layoutParams.apply {
            height = ((1 - value) * currentFull).toInt()
        }
        this.layoutParams = params
    }.withEndAction {
        this.visibility = View.GONE
    }.start()
}

fun View.visibleAlphaWithHeightAnimation(height: Int, duration: Long = 500L) {
    if (this.isVisible) return
    this.animate().setDuration(duration).alpha(1f).setUpdateListener {
        val value = it.animatedValue as Float
        val params = this.layoutParams.apply {
            this.height = ((value) * height).toInt()
        }
        this.layoutParams = params
    }.withStartAction {
        this.visibility = View.VISIBLE
        this.alpha = 0f
    }.start()
}

/**
 * 占位隐藏view，带有渐隐动画效果
 */
fun View.invisibleAlphaAnimation(duration: Long = 500L) {
    this.animate().alpha(0f).setDuration(duration).withEndAction {
        this.visibility = View.INVISIBLE
    }.start()
}

/**
 * 占位显示view，带有渐现动画效果
 */
fun View.visibleAlphaAnimation(duration: Long = 500L) {
    this.alpha = 0f
    this.animate().alpha(1f).setDuration(duration).withStartAction {
        this.visibility = View.VISIBLE
    }.start()
}

// ==================== 触摸动效 ====================

@BindingAdapter("bindTouchScale")
fun bindTouchScale(view: View, scale: Float?) {
    if (scale == null) return
    view.setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            v.clearAnimation()
            v.startAnimation(ScaleAnimation(1f, scale, 1f, scale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                duration = 200
                repeatCount = 1
                repeatMode = Animation.REVERSE
            })
        }
        false
    }
}

@BindingAdapter("bindTouchAlpha")
fun bindTouchAlpha(view: View, alpha: Float?) {
    if (alpha == null) return
    view.setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            v.clearAnimation()
            v.startAnimation(AlphaAnimation(1f, alpha).apply {
                duration = 200
                repeatCount = 1
                repeatMode = Animation.REVERSE
            })
        }
        false
    }
}

/**
 * 控件放大缩小（闪烁效果）
 */
@BindingAdapter("bindFlicker")
fun bindFlicker(view: View, time: Long) {
    val valueAnimator = ValueAnimator.ofFloat(0.9f, 1.1f).apply {
        duration = time
        interpolator = AnticipateOvershootInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
    }
    valueAnimator.addUpdateListener {
        view.scaleX = it.animatedValue as Float
        view.scaleY = it.animatedValue as Float
    }
    valueAnimator.start()
}

/**
 * 旋转动画
 */
@BindingAdapter("bindRotate")
fun bindRotate(view: View, time: Long) {
    val rotateAnimation = RotateAnimation(
        0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )
    rotateAnimation.duration = time
    rotateAnimation.repeatCount = Animation.INFINITE
    rotateAnimation.interpolator = LinearInterpolator()
    view.clearAnimation()
    view.startAnimation(rotateAnimation)
}

/**
 * 点击动效（点击缩小）
 */
@SuppressLint("ClickableViewAccessibility")
@BindingAdapter("bindClickScale")
fun bindClickScale(view: View, scale: Float) {
    view.setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            v.clearAnimation()
            v.startAnimation(
                ScaleAnimation(
                    1f, scale,
                    1f, scale,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 200
                    repeatCount = 1
                    repeatMode = Animation.REVERSE
                })
        }
        false
    }
}

// ==================== 日期转换 ====================

/**
 * 时间戳转日期字符串
 */
@BindingAdapter(value = ["bindTimestampToString", "bindTimestampPattern"], requireAll = false)
fun bindTimestampToString(textView: TextView, timestamp: Long, pattern: String?) {
    textView.apply {
        text = timestamp.toDateStr(pattern ?: "yyyy-MM-dd hh:mm:ss")
    }
}

// ==================== ImageView 图片加载 ====================

@BindingAdapter("bindDrawableByNameToImageView")
fun bindDrawableByNameToImageView(imageView: ImageView, drawableName: String?) {
    if (drawableName == null) {
        imageView.setImageDrawable(null)
        return
    }
    val drawableId = imageView.context.resources.getIdentifier(drawableName, "drawable", imageView.context.packageName)
    val drawable = AppCompatResources.getDrawable(imageView.context, drawableId)
    imageView.setImageDrawable(drawable)
}

@BindingAdapter("bindBackgroundDrawableToView")
fun bindBackgroundDrawableToView(view: View, drawable: Int?) {
    drawable?.let {
        view.background = AppCompatResources.getDrawable(view.context, drawable)
    }
}

@BindingAdapter("bindDrawableToImage")
fun bindDrawableToImage(imageView: ImageView, drawable: Int?) {
    drawable?.let {
        imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, drawable))
    }
}

@BindingAdapter("bindDrawableToImage")
fun bindDrawableToImage(imageView: ImageView, drawable: Drawable?) {
    drawable?.let {
        Glide.with(imageView).load(it).into(imageView)
    }
}

@BindingAdapter(value = ["bindSrcToImage", "bindSrcDefaultToImage", "bindSrcErrorToImage"], requireAll = false)
fun bindSrcToImage(imageView: ImageView, url: String?, defaultDrawable: Drawable? = null, errorDrawable: Drawable? = null) {
    url?.let {
        Glide.with(imageView).load(it).placeholder(defaultDrawable).error(errorDrawable).into(imageView)
    } ?: run {
        imageView.setImageDrawable(defaultDrawable)
    }
}

@BindingAdapter("bindFilePathOrUriToImage")
fun bindFilePathOrUriToImage(imageView: ImageView, filePath: String?) {
    filePath?.let {
        val uri = if (it.contains("://")) {
            Uri.parse(it)
        } else {
            Uri.fromFile(File(it))
        }
        Glide.with(imageView).load(uri).into(imageView)
    }
}

// ==================== TextView 设置 ====================

@BindingAdapter("bindTextPaintFlags")
fun bindTextPaintFlags(textView: TextView, flags: Int) {
    textView.apply {
        paint.flags = flags
    }
}

@BindingAdapter("bindTextColor")
fun bindTextColor(textView: TextView, colorStr: String?) {
    colorStr?.let {
        textView.setTextColor(Color.parseColor(colorStr))
    }
}

@BindingAdapter(value = ["bindCompoundDrawableStart", "bindCompoundDrawableTop", "bindCompoundDrawableEnd", "bindCompoundDrawableBottom"], requireAll = false)
fun bindCompoundDrawables(textView: TextView, drawableStart: Drawable? = null, drawableTop: Drawable? = null, drawableEnd: Drawable? = null, drawableBottom: Drawable? = null) {
    textView.setCompoundDrawablesWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom)
}

// ==================== Tint 颜色 ====================

@BindingAdapter("bindTintToImage")
fun bindTintToImage(imageView: ImageView, color: Int?) {
    color?.let {
        imageView.imageTintList = ColorStateList.valueOf(color)
    }
}

@BindingAdapter("bindTintToTextViewDrawable")
fun bindTintToTextViewDrawable(textView: TextView, color: Int?) {
    color?.let {
        TextViewCompat.setCompoundDrawableTintList(textView, ColorStateList.valueOf(color))
    }
}

// ==================== 双向绑定 ====================

/**
 * CompoundButton系列控件(CheckBox、Switch、ToggleButton等)双向绑定isChecked
 */
@BindingAdapter("isCheck")
fun setCheck(view: CompoundButton, isCheck: Boolean?) {
    if (isCheck != null && view.isChecked != isCheck)
        view.isChecked = isCheck
}

@InverseBindingAdapter(attribute = "isCheck", event = "checkAttrChanged")
fun getCheck(view: CompoundButton?): Boolean {
    return view?.isChecked ?: false
}

@BindingAdapter("checkAttrChanged")
fun setCompoundCheckListener(view: CompoundButton, checkAttrChanged: InverseBindingListener) {
    view.setOnCheckedChangeListener { _, _ ->
        checkAttrChanged.onChange()
    }
}

/**
 * 绑定SeekBar进度
 */
@BindingAdapter("process")
fun setProcess(seekBar: SeekBar, process: Int?) {
    if (process != null && seekBar.progress != process)
        seekBar.progress = process
}

@InverseBindingAdapter(attribute = "process", event = "processAttrChanged")
fun getProcess(seekBar: SeekBar?): Int {
    return seekBar?.progress ?: 0
}

@BindingAdapter("processAttrChanged")
fun setProcessAttrChanged(seekBar: SeekBar, onSeekBarProcessChangeListener: InverseBindingListener) {
    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            onSeekBarProcessChangeListener.onChange()
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }
    })
}

// ==================== 布局属性 ====================

@BindingAdapter(value = ["bindMarginStart", "bindMarginEnd", "bindMarginTop", "bindMarginBottom"], requireAll = false)
fun bindMargin(view: View, marginStart: Int?, marginEnd: Int?, marginTop: Int?, marginBottom: Int?) {
    val density = view.context.resources.displayMetrics.density
    view.layoutParams = view.layoutParams.apply {
        (this as? ViewGroup.MarginLayoutParams)?.setMargins(
            ((marginStart ?: 0) * density + 0.5f).toInt(),
            ((marginTop ?: 0) * density + 0.5f).toInt(),
            ((marginEnd ?: 0) * density + 0.5f).toInt(),
            ((marginBottom ?: 0) * density + 0.5f).toInt()
        )
    }
}

@BindingAdapter(value = ["bindPaddingStart", "bindPaddingEnd", "bindPaddingTop", "bindPaddingBottom"], requireAll = false)
fun bindPadding(view: View, paddingStart: Int?, paddingEnd: Int?, paddingTop: Int?, paddingBottom: Int?) {
    val density = view.context.resources.displayMetrics.density
    view.setPadding(
        ((paddingStart ?: 0) * density + 0.5f).toInt(),
        ((paddingTop ?: 0) * density + 0.5f).toInt(),
        ((paddingEnd ?: 0) * density + 0.5f).toInt(),
        ((paddingBottom ?: 0) * density + 0.5f).toInt()
    )
}
