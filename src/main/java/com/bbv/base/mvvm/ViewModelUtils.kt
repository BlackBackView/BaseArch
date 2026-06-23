package com.bbv.base.mvvm

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import java.lang.reflect.ParameterizedType

/**
 * ViewModel 自动创建工具
 *
 * 通过反射遍历类继承链，从泛型参数中提取 ViewModel 的 Class 类型，
 * 然后用 [ViewModelProvider] 自动创建实例。
 *
 * 这样子类无需写 `override val mViewModel: XxxViewModel by lazy { XxxViewModel() }`。
 *
 * ## 原理
 *
 * 假设类继承链为：
 * ```
 * OrderListFragment -> BaseListFragment<..., OrderListViewModel, ...> -> BaseVMFragment<VB, VM>
 * ```
 *
 * 遍历到 `OrderListFragment` 的 `genericSuperclass` = `BaseListFragment<..., OrderListViewModel, ...>`
 * 从中找到 `OrderListViewModel`（因为它继承 [BaseViewModel]）。
 */
@Suppress("UNCHECKED_CAST")
object ViewModelUtils {

    /**
     * 自动创建 ViewModel 实例
     *
     * @param owner ViewModelStoreOwner（Activity / Fragment）
     * @return 自动推断类型并创建的 ViewModel
     */
    @Suppress("UNCHECKED_CAST")
    fun <VM : BaseViewModel> create(owner: ViewModelStoreOwner): VM {
        val vmClass = findViewModelClass(owner.javaClass) as Class<VM>
        return ViewModelProvider(owner).get(vmClass)
    }

    /**
     * 从类继承链的泛型参数中找到 ViewModel 类
     *
     * 遍历 [clazz] 到 Object 的整个继承链，在每个节点检查 `genericSuperclass`
     * 的 `actualTypeArguments`，找到第一个继承 [BaseViewModel] 的具体类型。
     */
    @Suppress("UNCHECKED_CAST")
    fun findViewModelClass(clazz: Class<*>): Class<out BaseViewModel> {
        var cls: Class<*> = clazz
        while (cls != Any::class.java) {
            val genericSuperclass = cls.genericSuperclass
            if (genericSuperclass is ParameterizedType) {
                for (typeArg in genericSuperclass.actualTypeArguments) {
                    if (typeArg is Class<*> && BaseViewModel::class.java.isAssignableFrom(typeArg)) {
                        return typeArg as Class<out BaseViewModel>
                    }
                }
            }
            cls = cls.superclass
        }
        throw IllegalArgumentException(
            "Cannot find ViewModel class from generic type of $clazz.\n" +
                    "Make sure the class hierarchy has a concrete ViewModel type parameter, e.g.:\n" +
                    "class MyFragment : BaseVMFragment<Binding, MyViewModel>()"
        )
    }
}
