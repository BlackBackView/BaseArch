package com.bbv.base.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import java.io.Serializable

/**
 * Intent 工具类
 *
 * 提供：
 * - [obtainArg] 键值对序列 → Bundle（支持 String/Boolean/Int/Parcelable/Serializable/Enum/List）
 * - [success] / [fail] 设置 Activity Result 并 finish
 */
object IntentUtils {

    /**
     * 将键值对序列转换为 Bundle
     *
     * 用法：`obtainArg("key1", value1, "key2", value2)`
     * 支持类型：CharSequence, Boolean, Int, Parcelable, Serializable, Enum, List<String>, List<Parcelable>
     */
    fun obtainArg(vararg args: Any): Bundle {
        val b = Bundle()
        val size = args.size
        var key: String
        var value: Any
        var i = 0
        while (i < size) {
            key = args[i].toString()
            value = args[i + 1]
            when (value) {
                is CharSequence -> b.putString(key, value.toString())
                is Boolean -> b.putBoolean(key, value)
                is Int -> b.putInt(key, value)
                is Parcelable -> b.putParcelable(key, value)
                is Serializable -> b.putSerializable(key, value)
                is Enum<*> -> b.putSerializable(key, value as Serializable)
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val valList = value as List<Any?>
                    if (valList.isNotEmpty()) {
                        when (valList[0]) {
                            is String -> b.putStringArrayList(key, value as ArrayList<String?>)
                            is Parcelable -> b.putParcelableArrayList(
                                key,
                                value as ArrayList<out Parcelable?>
                            )
                        }
                    }
                }
                else -> Log.d("IntentUtils", "obtainArg: $value is not supported")
            }
            i += 2
        }
        return b
    }

    /**
     * 设置 Activity Result 并 finish
     * @param isSuccess true=RESULT_OK, false=RESULT_CANCELED
     * @param contextObj Fragment / Activity
     * @param bundle 需要回传的数据
     */
    fun result(isSuccess: Boolean, contextObj: Any?, bundle: Bundle?) {
        if (contextObj == null) return
        val aty: Activity? = when (contextObj) {
            is Fragment -> contextObj.activity
            is Activity -> contextObj
            else -> null
        }
        if (aty == null || aty.isFinishing) {
            Log.d("IntentUtils", "result: activity is null or finishing")
            return
        }
        val code = if (isSuccess) Activity.RESULT_OK else Activity.RESULT_CANCELED
        if (bundle != null) {
            aty.setResult(code, Intent().putExtras(bundle))
        } else {
            aty.setResult(code)
        }
        aty.finish()
    }

    /** 成功并回传数据 */
    fun success(atyOrFrag: Any?, bundle: Bundle?) = result(true, atyOrFrag, bundle)

    /** 失败并回传数据 */
    fun fail(atyOrFrag: Any?, bundle: Bundle?) = result(false, atyOrFrag, bundle)

    /** 成功无回传 */
    fun success(atyOrFrag: Any?) = result(true, atyOrFrag, null)

    /** 失败无回传 */
    fun fail(atyOrFrag: Any?) = result(false, atyOrFrag, null)
}
