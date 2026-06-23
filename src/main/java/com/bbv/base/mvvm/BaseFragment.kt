package com.bbv.base.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.lang.reflect.ParameterizedType
import kotlin.coroutines.CoroutineContext

/**
 * 基础 Fragment
 *
 * 提供 ViewBinding 自动绑定 + 协程作用域
 *
 * 使用方式：
 * ```
 * class HomeFragment : BaseFragment<FragmentHomeBinding>() {
 *     override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
 *         // 直接使用 mViewBinding
 *     }
 * }
 * ```
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment(), CoroutineScope {

    lateinit var mViewBinding: VB
    val mJob: Job by lazy { Job() }

    override val coroutineContext: CoroutineContext by lazy {
        mJob + Dispatchers.Main
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding = createViewBinding(inflater, container)
        return mViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onFragmentCreated(view, savedInstanceState)
    }

    abstract fun onFragmentCreated(view: View, savedInstanceState: Bundle?)

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    @Suppress("UNCHECKED_CAST")
    private fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB {
        val bindingClass = findViewBindingClass(this.javaClass)
        val method = bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        return method.invoke(null, inflater, container, false) as VB
    }

    @Suppress("UNCHECKED_CAST")
    private fun findViewBindingClass(clazz: Class<*>): Class<VB> {
        if (clazz.genericSuperclass is ParameterizedType) {
            val generics = (clazz.genericSuperclass as ParameterizedType).actualTypeArguments
            if (generics.isNotEmpty() && (generics[0] as Class<*>).simpleName.endsWith("Binding")) {
                return generics[0] as Class<VB>
            }
        }
        return findViewBindingClass(clazz.superclass)
    }
}
