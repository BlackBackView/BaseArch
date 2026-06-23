package com.bbv.base.util.ext

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bbv.base.R
import java.util.concurrent.TimeUnit

/**
 * Glide 图片加载的 ImageView 扩展函数
 *
 * 使用示例：
 * ```kotlin
 * // 加载普通图片
 * imageView.loadImage("https://example.com/image.jpg")
 *
 * // 加载圆形头像
 * imageView.loadCircleImage("https://example.com/avatar.jpg")
 *
 * // 加载圆角图片
 * imageView.loadRoundedCornerImage("https://example.com/image.jpg", 8f)
 *
 * // 加载视频缩略图
 * imageView.loadVideo("https://example.com/video.mp4")
 * ```
 */

private val DEFAULT_PLACEHOLDER = R.drawable.default_img

/**
 * 加载圆形图片（默认占位图）
 */
fun ImageView.loadCircleImage(url: String) {
    Glide.with(this).load(url).apply(
        RequestOptions.circleCropTransform().placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载圆形图片（自定义占位图）
 */
fun ImageView.loadCircleImage(url: String, @DrawableRes errorInt: Int) {
    Glide.with(this).load(url).apply(
        RequestOptions.circleCropTransform().placeholder(errorInt).error(errorInt)
    ).into(this)
}

/**
 * 加载圆形图片（支持 Any 类型源）
 */
fun ImageView.loadCircleImage(any: Any) {
    Glide.with(this).load(any).apply(
        RequestOptions.circleCropTransform().placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载圆形图片（不缓存）
 */
fun ImageView.loadCircleImageNotCache(any: Any) {
    Glide.with(this).load(any).apply(
        RequestOptions.circleCropTransform()
            .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载圆角图片（String 类型 url）
 * @param roundingRadius dp 值圆角半径
 */
fun ImageView.loadRoundedCornerImage(url: String, roundingRadius: Float) {
    Glide.with(this).load(url).apply(
        RequestOptions.bitmapTransform(
            MultiTransformation<Bitmap>(
                CenterCrop(), RoundedCorners(dp2px(roundingRadius))
            )
        ).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载圆角图片（Any 类型源）
 * @param roundingRadius dp 值圆角半径
 */
fun ImageView.loadRoundedCornerImage(url: Any?, roundingRadius: Float) {
    Glide.with(this).load(url).apply(
        RequestOptions.bitmapTransform(
            MultiTransformation<Bitmap>(
                CenterCrop(), RoundedCorners(dp2px(roundingRadius))
            )
        ).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载普通图片（带默认占位图）
 */
fun ImageView.loadImage(url: Any?) {
    val any = url ?: ""
    Glide.with(this).load(any).apply(
        RequestOptions().placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载普通图片（自定义占位图）
 */
fun ImageView.loadImage(url: Any?, @DrawableRes errorInt: Int) {
    Glide.with(this).load(url).apply(
        RequestOptions().placeholder(errorInt).error(errorInt)
    ).into(this)
}

/**
 * 加载普通图片（Uri 类型）
 */
fun ImageView.loadImage(uri: Uri) {
    Glide.with(this).load(uri).apply(
        RequestOptions().placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载图片（无默认占位图，不缓存）
 */
fun ImageView.loadImageNoCache(url: Any?) {
    val any = url ?: ""
    Glide.with(this).load(any).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(this)
}

/**
 * 加载图片（无默认占位图）
 */
fun ImageView.loadImageNoDefault(url: Any?) {
    val any = url ?: ""
    Glide.with(this).load(any).into(this)
}

/**
 * 加载视频缩略图（默认占位图）
 */
fun ImageView.loadVideo(url: Any?) {
    val any = url ?: ""
    Glide.with(this).load(any).apply(
        RequestOptions().placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
            .frame(TimeUnit.SECONDS.toMicros(1))
    ).into(this)
}

/**
 * 加载圆角视频缩略图
 */
fun ImageView.loadRoundedCornerVideo(url: Any?, roundingRadius: Float) {
    val any = url ?: ""
    Glide.with(this).load(any).apply(
        RequestOptions.bitmapTransform(
            MultiTransformation<Bitmap>(
                CenterCrop(), RoundedCorners(dp2px(roundingRadius))
            )
        ).placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER).frame(TimeUnit.SECONDS.toMicros(1))
    ).into(this)
}

/**
 * 加载头像（带默认用户占位图）
 */
fun ImageView.loadAvatar(uri: Any?) {
    Glide.with(this).load(uri).apply(
        RequestOptions().placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
    ).into(this)
}

/**
 * 加载图片并按原始比例自适应宽高
 */
fun ImageView.loadImageAspectRatio(url: Any?) {
    Glide.with(this).asBitmap().load(url ?: "").apply(
        RequestOptions().placeholder(DEFAULT_PLACEHOLDER).error(DEFAULT_PLACEHOLDER)
    ).into(object : CustomTarget<Bitmap>() {
        override fun onLoadCleared(placeholder: Drawable?) {}

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val bitHeight = resource.height
            val bitWidth = resource.width
            val w = this@loadImageAspectRatio.width
            val h = ((bitHeight * 1f) / bitWidth) * w
            this@loadImageAspectRatio.layoutParams = layoutParams.apply {
                width = w.toInt()
                height = h.toInt()
            }
            this@loadImageAspectRatio.setImageBitmap(resource)
        }
    })
}

/**
 * dp 转 px
 */
private fun ImageView.dp2px(dpValue: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpValue,
        context.resources.displayMetrics
    ).toInt()
}
