package com.bbv.base.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.lang.reflect.ParameterizedType

/**
 * 基础 Activity
 *
 * 提供 ViewBinding 自动绑定 + 协程作用域
 *
 * 使用方式：
 * ```
 * class MainActivity : BaseActivity<ActivityMainBinding>() {
 *     override fun onActivityCreated(savedInstanceState: Bundle?) {
 *         // 直接使用 mViewBinding
 *     }
 * }
 * ```
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), CoroutineScope by MainScope() {

    lateinit var mViewBinding: VB

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = createViewBinding(layoutInflater)
        setContentView(mViewBinding.root)
        onActivityCreated(savedInstanceState)
    }

    abstract fun onActivityCreated(savedInstanceState: Bundle?)

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    /**
     * 通过反射自动 inflate ViewBinding
     */
    @Suppress("UNCHECKED_CAST")
    private fun createViewBinding(inflater: LayoutInflater): VB {
        val bindingClass = findViewBindingClass(this.javaClass)
        val method = bindingClass.getMethod("inflate", LayoutInflater::class.java)
        return method.invoke(null, inflater) as VB
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
