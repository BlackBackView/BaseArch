package com.bbv.base.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.regex.Pattern

/**
 * Created by yym on 2018/10/17.
 */
object DeviceUtil {
    /**
     * 得到设备剪切板里的邀请码
     */

    fun getClipboardInviteCode(context: Context): String? {
        val cm =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        if (null != cm) {
            val data = cm.primaryClip
            if (null != data) {
                val itemCount = data.itemCount
                for (i in 0 until itemCount) {
                    val item = data.getItemAt(i)
                    val itemText = item.text
                    if (itemText != null) {
                        val text = item.text.toString()
                        val pattern =
                            Pattern.compile("invite:([a-zA-Z0-9]{24})")
                        val matcher = pattern.matcher(text)
                        if (matcher.find()) {
                            return matcher.group(1)
                        }
                    }
                }
            }
        }
        return null
    }

    fun String.copyToClipboard(context: Context) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("code", this))
    }

    /**
     * 保存图片到设备文件系统
     */
    private val isExternalStorageMounted: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    fun Bitmap.saveImageToGallery(context: Context, dir: String, fileName: String): Uri? {
        if (isExternalStorageMounted) { // 首先保存图片
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName.removeSuffix(".jpg").removeSuffix(".jpeg"))
                values.put(MediaStore.Images.Media.DESCRIPTION, "This is an $dir image")
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/$dir")
                val external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val insertUri = context.contentResolver.insert(external, values)
                var os: OutputStream? = null
                try {
                    if (insertUri != null) {
                        os = context.contentResolver.openOutputStream(insertUri)
                    }
                    if (os != null) {
                        // 向os流写入数据（这里不能用PNG，会大很多且耗时很长）
                        val isSuccess = this.compress(Bitmap.CompressFormat.JPEG, 100, os)
                        os.flush()
                        os.close()
                        return if (!isSuccess){
                            null
                        }else{
                            insertUri
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }else{
                val storePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/$dir"
                val storeDirFile = File(storePath)
                try {
                    if (!storeDirFile.exists()){
                        storeDirFile.mkdir()
                    }
                    val file = if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")){
                        File(storePath, fileName)
                    }else{
                        File(storePath, "$fileName.jpg")
                    }
                    val fos = FileOutputStream(file)
                    //通过io流的方式来压缩保存图片
                    val isSuccess = this.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.flush()
                    fos.close()
                    if (!isSuccess){
                        return null
                    }
                    //保存图片后发送广播通知更新数据库
                    val uri = Uri.fromFile(file)
                    context.sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            uri
                        )
                    )
                    return uri
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    /**
     * 弹出键盘
     */
    fun showKeyBoard(view: View?) {
        if (view != null) {
            val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            view.requestFocus()
            inputMethodManager.showSoftInput(view, 0)
        }
    }

    /**
     * 收起键盘
     */
    fun hideKeyBoard(iBinder: IBinder?, context: Context?) {
        if (iBinder != null && context != null) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(iBinder, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * 是否支持沉浸式状态栏
     * @return
     */
    fun supportTranslucent(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT
    }

    fun getAppLabel(context: Context) = getAppLabelImpl(context)

    private fun getAppLabelImpl(context: Context): String {
        val label = context.packageManager.getApplicationLabel(context.applicationInfo)
        return label.toString()
    }
}
