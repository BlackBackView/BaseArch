package com.bbv.base.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

private const val BUNDLE_OPEN_WITHOUT_ANIM = "open_without_anim"
private const val BUNDLE_FINISH_WITHOUT_ANIM = "finish_without_anim"

/**
 * 流式 Intent 启动工具
 *
 * 核心能力：
 * 1. **自动为 Fragment 创建宿主 Activity** — 调用 [startFragment] 时自动通过 [FragmentContainActivity] 包装
 * 2. **流式传参** — [withData] 支持键值对序列或 Bundle
 * 3. **页面结果回调** — [withActivityResultCallback] 直接在跳转处处理返回结果
 *
 * ## 使用方式
 *
 * ```kotlin
 * // 1. 启动 Fragment（自动包裹 Activity）
 * IntentStarter.create(this)
 *     .withData("orderId", orderId)
 *     .startFragment(OrderDetailFragment::class.java)
 *
 * // 2. 启动 Activity 并获取返回结果
 * IntentStarter.create(this)
 *     .withData("key", value)
 *     .withActivityResultCallback { result ->
 *         if (result.resultCode == Activity.RESULT_OK) {
 *             val data = result.data?.getStringExtra("resultKey")
 *             // 处理返回结果
 *         }
 *     }
 *     .startActivity(SomeActivity::class.java)
 *
 * // 3. 启动 Fragment 并获取返回结果（直接在跳转处连接业务逻辑）
 * IntentStarter.create(this)
 *     .withData("type", type)
 *     .withActivityResultCallback { result ->
 *         // 在这里直接处理返回的业务逻辑
 *         val resultData = result.data?.getStringExtra("resultKey")
 *         updateUI(resultData)
 *     }
 *     .startFragment(PickerFragment::class.java)
 * ```
 */
class IntentStarter(private val mContentObj: Any) {

    private var mBundle: Bundle? = null
    private var mRequestCode = -1
    private var mActivityFlag = 0
    private var mOpenWithoutAnim: Boolean = false
    private var mFinishWithoutAnim: Boolean = false
    private var mActivityResultCallback: ActivityResultCallback<ActivityResult>? = null
    private var mActivityResultOptions: ActivityOptionsCompat? = null

    // ============ 配置方法 ============

    fun openWithoutAnim(without: Boolean? = true): IntentStarter {
        mOpenWithoutAnim = without ?: true
        return this
    }

    fun finishWithoutAnim(without: Boolean? = false): IntentStarter {
        mFinishWithoutAnim = without ?: true
        return this
    }

    /** 通过 Bundle 传参 */
    fun withData(bundle: Bundle?): IntentStarter {
        mBundle = bundle
        return this
    }

    /**
     * 通过键值对序列传参
     *
     * 用法：`withData("key1", value1, "key2", value2)`
     */
    fun withData(vararg data: Any?): IntentStarter {
        @Suppress("UNCHECKED_CAST")
        val bundle = IntentUtils.obtainArg(*data as Array<out Any>)
        if (mBundle != null) {
            mBundle!!.putAll(bundle)
        } else {
            mBundle = bundle
        }
        return this
    }

    fun withRequestCode(requestCode: Int): IntentStarter {
        mRequestCode = requestCode
        return this
    }

    fun withFlag(activityFlag: Int): IntentStarter {
        mActivityFlag = activityFlag
        return this
    }

    /**
     * 设置 Activity Result 回调
     *
     * 启动 Activity/Fragment 后，在返回时通过此回调接收结果数据。
     * 无需在 onActivityResult 中处理，业务逻辑可以直接写在这里。
     */
    fun withActivityResultCallback(
        activityResultCallback: ActivityResultCallback<ActivityResult>?
    ): IntentStarter {
        mActivityResultCallback = activityResultCallback
        return this
    }

    fun withActivityResultOptions(activityResultOptions: ActivityOptionsCompat?): IntentStarter {
        mActivityResultOptions = activityResultOptions
        return this
    }

    // ============ 启动方法 ============

    /**
     * 启动 Activity
     */
    @JvmOverloads
    fun startActivity(
        targetActivity: Class<out Activity?>,
        optionss: Bundle? = null,
    ) {
        startActivity(
            mContentObj, targetActivity, mBundle, optionss,
            mRequestCode, mActivityFlag, mOpenWithoutAnim, mFinishWithoutAnim,
            mActivityResultCallback, mActivityResultOptions
        )
    }

    /**
     * 启动 Fragment（自动通过 FragmentContainActivity 包裹）
     *
     * 这是最常用的方法 — 传入 Fragment 的 Class，自动创建宿主 Activity。
     */
    fun startFragment(
        fragmentClass: Class<out Fragment>,
        fragmentTag: String? = null
    ) {
        startFragment(fragmentClass.name, null, FragmentContainActivity::class.java, fragmentTag)
    }

    /**
     * 启动 Fragment 到指定的宿主 Activity
     *
     * @param fragmentName Fragment 完整类名
     * @param targetActivity 必须实现 [FragmentContain] 接口的 Activity
     */
    fun <T> startFragment(
        fragmentName: String,
        targetActivity: Class<out T?>,
        fragmentTag: String? = null
    ) where T : Activity?, T : FragmentContain? {
        startFragment(fragmentName, null, targetActivity, fragmentTag)
    }

    /**
     * 启动 Fragment（完整参数版本）
     *
     * @param fragmentName Fragment 完整类名
     * @param options 启动选项
     * @param targetActivity 必须实现 [FragmentContain] 接口的 Activity
     * @param fragmentTag Fragment 标签（可选）
     */
    fun <T> startFragment(
        fragmentName: String,
        options: Bundle?,
        targetActivity: Class<out T?>,
        fragmentTag: String? = null
    ) where T : Activity?, T : FragmentContain? {
        if (mBundle == null) {
            mBundle = Bundle()
        }
        mBundle!!.putString(INTENT_FRAGMENT_NAME, fragmentName)
        mBundle!!.putString(INTENT_FRAGMENT_TAG, fragmentTag)
        startActivity(
            mContentObj, targetActivity, mBundle, options,
            mRequestCode, mActivityFlag, mOpenWithoutAnim, mFinishWithoutAnim,
            mActivityResultCallback, mActivityResultOptions
        )
    }

    // ============ FragmentContain 接口 ============

    /**
     * 通过 Intent 加载 Fragment 的宿主 Activity 必须实现此接口。
     * [FragmentContainActivity] 已实现此接口，可直接使用。
     */
    interface FragmentContain {
        val fragmentName: String
    }

    // ============ 内部启动逻辑 ============

    private fun startActivity(
        contextObj: Any,
        target: Class<out Activity?>,
        bundle: Bundle?,
        options: Bundle?,
        requestCode: Int,
        flags: Int,
        openWithoutAnim: Boolean,
        finishWithoutAnim: Boolean,
        activityResultCallback: ActivityResultCallback<ActivityResult>? = null,
        activityResultOptions: ActivityOptionsCompat? = null
    ) {
        Log.d(TAG, "startActivity: target=$target, bundle=$bundle, requestCode=$requestCode")
        val context = contextObjToContent(contextObj)
            ?: throw InvalidParameterException("Unsupported contextObj: $contextObj")
        val intent = Intent(context, target)
        if (flags != 0) intent.addFlags(flags)
        if (bundle != null) {
            bundle.putBoolean(BUNDLE_OPEN_WITHOUT_ANIM, openWithoutAnim)
            bundle.putBoolean(BUNDLE_FINISH_WITHOUT_ANIM, finishWithoutAnim)
            intent.putExtras(bundle)
        }
        startActivityImpl(contextObj, intent, options, requestCode, openWithoutAnim, activityResultCallback, activityResultOptions)
    }

    /**
     * 实际执行 startActivity 的逻辑
     */
    private fun startActivityImpl(
        contextObj: Any,
        intent: Intent,
        options: Bundle?,
        requestCode: Int,
        isOpenWithoutAnim: Boolean,
        activityResultCallback: ActivityResultCallback<ActivityResult>? = null,
        activityResultOptions: ActivityOptionsCompat? = null
    ) {
        if (activityResultCallback == null) {
            // 无回调 — 直接启动
            when (contextObj) {
                is Fragment -> {
                    contextObj.startActivityForResult(intent, requestCode, options)
                    if (isOpenWithoutAnim) contextObj.activity?.overridePendingTransition(0, 0)
                }
                is Activity -> {
                    contextObj.startActivityForResult(intent, requestCode, options)
                    if (isOpenWithoutAnim) contextObj.overridePendingTransition(0, 0)
                }
                is Context -> contextObj.startActivity(intent)
                else -> throw InvalidParameterException("Unsupported context: $contextObj")
            }
        } else {
            // 有回调 — 通过临时 Fragment 的 registerForActivityResult
            val fragmentManager = when (contextObj) {
                is FragmentActivity -> contextObj.supportFragmentManager
                is Fragment -> contextObj.childFragmentManager
                else -> throw InvalidParameterException("Context with callback must be Fragment or FragmentActivity")
            }
            val tempFragment = Fragment()
            fragmentManager.beginTransaction()
                .add(tempFragment, "intentStarter_result")
                .commitNow()
            val launcher = tempFragment.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                activityResultCallback
            )
            tempFragment.lifecycleScope.launch {
                launcher.launch(intent, activityResultOptions)
            }
        }
    }

    companion object {
        private val TAG = IntentStarter::class.java.simpleName

        /** Intent 中传递 Fragment 类名的 key */
        const val INTENT_FRAGMENT_NAME = "INTENT_FRAGMENT_NAME"

        /** Intent 中传递 Fragment Tag 的 key */
        const val INTENT_FRAGMENT_TAG = "INTENT_FRAGMENT_TAG"

        // ============ 工厂方法 ============

        fun create(context: Any): IntentStarter = IntentStarter(context)
        fun create(context: Context): IntentStarter = IntentStarter(context)
        fun create(activity: Activity): IntentStarter = IntentStarter(activity)
        fun create(fragment: Fragment): IntentStarter = IntentStarter(fragment)
    }
}

// ============ 内部辅助函数 ============

/** 将 Any 类型转换为 Context */
private fun contextObjToContent(contextObj: Any): Context? {
    return when (contextObj) {
        is Fragment -> contextObj.activity
        is Activity -> contextObj
        is Context -> contextObj
        else -> null
    }
}
