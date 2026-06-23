package com.bbv.base.mvvm

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import org.greenrobot.eventbus.EventBus

/**
 * 带 ViewModel 的 BaseFragment
 *
 * **ViewModel 自动创建** — 子类无需手动注册 ViewModel。
 * 通过反射从泛型参数中自动推断 ViewModel 类，用 [ViewModelProvider] 创建。
 *
 * ## 使用方式（不再需要手动创建 ViewModel）
 *
 * ```kotlin
 * // ✅ 新方式：干干净净
 * class HomeFragment : BaseVMFragment<FragmentHomeBinding, HomeViewModel>() {
 *     override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
 *         mViewModel.doSomething()  // 直接用，无需 override
 *     }
 * }
 * ```
 *
 * **如果 ViewModel 需要构造函数参数**，仍然可以手动覆盖：
 * ```kotlin
 * class HomeFragment : BaseVMFragment<FragmentHomeBinding, HomeViewModel>() {
 *     override val mViewModel: HomeViewModel by lazy {
 *         HomeViewModel(repository)
 *     }
 * }
 * ```
 */
abstract class BaseVMFragment<VB : ViewBinding, VM : BaseViewModel> : BaseFragment<VB>() {

    /**
     * ViewModel 实例 — 自动创建，子类无需手动声明。
     *
     * 如需带参构造，可以手动 override。
     */
    open val mViewModel: VM by lazy {
        ViewModelUtils.create<VM>(this)
    }

    @CallSuper
    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        mViewModel.mPageState.observe(viewLifecycleOwner, Observer { pageState ->
            dispatchPageState(pageState)
        })
        if (mViewModel.isNeedEventBus() && !EventBus.getDefault().isRegistered(mViewModel)) {
            EventBus.getDefault().register(mViewModel)
        }
    }

    @CallSuper
    override fun onDestroyView() {
        if (mViewModel.isNeedEventBus() && EventBus.getDefault().isRegistered(mViewModel)) {
            EventBus.getDefault().unregister(mViewModel)
        }
        super.onDestroyView()
    }

    open fun dispatchPageState(pageState: PageState?) {
        Log.d("BaseVMFragment", "dispatchPageState: $pageState")
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
