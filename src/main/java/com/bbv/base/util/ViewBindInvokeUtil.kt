package com.bbv.base.util

/**
 * @Description:
 * @Author: 
 * @Date: 2024/12/24 23:19
 */

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.io.InvalidClassException
import java.lang.reflect.ParameterizedType

fun <VB : ViewBinding> invokeInflate(
    component: Any,
    inflater: LayoutInflater,
    container: ViewGroup?
): VB {
    val tagetClass: Class<VB> = getTargetClass(component.javaClass)
    val method = tagetClass.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )
    Log.d("", "invokeInflate() called with: component = [$component], inflater = [$inflater], container = [$container], tagetClass = [$tagetClass]")
    val invoke = method.invoke(tagetClass, inflater, container, false)
    return invoke as VB
}

fun <VB : ViewBinding> invokeInflate(
    component: Any,inflater: LayoutInflater
): VB {
    val targetClass: Class<VB> = getTargetClass(component.javaClass)
    val method = targetClass.getMethod(
        "inflate",
        LayoutInflater::class.java
    )
    Log.d("", "invokeInflate() called with: component = [$component], inflater = [$inflater], tagetClass = [$targetClass]")
    val invoke = method.invoke(targetClass, inflater)
    return invoke as VB
}

private fun <VB:ViewBinding> getTargetClass(component:Class<Any>):Class<VB>{
    if(component.genericSuperclass !is ParameterizedType){
        return getTargetClass<VB>(component.superclass!!)
    }
    val generics = (component.genericSuperclass as ParameterizedType).actualTypeArguments
    // 用名字判断，不太准确
    if (generics.isEmpty() || !(generics[0] as Class<Any>).simpleName.endsWith("Binding")){
        if(component.superclass == null) {
            throw InvalidClassException("The first generic type a class must be ViewBinding")
        }else{
            return getTargetClass<VB>(component.superclass!!)
        }
    }
    return generics[0] as Class<VB>
}
