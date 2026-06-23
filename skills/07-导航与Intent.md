# 导航与 Intent — BaseArch Skill

## 功能概述
提供页面导航工具类，包含 Intent 启动器、Fragment 容器 Activity、Intent 参数传递工具。

## 文件清单

### 1. IntentStarter.kt
```kotlin
package com.bbv.base.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Intent 启动器
 * 简化页面跳转，支持携带参数、startActivityForResult、转场动画
 */
object IntentStarter {

    inline fun <reified T : Activity> start(
        context: Context,
        block: Intent.() -> Unit = {}
    ) {
        val intent = Intent(context, T::class.java).apply(block)
        context.startActivity(intent)
    }

    inline fun <reified T : Activity> startWithAnim(
        context: Context,
        enterAnim: Int = 0,
        exitAnim: Int = 0,
        block: Intent.() -> Unit = {}
    ) {
        val intent = Intent(context, T::class.java).apply(block)
        if (context is Activity) {
            context.startActivity(intent)
            context.overridePendingTransition(enterAnim, exitAnim)
        } else {
            context.startActivity(intent)
        }
    }

    inline fun <reified T : Activity> startForResult(
        activity: Activity,
        requestCode: Int,
        block: Intent.() -> Unit = {}
    ) {
        val intent = Intent(activity, T::class.java).apply(block)
        activity.startActivityForResult(intent, requestCode)
    }
}
```

### 2. IntentUtils.kt
```kotlin
package com.bbv.base.navigation

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Intent 参数工具类
 * 安全的参数读写，支持泛型转换
 */
object IntentUtils {

    inline fun <reified T> Intent.getExtra(key: String): T? {
        return when (T::class) {
            String::class -> getStringExtra(key) as? T
            Int::class -> getIntExtra(key, 0) as? T
            Boolean::class -> getBooleanExtra(key, false) as? T
            Long::class -> getLongExtra(key, 0L) as? T
            Float::class -> getFloatExtra(key, 0f) as? T
            Double::class -> getDoubleExtra(key, 0.0) as? T
            else -> {
                if (Parcelable::class.java.isAssignableFrom(T::class.java)) {
                    getParcelableExtra(key) as? T
                } else if (Serializable::class.java.isAssignableFrom(T::class.java)) {
                    getSerializableExtra(key) as? T
                } else {
                    null
                }
            }
        }
    }

    fun Intent.putParams(bundle: Bundle): Intent {
        putExtras(bundle)
        return this
    }
}
```

### 3. FragmentContainActivity.kt
```kotlin
package com.bbv.base.navigation

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bbv.base.R

/**
 * Fragment 容器 Activity
 * 承载单个 Fragment 的通用 Activity
 */
class FragmentContainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FRAGMENT = "extra_fragment"
        const val EXTRA_BUNDLE = "extra_bundle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity_fragment_contain)

        val fragmentName = intent.getStringExtra(EXTRA_FRAGMENT) ?: return
        val bundle = intent.getBundleExtra(EXTRA_BUNDLE)

        val fragment = Fragment.instantiate(this, fragmentName, bundle)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is OnBackPressListener) {
            if (!fragment.onBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    interface OnBackPressListener {
        fun onBackPressed(): Boolean
    }
}
```

## 依赖要求
```gradle
dependencies {
    implementation 'androidx.fragment:fragment-ktx:1.6.2'
}
```

## 使用方法
```kotlin
// 跳转 Activity
IntentStarter.start<DetailActivity>(context) {
    putExtra("id", 123)
    putExtra("title", "详情")
}

// 带转场动画
IntentStarter.startWithAnim<DetailActivity>(context,
    R.anim.slide_in_right, R.anim.slide_out_left
)

// Fragment 容器模式
IntentStarter.start<FragmentContainActivity>(context) {
    putExtra(FragmentContainActivity.EXTRA_FRAGMENT, MyFragment::class.java.name)
    putExtra(FragmentContainActivity.EXTRA_BUNDLE, bundleOf("key" to "value"))
}

// 接收参数
val id: Int? = intent.getExtra("id")
```