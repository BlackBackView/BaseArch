package com.bbv.base.util

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.bbv.base.mvvm.BaseViewModel
import java.io.InvalidClassException
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

/**
 * @Description:
 * @Author: 
 * @Date: 2025/1/8 23:57
 */

fun ObservableBoolean.toggle(){
    this.set(!this.get())
}

fun MutableLiveData<Boolean>.toggle(){
    if (this.value == null){
        this.value = false
    }
    this.value = !this.value!!
}

fun <VM: BaseViewModel> getViewModelKClass(component: Class<Any>): KClass<VM>{
    if(component.genericSuperclass !is ParameterizedType){
        return getViewModelKClass(component.superclass!!)
    }
    val generics = (component.genericSuperclass as ParameterizedType).actualTypeArguments
    // 用名字判断，不太准确。但是相较于直接拿第几个，还是更合理一点
    return (generics.firstOrNull {
        (it as Class<*>).simpleName.endsWith("ViewModel")
    }  as? Class<VM>)?.kotlin ?: run {
        if (component.superclass == null){
            throw InvalidClassException("The second generic type must be a ViewModel class")
        }else{
            getViewModelKClass(component.superclass!!)
        }
    }
}
