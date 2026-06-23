# DataBinding 工具集 — BaseArch Skill

## 功能概述
提供 ViewBinding 的反射注入、防抖点击、以及常用 BindingAdapter。

## 文件清单

### 1. ViewBindInvokeUtil.kt
```kotlin
package com.bbv.base.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method

/**
 * ViewBinding 反射调用工具
 * 通过反射调用 Binding 类的 inflate 或 bind 方法
 */
object ViewBindInvokeUtil {

    private val BINDING_CLASS_CACHE = HashMap<String, Class<*>>()

    fun <VB : ViewBinding> inflate(inflater: LayoutInflater, layoutId: Int): VB {
        val bindingClass = getBindingClass(layoutId)
        val inflateMethod = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        return inflateMethod.invoke(null, inflater) as VB
    }

    fun <VB : ViewBinding> inflate(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): VB {
        val layoutId = 0 // 需要从外部传入
        val bindingClass = getBindingClass(layoutId)
        val inflateMethod = bindingClass.getDeclaredMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        )
        return inflateMethod.invoke(null, inflater, parent, attachToParent) as VB
    }

    private fun getBindingClass(layoutId: Int): Class<*> {
        return BINDING_CLASS_CACHE.getOrPut(layoutId.toString()) {
            // 实际项目中可能会根据 layoutId 映射到对应的 Binding 类
            throw IllegalArgumentException("无法找到 layoutId=$layoutId 对应的 Binding 类")
        }
    }
}
```

### 2. ThrottleClickHandler.kt
```kotlin
package com.bbv.base.binding

import android.os.SystemClock
import android.view.View

/**
 * 通用防抖点击处理器
 */
class ThrottleClickHandler(
    private val interval: Long = 600L
) {
    private var lastClickTime: Long = 0

    fun isThrottled(): Boolean {
        val current = SystemClock.elapsedRealtime()
        if (current - lastClickTime < interval) {
            return true
        }
        lastClickTime = current
        return false
    }

    fun onClick(view: View, action: (View) -> Unit) {
        if (!isThrottled()) {
            action(view)
        }
    }
}
```

### 3. BindThrottleClickHandler.kt
```kotlin
package com.bbv.base.binding

import android.os.SystemClock
import android.view.View
import androidx.databinding.BindingAdapter

/**
 * DataBinding 防抖点击 BindingAdapter
 */
object BindThrottleClickHandler {

    private var lastClickTime: Long = 0

    @JvmStatic
    @BindingAdapter("onThrottleClick")
    fun onThrottleClick(view: View, click: View.OnClickListener?) {
        view.setOnClickListener {
            val current = SystemClock.elapsedRealtime()
            if (current - lastClickTime > 600L) {
                lastClickTime = current
                click?.onClick(it)
            }
        }
    }
}
```

### 4. ViewBindingAdapter.kt
```kotlin
package com.bbv.base.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * 常用 BindingAdapter 集合
 */
object ViewBindingAdapter {

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, url: String?) {
        if (url.isNullOrBlank()) return
        Glide.with(view.context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("imageCircleUrl")
    fun loadCircleImage(view: ImageView, url: String?) {
        if (url.isNullOrBlank()) return
        Glide.with(view.context)
            .load(url)
            .transform(CircleCrop())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}
```

## 依赖要求
```gradle
android {
    dataBinding { enabled = true }
}
dependencies {
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    implementation 'androidx.databinding:databinding-runtime:8.0.0'
}
```

## 使用方法

### XML 中使用防抖点击
```xml
<layout>
    <data>
        <variable name="click" type="android.view.View.OnClickListener" />
    </data>
    <Button
        android:onThrottleClick="@{click}" />
</layout>
```

### XML 中使用图片加载
```xml
<layout>
    <data>
        <variable name="url" type="String" />
    </data>
    <ImageView
        imageUrl="@{url}" />
</layout>
```