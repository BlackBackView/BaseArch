package com.bbv.base.navigation

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.bbv.base.R
import com.bbv.base.databinding.BaseActivityFragmentContainBinding
import com.bbv.base.mvvm.BaseActivity

/**
 * Fragment 容器 Activity
 *
 * 当通过 [IntentStarter.startFragment] 启动 Fragment 时，
 * 自动使用此 Activity 作为宿主，在 [fragment_container] 中加载目标 Fragment。
 *
 * 此 Activity 实现了 [IntentStarter.FragmentContain] 接口，
 * 从 Intent 中读取 Fragment 类名并实例化。
 */
class FragmentContainActivity :
    BaseActivity<BaseActivityFragmentContainBinding>(),
    IntentStarter.FragmentContain {

    override val fragmentName: String
        get() = intent?.getStringExtra(IntentStarter.INTENT_FRAGMENT_NAME)
            ?: throw IllegalArgumentException("Missing INTENT_FRAGMENT_NAME in intent")

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment: Fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader, fragmentName
            )
            // 将 Intent 中的 extra 数据传递给 Fragment 作为 arguments
            fragment.arguments = intent?.extras
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is OnBackPressedHandler) {
            if (!currentFragment.onBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}

/**
 * Fragment 返回键处理接口
 * 如果 Fragment 需要拦截返回键，实现此接口并在 [onBackPressed] 中返回 true
 */
interface OnBackPressedHandler {
    fun onBackPressed(): Boolean
}
