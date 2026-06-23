package com.bbv.base.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

/**
 * @Description:
 * @Author: yang liv
 * @Date: 2025/4/22 14:09
 */
class FileDownload {

    /**
     * 文件下载到指定路径
     *
     * @param urlStr   链接
     * @param savePath 保存路径
     * @param filename 文件名
     * @throws Exception
     */
    @Throws(IOException::class)
    suspend fun download(urlStr: String?, savePath: String?, filename: String, onError: (() -> Unit)? = null, onSuccess: (() -> Unit)? = null) {
        withContext(Dispatchers.IO) {
            try {
                //构造URL
                val url = URL(urlStr)
                val con = url.openConnection()
                con.connectTimeout = 20 * 1000
                //文件路径不存在 则创建
                val folder = File(savePath)
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                //jdk 7 流自动关闭
                con.getInputStream().use { `in` ->
                    FileOutputStream(folder.path + File.separator + filename).use { out ->
                        //创建缓冲区
                        val buff = ByteArray(1024)
                        var n: Int
                        // 开始读取
                        while ((`in`.read(buff).also { n = it }) >= 0) {
                            out.write(buff, 0, n)
                        }
                    }
                }
                // 下载成功回调
                onSuccess?.invoke()
            } catch (e: Exception) {
                onError?.invoke()
                e.printStackTrace()
            }
        }
    }


}
