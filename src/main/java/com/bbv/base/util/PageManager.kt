package com.bbv.base.util

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bbv.base.navigation.FragmentContainActivity
import java.util.Stack

/**
 * @Description:
 * @Author: 
 * @Date: 2025/1/2 23:47
 */
object PageManager {
    fun interface BackResultListener {
        fun onBackResult(data: Bundle)
    }
    private class ActivityStackItem(val activity: Activity, val activityName: String, val tag: String? = null)
    private val mActivityStack = Stack<ActivityStackItem>()
    fun addActivity(activity: Activity, tag: String? = null) {
        mActivityStack.add(ActivityStackItem(activity, activity::class.java.simpleName, tag))
    }
    internal fun addFragment(activity: Activity, fragmentClass: Class<out Fragment>, tag: String? = null) {
        addActivity(activity, "${fragmentClass.simpleName}$tag")
    }
    internal fun removeActivity(activity: Activity) {
        mActivityStack.firstOrNull { activity == it.activity}?.let {
            mActivityStack.remove(it)
        }
    }
    fun finishActivity(activityClass: Class<out Activity>, tag: String? = null) {
        mActivityStack.firstOrNull { activityClass.simpleName == it.activityName && tag == it.tag }?.let {
            it.activity.finish()
            mActivityStack.remove(it)
        }
    }
    fun finishFragment(fragmentClass: Class<out Fragment>, tag: String? = null) {
        finishActivity(FragmentContainActivity::class.java, "${fragmentClass.simpleName}$tag")
    }
    fun backToActivity(activityClass: Class<out Activity>, tag: String? = null, data: Bundle? = null) {
        val backActivityStackItem = mActivityStack.firstOrNull { activityClass.simpleName == it.activityName && tag == it.tag }
        if (backActivityStackItem != null) {
            while (mActivityStack.isNotEmpty()){
                val activityStackItem = mActivityStack.peek()
                if (backActivityStackItem != activityStackItem) {
                    mActivityStack.pop()
                    activityStackItem.activity.finish()
                }else{
                    data?.let {
                        if (activityStackItem.activity is FragmentContainActivity) {
                            val fragment = (activityStackItem.activity as FragmentContainActivity)
                                .supportFragmentManager.findFragmentById(com.bbv.base.R.id.fragment_container)
                            (fragment as? BackResultListener)?.onBackResult(it)
                        } else {
                            (activityStackItem.activity as? BackResultListener)?.onBackResult(it)
                        }
                    }
                    break
                }
            }
        }
    }
    fun backToFragment(fragmentClass: Class<out Fragment>, tag: String? = null, data: Bundle? = null){
        backToActivity(FragmentContainActivity::class.java, "${fragmentClass.simpleName}$tag", data)
    }
}
