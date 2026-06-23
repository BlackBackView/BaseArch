# Dialog 对话框体系 — BaseArch Skill

## 功能概述
提供完整的对话框体系，包含基础对话框、确认对话框、带输入框对话框、
底部弹出菜单、SweetAlert 风格对话框、滚动内容对话框等。

## 文件清单

### 1. BaseDialog.kt
```kotlin
package com.bbv.base.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialog

open class BaseDialog(
    context: Context,
    @LayoutRes private val layoutRes: Int,
    private val gravity: Int = Gravity.CENTER,
    private val dimAmount: Float = 0.5f,
    private val cancelable: Boolean = true
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
        setCancelable(cancelable)
        setCanceledOnTouchOutside(cancelable)
        configWindow()
    }

    private fun configWindow() {
        window?.apply {
            setGravity(gravity)
            setDimAmount(dimAmount)
            val lp = attributes
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = lp
        }
    }
}
```

### 2. CommonDialog.kt
```kotlin
package com.bbv.base.dialog

import android.content.Context
import android.view.Gravity
import com.bbv.base.R

/**
 * 通用确认对话框
 */
class CommonDialog(context: Context) :
    BaseDialog(context, R.layout.base_layout_confirm_dialog) {

    fun setTitle(text: String): CommonDialog = apply { /* 设置标题 */ }
    fun setMessage(text: String): CommonDialog = apply { /* 设置消息 */ }
    fun setPositive(text: String, onClick: () -> Unit): CommonDialog = apply { /* 确认按钮 */ }
    fun setNegative(text: String, onClick: () -> Unit): CommonDialog = apply { /* 取消按钮 */ }
}
```

### 3. CommonBindDialog.kt
```kotlin
package com.bbv.base.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.bbv.base.util.ViewBindInvokeUtil

/**
 * 支持 ViewBinding 的通用对话框
 */
open class CommonBindDialog<VB : ViewBinding>(
    context: Context,
    private val binding: VB
) : BaseDialog(context, 0) {

    init {
        setContentView(binding.root)
    }

    companion object {
        fun <VB : ViewBinding> create(
            context: Context,
            inflate: (LayoutInflater) -> VB
        ): CommonBindDialog<VB> {
            val binding = inflate(LayoutInflater.from(context))
            return CommonBindDialog(context, binding)
        }
    }
}
```

### 4. DialogDSL.kt & PPDialogDSL.kt
提供 DSL 风格的对话框构建方式：

```kotlin
// DialogDSL
dialog(context) {
    title = "提示"
    message = "确定要删除吗？"
    positiveButton("确定") { /* 确认逻辑 */ }
    negativeButton("取消") { dismiss() }
}.show()

// PPDialogDSL — 居中弹出面板
ppdialog(context) {
    title = "选择操作"
    contentView = R.layout.layout_option_panel
    onDismiss { /* 关闭回调 */ }
}.show()
```

### 5. ScrollDialog.kt
```kotlin
package com.bbv.base.dialog

import android.content.Context
import android.view.Gravity

/**
 * 可滚动内容的对话框（常用于协议/条款展示）
 */
class ScrollDialog(context: Context) :
    BaseDialog(context, R.layout.base_layout_scroll_dialog, Gravity.CENTER) {

    fun setContent(html: String): ScrollDialog = apply { /* 设置 HTML 内容 */ }
    fun setTitle(text: String): ScrollDialog = apply { /* 设置标题 */ }
}
```

### 6. SweetDialog.kt
SweetAlert 风格的对话框（成功、错误、警告、进度条）。

### 7. BaseBottomSheetDialog.kt & CommonBottomDialog.kt
底部弹出对话框，支持从底部滑入，常用于分享菜单、选项列表。

## 依赖要求
无特殊依赖，基于 AppCompat 实现。

## 使用方法
```kotlin
// 普通确认对话框
CommonDialog(context)
    .setTitle("提示")
    .setMessage("确认此操作？")
    .setPositive("确定") { toast("已确认") }
    .setNegative("取消") { dismiss() }
    .show()

// DSL 风格
dialog(context) {
    title = "退出"
    message = "确定退出编辑？"
    positiveButton("确定") { finish() }
    negativeButton("取消") {}
}.show()
```