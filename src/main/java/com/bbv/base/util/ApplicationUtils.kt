package com.bbv.base.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import java.lang.Exception

/**
 * @Description:
 * @Author:
 * @Date: 2025/4/8 22:24
 */

object ApplicationUtils {
    fun isMainProcess(context: Context): Boolean {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfos = am.runningAppProcesses
            val mainProcessName = context.packageName
            val myPid = Process.myPid()
            for (info in processInfos) {
                if (info.pid == myPid && mainProcessName == info.processName) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    var INSTANCE: Application? = null
    @SuppressLint("PrivateApi")
    fun getAppContext(): Application?{
        if (INSTANCE != null){
            return INSTANCE
        }
        var app: Application? = null
        try {
            app = Class.forName("android.app.AppGlobals").getMethod("getInitialApplication")
                .invoke(null) as? Application
            if (app == null)
                throw IllegalStateException("Static initialization of Applications must be on main thread.")
        } catch (e: Exception) {
            try {
                app = Class.forName("android.app.ActivityThread").getMethod("currentApplication")
                    .invoke(null) as? Application
            } catch (e: Exception) {

            }
        } finally {
            INSTANCE = app
        }
        return INSTANCE
    }

}
