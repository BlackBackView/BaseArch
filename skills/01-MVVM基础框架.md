# MVVM 基础框架 — BaseArch Skill

## 功能概述
提供 Android MVVM 架构的基类，包含 Activity、Fragment、ViewModel 的基础封装，
以及页面状态管理（PageState）和 ViewModel 工具类。

## 文件清单

### 1. BaseActivity.kt
```kotlin
package com.bbv.base.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.bbv.base.util.ViewBindInvokeUtil

/**
 * 不使用 ViewModel 的 Activity 基类
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
        private set

    @LayoutRes
    abstract fun getLayoutRes(): Int

    abstract fun getBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getBinding(layoutInflater)
        setContentView(binding.root)
        initView(savedInstanceState)
        initData()
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData()
}
```

### 2. BaseVMActivity.kt
```kotlin
package com.bbv.base.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

/**
 * 使用 ViewModel 的 Activity 基类
 */
abstract class BaseVMActivity<VB : ViewBinding, VM : ViewViewModel> : AppCompatActivity() {

    protected lateinit var binding: VB
        private set

    protected lateinit var viewModel: VM
        private set

    abstract fun getBinding(inflater: LayoutInflater): VB
    abstract fun getViewModelClass(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[getViewModelClass()]!!
        initView(savedInstanceState)
        initData()
        initObserve()
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData()
    abstract fun initObserve()
}
```

### 3. BaseFragment.kt
```kotlin
package com.bbv.base.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected lateinit var binding: VB
        private set

    abstract fun getBinding(inflater: LayoutInflater): VB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getBinding(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initData()
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData()
}
```

### 4. BaseVMFragment.kt
```kotlin
package com.bbv.base.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseVMFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {

    protected lateinit var binding: VB
        private set

    protected lateinit var viewModel: VM
        private set

    abstract fun getBinding(inflater: LayoutInflater): VB
    abstract fun getViewModelClass(): Class<VM>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getBinding(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[getViewModelClass()]!!
        initView(savedInstanceState)
        initData()
        initObserve()
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData()
    abstract fun initObserve()
}
```

### 5. BaseViewModel.kt
```kotlin
package com.bbv.base.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { block() }
    }
}
```

### 6. PageState.kt
```kotlin
package com.bbv.base.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

sealed class PageState {
    object Loading : PageState()
    object Empty : PageState()
    data class Error(val code: Int, val msg: String) : PageState()
    object Success : PageState()
    object NetError : PageState()
}

fun MutableLiveData<PageState>.loading() {
    postValue(PageState.Loading)
}

fun MutableLiveData<PageState>.success() {
    postValue(PageState.Success)
}

fun MutableLiveData<PageState>.empty() {
    postValue(PageState.Empty)
}

fun MutableLiveData<PageState>.error(code: Int, msg: String) {
    postValue(PageState.Error(code, msg))
}

fun MutableLiveData<PageState>.netError() {
    postValue(PageState.NetError)
}
```

### 7. ViewModelUtils.kt
```kotlin
package com.bbv.base.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.ParameterizedType

object ViewModelUtils {

    fun <VM : ViewModel> getGenericViewModelClass(obj: Any): Class<VM> {
        val type = obj.javaClass.genericSuperclass
        val params = (type as ParameterizedType).actualTypeArguments
        @Suppress("UNCHECKED_CAST")
        return params.first { it is Class<*> && ViewModel::class.java.isAssignableFrom(it) } as Class<VM>
    }
}
```

## 使用方法

```kotlin
// 1. 定义 ViewModel
class MyViewModel : BaseViewModel() {
    private val _pageState = MutableLiveData<PageState>()
    val pageState: LiveData<PageState> = _pageState

    fun loadData() {
        launchOnUI {
            _pageState.loading()
            try {
                // 网络请求...
                _pageState.success()
            } catch (e: Exception) {
                _pageState.error(-1, e.message ?: "未知错误")
            }
        }
    }
}

// 2. Activity 中使用
class MainActivity : BaseVMActivity<ActivityMainBinding, MyViewModel>() {
    override fun getBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)
    override fun getViewModelClass() = MyViewModel::class.java

    override fun initView(savedInstanceState: Bundle?) {}
    override fun initData() { viewModel.loadData() }
    override fun initObserve() {
        viewModel.pageState.observe(this) { state ->
            when (state) {
                is PageState.Loading -> showLoading()
                is PageState.Success -> hideLoading()
                is PageState.Error -> showError(state.msg)
                else -> {}
            }
        }
    }
}
```