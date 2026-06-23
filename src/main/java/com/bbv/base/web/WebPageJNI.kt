package com.bbv.base.web

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * WebView JS Bridge
 * 通过 @JavascriptInterface 暴露给前端调用
 */
class WebPageJNI(private val activity: FragmentActivity) {

    @Keep
    @JavascriptInterface
    fun close() {
        activity.finish()
    }

    @Keep
    @JavascriptInterface
    fun getStatusHeight(): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            activity.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    @Keep
    @JavascriptInterface
    fun saveImageToGallery(url: String) {
        Glide.with(activity)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveBitmapToGallery(activity, resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Toast.makeText(activity, "图片保存失败", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        try {
            val filename = "webview_${System.currentTimeMillis()}.jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, values, null, null)
                }
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )
                if (!dir.exists()) dir.mkdirs()
                val file = java.io.File(dir, filename)
                java.io.FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                context.sendBroadcast(
                    android.content.Intent(
                        android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        android.net.Uri.fromFile(file)
                    )
                )
            }
            Toast.makeText(context, "图片已保存至相册", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "图片保存失败", Toast.LENGTH_SHORT).show()
        }
    }
}
