package com.bbv.base.widget.tab

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.bbv.base.R

/**
 * 自定义 Tab 按钮控件，支持：
 * - 图标自动缩放（最大 32dp）
 * - 红点角标（支持文字）
 * - 文本模式 / 图标模式
 */
class SimpleMenuItem : AppCompatTextView {

    private var mSavedPaddingLeft = 0
    private var mIcon: Drawable? = null
    private var mTitle: String? = null
    private var mMaxIconSize = 0
    private var mReadPointTextSize = 0
    private var mReadPointTextPadding = 0
    private var mReadPoint: RedPoint? = null
    private var mPaintRedPointCircle: Paint? = null
    private var mPaintRedPointText: Paint? = null

    private fun dpToPx(dp: Int): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)

    private fun spToPx(sp: Int): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)

    fun setTitle(title: String?) {
        mTitle = title
        updateTextButtonVisibility()
    }

    fun setTitleBackground(drawable: Drawable?) {
        val hPad = dpToPx(TITLE_INSERT_BACKGROUND_PADDING_HORIZONTAL).toInt()
        val vPad = dpToPx(TITLE_INSERT_BACKGROUND_PADDING_VERTICAL).toInt()
        background = InsetDrawable(drawable, hPad, vPad, hPad, vPad)
        minHeight = dpToPx(MENU_MINI_TEXT_HEIGHT).toInt()
        minWidth = dpToPx(MENU_MINI_TEXT_WIDTH).toInt()
        setPadding(dpToPx(TITLE_PADDING_HORIZONTAL).toInt(), 0, dpToPx(TITLE_PADDING_HORIZONTAL).toInt(), 0)
    }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        mMaxIconSize = dpToPx(ICON_MAX_WIDTH).toInt()
        mReadPointTextSize = spToPx(RED_POINT_TEXT_SIZE).toInt()
        mReadPointTextPadding = spToPx(4).toInt()
        minWidth = dpToPx(MENU_MINI_ICON_WIDTH).toInt()
        minHeight = dpToPx(MENU_MINI_ICON_HEIGHT).toInt()
        gravity = Gravity.CENTER
        if (mPaintRedPointCircle == null) {
            mPaintRedPointCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED }
        }
        if (mPaintRedPointText == null) {
            mPaintRedPointText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                color = Color.WHITE
                textSize = spToPx(RED_POINT_TEXT_SIZE)
            }
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.SimpleMenuItem, defStyleAttr, 0)
        if (a.hasValue(R.styleable.SimpleMenuItem_icon)) {
            setIcon(a.getDrawable(R.styleable.SimpleMenuItem_icon))
        }
        a.recycle()
    }

    fun setIcon(icon: Drawable?) {
        mIcon = icon
        if (icon != null) {
            var width = icon.intrinsicWidth.toFloat()
            var height = icon.intrinsicHeight.toFloat()
            if (width > mMaxIconSize) {
                val scale = mMaxIconSize / width
                width = mMaxIconSize.toFloat()
                height *= scale
            }
            if (height > mMaxIconSize) {
                val scale = mMaxIconSize.toFloat() / height
                height = mMaxIconSize.toFloat()
                width *= scale
            }
            icon.setBounds(0, 0, width.toInt(), height.toInt())
        }
        setCompoundDrawables(icon, null, null, null)
        updateTextButtonVisibility()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        mSavedPaddingLeft = left
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val textVisible = hasText()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val oldMeasuredWidth = measuredWidth
        val targetWidth = if (widthMode == MeasureSpec.AT_MOST) minOf(widthSize, minWidth) else minWidth
        if (widthMode != MeasureSpec.EXACTLY && minWidth > 0 && oldMeasuredWidth < targetWidth) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY), heightMeasureSpec)
        }
        if (!textVisible && mIcon != null) {
            val w = measuredWidth
            val dw = mIcon!!.bounds.width()
            super.setPadding((w - dw) / 2, paddingTop, paddingRight, paddingBottom)
        }
    }

    private fun hasText(): Boolean = !TextUtils.isEmpty(text)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mReadPoint != null && mReadPoint!!.isShow) {
            val redPointRect = calculateRect()
            if (redPointRect.right > measuredWidth) {
                val size = redPointRect.right - measuredWidth
                redPointRect.left -= size
                redPointRect.right -= size
            }
            if (redPointRect.top < 0) {
                val size = Math.abs(redPointRect.top)
                redPointRect.top += size
                redPointRect.bottom += size
            }
            if (redPointRect.width() == redPointRect.height()) {
                canvas.drawCircle(redPointRect.centerX(), redPointRect.centerY(), redPointRect.height() / 2, mPaintRedPointCircle!!)
            } else {
                canvas.drawRoundRect(redPointRect, redPointRect.height() / 2, redPointRect.height() / 2, mPaintRedPointCircle!!)
            }
            val text = mReadPoint!!.text
            if (text != null) {
                val fontMetrics = mPaintRedPointText!!.fontMetrics
                val fontHeight = fontMetrics.descent + Math.abs(fontMetrics.ascent)
                val fontX = redPointRect.centerX()
                val fontY = redPointRect.top + redPointRect.height() / 2 + fontHeight / 2 - fontMetrics.descent
                canvas.drawText(text, fontX, fontY, mPaintRedPointText!!)
            }
        }
    }

    fun setReadPoint(readPoint: RedPoint?) {
        mReadPoint = readPoint
        requestLayout()
    }

    private fun calculateRect(): RectF {
        val isTextButton = isTextModel
        val pointCenter = if (isTextButton) textPoint else iconPoint
        return if (TextUtils.isEmpty(mReadPoint!!.text)) {
            getNoTextRectF(pointCenter)
        } else {
            if (mReadPoint!!.text.length <= 2) {
                val size = dpToPx(9)
                RectF(pointCenter.x - size, pointCenter.y - size, pointCenter.x + size, pointCenter.y + size)
            } else {
                val textWidth = mPaintRedPointText!!.measureText(mReadPoint!!.text)
                val textHeight = mPaintRedPointText!!.fontMetrics.bottom - mPaintRedPointText!!.fontMetrics.top
                RectF(
                    pointCenter.x - textWidth / 2 - mReadPointTextPadding,
                    pointCenter.y - textHeight / 2 - mReadPointTextPadding / 3,
                    pointCenter.x + textWidth / 2 + mReadPointTextPadding,
                    pointCenter.y + textHeight / 2 + mReadPointTextPadding / 3
                )
            }
        }
    }

    private val textPoint: Point
        get() = Point(measuredWidth - paddingBottom - 20, paddingTop + 20)

    private val iconPoint: Point
        get() {
            val rect = Rect(0, 0, measuredWidth, measuredHeight)
            val b = (Math.acos(Math.sin(Math.toRadians(45.0))) * mIcon!!.bounds.width() / 2).toInt()
            val a = (Math.acos(Math.cos(Math.toRadians(45.0))) * mIcon!!.bounds.width() / 2).toInt()
            return Point(rect.centerX() + b * 2, rect.centerY() - a)
        }

    private fun getNoTextRectF(point: Point): RectF {
        val noTextPointSize = dpToPx(4).toInt()
        return RectF(
            (point.x - noTextPointSize).toFloat(),
            (point.y - noTextPointSize).toFloat(),
            (point.x + noTextPointSize).toFloat(),
            (point.y + noTextPointSize).toFloat()
        )
    }

    private fun updateTextButtonVisibility() {
        val textVisible = isTextModel
        text = if (textVisible) mTitle else null
    }

    private val isTextModel: Boolean get() = mIcon == null

    class RedPoint(val isShow: Boolean, val text: String)

    companion object {
        const val ICON_MAX_WIDTH = 32
        const val ICON_MAX_HEIGHT = 32
        const val TITLE_INSERT_BACKGROUND_PADDING_HORIZONTAL = 8
        const val TITLE_INSERT_BACKGROUND_PADDING_VERTICAL = 8
        const val TITLE_PADDING_HORIZONTAL = 15
        const val TITLE_PADDING_VERTICAL = 15
        const val MENU_MINI_ICON_WIDTH = 48
        const val MENU_MINI_ICON_HEIGHT = 48
        const val MENU_MINI_TEXT_WIDTH = 72
        const val MENU_MINI_TEXT_HEIGHT = 48
        const val RED_POINT_TEXT_SIZE = 12
    }
}
