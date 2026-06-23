# WebView 页面 — BaseArch Skill

## 功能概述
提供完整的 WebView 页面封装，包含进度条、JavaScript 交互、页面导航控制。

## 文件清单

### 1. WebPageViewModel.kt
```kotlin
package com.bbv.base.web

import androidx.lifecycle.MutableLiveData
import com.bbv.base.mvvm.BaseViewModel

/**
 * WebView 页 ViewModel
 */
class WebPageViewModel : BaseViewModel() {

    val url = MutableLiveData<String>()
    val title = MutableLiveData<String>()
    val progress = MutableLiveData<Int>()   // 0-100
    val isLoading = MutableLiveData<Boolean>()

    fun loadUrl(url: String) {
        this.url.postValue(url)
    }

    fun onProgressChanged(newProgress: Int) {
        progress.postValue(newProgress)
        isLoading.postValue(newProgress < 100)
    }

    fun onTitleChanged(newTitle: String) {
        title.postValue(newTitle)
    }
}
```

### 2. WebPageFragment.kt
```kotlin
package com.bbv.base.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bbv.base.mvvm.BaseVMFragment
import com.bbv.base.databinding.BaseFragmentWebPageBinding

/**
 * WebView 页面 Fragment
 */
class WebPageFragment : BaseVMFragment<BaseFragmentWebPageBinding, WebPageViewModel>() {

    companion object {
        private const val ARG_URL = "arg_url"
        private const val ARG_TITLE = "arg_title"

        fun newInstance(url: String, title: String = ""): WebPageFragment {
            return WebPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putString(ARG_TITLE, title)
                }
            }
        }
    }

    override fun getBinding(inflater: LayoutInflater) =
        BaseFragmentWebPageBinding.inflate(inflater)

    override fun getViewModelClass() = WebPageViewModel::class.java

    override fun initView(savedInstanceState: Bundle?) {
        setupWebView()
        arguments?.let {
            it.getString(ARG_URL)?.let { url -> viewModel.loadUrl(url) }
            it.getString(ARG_TITLE)?.let { title -> viewModel.onTitleChanged(title) }
        }
    }

    override fun initData() {
        viewModel.url.observe(viewLifecycleOwner) { url ->
            binding.webview.loadUrl(url)
        }
    }

    override fun initObserve() {
        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.webProgress.progress = progress
            binding.webProgress.visibility = if (progress < 100) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webview.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { viewModel.loadUrl(it) }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    viewModel.onProgressChanged(newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    title?.let { viewModel.onTitleChanged(it) }
                }
            }
        }
    }

    fun goBack(): Boolean {
        return if (binding.webview.canGoBack()) {
            binding.webview.goBack()
            true
        } else false
    }
}
```

### 3. WebPageJNI.kt
```kotlin
package com.bbv.base.web

import android.webkit.JavascriptInterface

/**
 * WebView JS 交互接口
 * 通过 @JavascriptInterface 暴露给 H5 调用
 */
class WebPageJNI {

    @JavascriptInterface
    fun getToken(): String {
        return "your_token_here"
    }

    @JavascriptInterface
    fun share(title: String, content: String, url: String) {
        // 调用原生分享
    }

    @JavascriptInterface
    fun close() {
        // 关闭页面
    }
}
```

## 依赖要求
```gradle
dependencies {
    implementation 'androidx.webkit:webkit:1.9.0'
}
```

## 使用方法
```kotlin
// 在 Activity 中加载 WebPageFragment
IntentStarter.start<FragmentContainActivity>(context) {
    putExtra(FragmentContainActivity.EXTRA_FRAGMENT, WebPageFragment::class.java.name)
    putExtra(FragmentContainActivity.EXTRA_BUNDLE, Bundle().apply {
        putString("arg_url", "https://example.com")
        putString("arg_title", "网页标题")
    })
}

// 或者直接使用
val fragment = WebPageFragment.newInstance("https://example.com")
supportFragmentManager.beginTransaction()
    .replace(R.id.container, fragment)
    .commit()
```