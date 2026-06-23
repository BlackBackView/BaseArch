# 自定义 Widget — BaseArch Skill

## 功能概述
提供自定义 UI 组件，包括 Fragment Tab 宿主、Tab 构建器、Grid 间距装饰器等功能。

## 文件清单

### 1. StableFragmentTabHost.kt & AbstractTabBuild.kt
```kotlin
package com.bbv.base.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TabHost

/**
 * 稳定的 Fragment Tab 宿主
 * 解决 TabHost + Fragment 的常见问题
 */
class StableFragmentTabHost : TabHost {

    private val mFragmentManager: FragmentManager
    private val mAttachedFragment: MutableMap<String, Fragment> = HashMap()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun addTabItem(
        tag: String,
        clazz: Class<out Fragment>,
        args: Bundle? = null,
        indicator: View
    ) {
        // 添加 Tab 页
    }

    fun getFragment(tag: String): Fragment? = mAttachedFragment[tag]
}

/**
 * Tab 构建器基类
 */
abstract class AbstractTabBuild<T>(
    protected val context: Context,
    protected val tabHost: StableFragmentTabHost
) {
    protected val tabItems = ArrayList<T>()

    abstract fun build(): StableFragmentTabHost

    inner class TabItem(
        val tag: String,
        val fragmentClass: Class<out Fragment>,
        val args: Bundle?
    )
}
```

### 2. SimpleMenuItem.kt
```kotlin
package com.bbv.base.widget.tab

/**
 * 简单菜单项数据模型
 */
data class SimpleMenuItem(
    val id: Int = 0,
    val title: String = "",
    val iconRes: Int = 0,
    val isSelected: Boolean = false
)
```

### 3. IScrollTop.kt
```kotlin
package com.bbv.base.widget.tab

/**
 * 滚动到顶部接口
 * 配合 Tab 切换时列表自动滚回顶部
 */
interface IScrollTop {
    fun scrollToTop()
}
```

### 4. GridSpacingItemDecoration.kt
```kotlin
package com.bbv.base.widget.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Grid 布局间距装饰器
 */
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position < spanCount) outRect.top = spacing
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) outRect.top = spacing
        }
    }
}
```

## 依赖要求
```gradle
dependencies {
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.fragment:fragment-ktx:1.6.2'
}
```

## 使用方法
```kotlin
// Grid 间距
recyclerView.addItemDecoration(
    GridSpacingItemDecoration(2, dp2px(8f), true)
)

// Fragment Tab 宿主
val tabHost = findViewById<StableFragmentTabHost>(R.id.tabHost)
tabHost.setup()
// 添加 Tab 页
tabHost.addTabItem("tab1", HomeFragment::class.java, null, tabIndicator)
tabHost.addTabItem("tab2", ProfileFragment::class.java, null, tabIndicator)
```