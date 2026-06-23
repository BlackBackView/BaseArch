package com.bbv.base.web

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.bbv.base.databinding.BaseFragmentWebPageBinding
import com.bbv.base.mvvm.BaseVMFragment
import com.bbv.base.navigation.IntentStarter
import com.bbv.base.navigation.OnBackPressedHandler
import com.bbv.base.data.IParamsProvider
import com.bbv.base.data.IntentConstants
import com.bbv.base.util.Lg
import java.net.URISyntaxException
import kotlin.math.max

/**
 * 全功能 WebView 容器 Fragment
 *
 * 特性：
 * - URL/HTML 加载
 * - Scheme 拦截（alipays/weixin/intent/http）
 * - WebProgress 动画进度条
 * - JS Bridge（WebPageJNI）
 * - 滚到底部回调
 * - 返回键 WebView 内历史栈导航
 */
open class WebPageFragment : BaseVMFragment<BaseFragmentWebPageBinding, WebPageViewModel>(),
    OnBackPressedHandler {

    companion object {
        private const val PREFIX_ALIPAYS = "alipays://"
        private const val PREFIX_WEIXIN = "weixin://"
        private const val PREFIX_INTENT = "intent:"

        fun start(
            any: Any,
            url: String,
            title: String? = null,
            needClose: Boolean = false,
            closeColor: Int? = null
        ) {
            IntentStarter.create(any)
                .withData(IntentConstants.INTENT_TITLE, title)
                .withData(IntentConstants.INTENT_URL, url)
                .withData(IntentConstants.INTENT_NEED_CLOSE, needClose)
                .withData(IntentConstants.INTENT_CLOSE_COLOR, closeColor)
                .startFragment(WebPageFragment::class.java)
        }

        fun startHtml(
            any: Any,
            html: String,
            title: String? = null,
            needClose: Boolean = false,
            closeColor: Int? = null
        ) {
            IntentStarter.create(any)
                .withData(IntentConstants.INTENT_TITLE, title)
                .withData(IntentConstants.INTENT_HTML, html)
                .withData(IntentConstants.INTENT_NEED_CLOSE, needClose)
                .withData(IntentConstants.INTENT_CLOSE_COLOR, closeColor)
                .startFragment(WebPageFragment::class.java)
        }

        private var mWebPageFragmentCallback: WebPageFragmentCallback? = null

        fun setWebPageFragmentCallback(callback: WebPageFragmentCallback) {
            mWebPageFragmentCallback = callback
        }
    }

    interface WebPageFragmentCallback {
        fun onWebContentScrollToBottom()
    }

    private var mTitle: String? = null
    private var mUrl: String? = null
    private var mHtml: String? = null

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        super.onFragmentCreated(view, savedInstanceState)
        mViewModel.setViewModelAction(object : WebPageViewModel.ViewModelAction {})

        // Status bar: white background + dark icons
        requireActivity().window.statusBarColor = Color.WHITE
        ViewCompat.getWindowInsetsController(requireActivity().window.decorView)
            ?.isAppearanceLightStatusBars = true

        mViewBinding.viewModel = mViewModel
        mViewBinding.lifecycleOwner = viewLifecycleOwner

        mTitle = arguments?.getString(IntentConstants.INTENT_TITLE)
        mUrl = arguments?.getString(IntentConstants.INTENT_URL)
        mHtml = arguments?.getString(IntentConstants.INTENT_HTML)

        // Set Activity title if provided
        mTitle?.let { requireActivity().title = it }

        initWebView()
    }

    override fun onBackPressed(): Boolean {
        return if (mViewBinding.webview.canGoBack()) {
            mViewBinding.webview.goBack()
            true
        } else {
            false
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        // Enable WebView debugging in debug mode
        WebView.setWebContentsDebuggingEnabled(
            (requireActivity().application as? IParamsProvider)?.isDebug() ?: false
        )

        // Set progress bar color from theme primary color
        kotlin.runCatching {
            val typedArray = requireContext().obtainStyledAttributes(
                intArrayOf(android.R.attr.colorPrimary)
            )
            mViewBinding.webProgress.setColor(
                typedArray.getColor(0, Color.parseColor("#2483D9"))
            )
            typedArray.recycle()
        }

        mViewBinding.webview.apply {
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccessFromFileURLs = true
            }

            addJavascriptInterface(WebPageJNI(requireActivity()), "jni")

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString()
                    Lg.d("shouldOverrideUrlLoading, url: $url")
                    return when {
                        url?.startsWith(PREFIX_ALIPAYS) == true -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                view!!.context.startActivity(intent)
                            } catch (e: Throwable) {
                                Toast.makeText(
                                    view!!.context,
                                    "启动失败，需要安装支付宝",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            true
                        }
                        url?.startsWith(PREFIX_WEIXIN) == true -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                view!!.context.startActivity(intent)
                            } catch (e: Throwable) {
                                Toast.makeText(
                                    view!!.context,
                                    "启动失败，需要安装微信",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            true
                        }
                        url?.startsWith(PREFIX_INTENT) == true -> {
                            try {
                                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                intent.addCategory("android.intent.category.BROWSABLE")
                                intent.component = null
                                startActivity(intent)
                            } catch (e: URISyntaxException) {
                                Lg.e("URISyntaxException: ${e.message}")
                            }
                            true
                        }
                        url?.startsWith("http") == true -> {
                            false
                        }
                        else -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                view!!.context.startActivity(intent)
                            } catch (e: Throwable) {
                                Toast.makeText(
                                    view!!.context,
                                    "应用未安装",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            true
                        }
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Lg.d("onPageStarted, url: $url")
                    mViewBinding.webProgress.show()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Lg.d("onPageFinished, url: $url")
                    mViewBinding.webProgress.hide()
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    Lg.d("onLoadResource, url: $url")
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mViewBinding.webProgress.setWebProgress(newProgress)
                }

                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    return true
                }

                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    return true
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    callback?.invoke(origin, true, false)
                    super.onGeolocationPermissionsShowPrompt(origin, callback)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    if (mTitle == null) {
                        if (title.isNullOrEmpty()) {
                            requireActivity().title =
                                mUrl?.substring(0, max(10, mUrl?.length ?: 10))
                        } else {
                            requireActivity().title = title
                        }
                    }
                }
            }
        }

        // Load content
        mUrl?.let {
            mViewBinding.webview.loadUrl(it)
        } ?: mHtml?.let {
            mViewBinding.webview.loadDataWithBaseURL(null, it, "text/html", "UTF-8", null)
        }

        // Scroll to bottom detection
        mViewBinding.webview.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            Lg.d(
                "scroll, htmlHeight: ${mViewBinding.webview.contentHeight * mViewBinding.webview.scale}, " +
                        "webviewHeight: ${v.height}, scrollY: $scrollY"
            )
            if (mViewBinding.webview.contentHeight > 0 &&
                mViewBinding.webview.contentHeight * mViewBinding.webview.scale - v.height - scrollY <= 10
            ) {
                Lg.d("scrollToBottom")
                mWebPageFragmentCallback?.onWebContentScrollToBottom()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mViewBinding.webview.onResume()
    }

    override fun onPause() {
        super.onPause()
        mViewBinding.webview.onPause()
    }

    override fun onDestroy() {
        mViewBinding.webview.removeAllViews()
        mViewBinding.webview.destroy()
        mWebPageFragmentCallback = null
        super.onDestroy()
    }
}
