package com.bbv.base.mvvm

import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import org.greenrobot.eventbus.EventBus

/**
 * 带 ViewModel 的 BaseActivity
 *
 * **ViewModel 自动创建** — 子类无需手动注册 ViewModel。
 * 通过反射从泛型参数中自动推断 ViewModel 类，用 [ViewModelProvider] 创建。
 *
 * ## 使用方式（不再需要手动创建 ViewModel）
 *
 * ```kotlin
 * // ❌ 旧方式：需要手动写 override val mViewModel
 * class MainActivity : BaseVMActivity<ActivityMainBinding, MainViewModel>() {
 *     override val mViewModel: MainViewModel by lazy { MainViewModel() }  // 不用写了
 *     ...
 * }
 *
 * // ✅ 新方式：干干净净
 * class MainActivity : BaseVMActivity<ActivityMainBinding, MainViewModel>() {
 *     // mViewModel 自动创建好了
 *     override fun onActivityCreated(savedInstanceState: Bundle?) {
 *         mViewModel.doSomething()  // 直接用
 *     }
 * }
 * ```
 *
 * **如果 ViewModel 需要构造函数参数**，仍然可以手动覆盖：
 * ```kotlin
 * class OrderListActivity : BaseListActivity<..., OrderListViewModel, ...>() {
 *     override val mViewModel: OrderListViewModel by lazy {
 *         OrderListViewModel(repository)
 *     }
 * }
 * ```
 */
abstract class BaseVMActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB>() {

    /**
     * ViewModel 实例 — 自动创建，子类无需手动声明。
     *
     * 如需带参构造，可以手动 override。
     */
    open val mViewModel: VM by lazy {
        ViewModelUtils.create<VM>(this)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.onPauseBundle(intent.extras)
        if (mViewModel.isNeedEventBus() && !EventBus.getDefault().isRegistered(mViewModel)) {
            EventBus.getDefault().register(mViewModel)
        }
    }

    @CallSuper
    override fun onDestroy() {
        if (mViewModel.isNeedEventBus() && EventBus.getDefault().isRegistered(mViewModel)) {
            EventBus.getDefault().unregister(mViewModel)
        }
        super.onDestroy()
    }

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mViewModel.mPageState.observe(this, Observer { pageState ->
            dispatchPageState(pageState)
        })
    }

    open fun dispatchPageState(pageState: PageState?) {
        Log.d("BaseVMActivity", "dispatchPageState: $pageState")
        pageState?.apply {
            when (pageStateType) {
                PageStateType.ERROR -> handleErrorPageState(this)
                else -> handleOtherPageState(this)
            }
        }
    }

    open fun handleErrorPageState(pageState: PageState) {
        mViewModel.setStateNormal()
    }

    open fun handleOtherPageState(pageState: PageState) {
        // 子类可扩展处理 LOADING / EMPTY / NORMAL
    }
}
