package com.bbv.base.widget

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bbv.base.widget.tab.SimpleMenuItem
import com.bbv.base.R

/**
 * 页面 Tab 构建器（基类）
 *
 * 不继承任何 Android 组件，作为辅助类使用。
 * 基于 Fragment#show/hide 方式管理 Tab 页面，Fragment 始终存活在内存中，
 * 切换时无需重建，适合需要保持状态的场景。
 *
 * @param tabHost  布局中的 [StableFragmentTabHost] 容器
 * @param fragmentManager FragmentManager，用于管理 Tab Fragment 生命周期
 * @param context  上下文
 */
abstract class AbstractTabBuild(
    protected val tabHost: StableFragmentTabHost,
    protected val fragmentManager: FragmentManager,
    protected val context: Context
) {

    /** ViewPager2 内容容器 ID，对应 layout 中的 FrameLayout */
    abstract val layContentId: Int

    /**
     * Tab 选中颜色（文字 & 图标）
     *
     * 子类两种写法均可：
     * - `Color.parseColor("#000000")`    — 直接色值
     * - `context.getColor(R.color.xxx)`  — 资源 ID
     */
    abstract val tabSelectColor: Int

    /**
     * Tab 未选中颜色，默认灰色
     *
     * 子类两种写法均可：
     * - `Color.parseColor("#999999")`
     * - `context.getColor(R.color.tab_color_unsel)`
     */
    open val tabUnSelectColor: Int = Color.parseColor("#999999")

    /** Fragment 类列表（按顺序对应 tab） */
    abstract val fragments: Array<Class<*>>

    /** 选中图标数组（长度必须与 [fragments] 一致） */
    abstract val tabSelectIcons: Array<Int>

    /** 未选中图标数组（长度必须与 [fragments] 一致） */
    abstract val tabIcons: Array<Int>

    /** Tab 文字资源 ID 数组（长度必须与 [fragments] 一致） */
    abstract val tabTexts: Array<Int>

    /** 当前选中的 tab 位置（-1 表示未初始化，首次 switchTo 强制更新） */
    private var mCurrentPosition = -1

    /** Fragment 实例缓存 */
    private val mFragmentList = mutableListOf<Fragment>()

    /** Tab 容器 */
    private lateinit var mTabGroup: ViewGroup

    /** Tab Item 列表 */
    private val mTabItems = mutableListOf<View>()

    // ==================== 公共方法 ====================

    /**
     * Tab Item 布局 ID，子类可重写自定义
     */
    open fun getTabItemLayoutId(): Int = R.layout.layout_tab_item

    /**
     * 构建 Tab（Activity/Fragment 在 setContentView 后调用）
     */
    fun build() {
        initFragments()
        buildTabViews()
        switchTo(0)
    }

    /**
     * 切换到指定位置的 tab
     */
    fun setCurrentTab(position: Int) {
        if (position < 0 || position >= fragments.size) return
        switchTo(position)
    }

    /**
     * 获取当前 tab 位置
     */
    fun getCurrentTab(): Int = mCurrentPosition

    // ==================== 内部实现 ====================

    private fun initFragments() {
        mFragmentList.clear()
        val transaction = fragmentManager.beginTransaction()
        for ((i, clazz) in fragments.withIndex()) {
            val tag = clazz.name
            var fragment: Fragment? = fragmentManager.findFragmentByTag(tag)
            if (fragment == null) {
                fragment = fragmentManager.fragmentFactory.instantiate(
                    clazz.classLoader, tag
                )
                transaction.add(layContentId, fragment, tag)
            }
            if (i != 0) {
                transaction.hide(fragment)
            }
            mFragmentList.add(fragment)
        }
        transaction.commitNow()
    }

    private fun buildTabViews() {
        val host = tabHost
        // Tab 容器 — 约定 id 为 tab_group，位于 tabHost 布局中
        val tabGroup = host.findViewById<ViewGroup>(R.id.tab_group) ?: return
        mTabGroup = tabGroup
        mTabGroup.removeAllViews()
        mTabItems.clear()

        val inflater = LayoutInflater.from(context)
        for (i in fragments.indices) {
            val tabView = inflater.inflate(getTabItemLayoutId(), mTabGroup, false)
            tabView.tag = i
            tabView.setOnClickListener { setCurrentTab(i) }
            mTabGroup.addView(tabView)
            mTabItems.add(tabView)
        }
    }

    private fun switchTo(position: Int) {
        if (position == mCurrentPosition) return
        mCurrentPosition = position

        val transaction = fragmentManager.beginTransaction()
        for (i in mFragmentList.indices) {
            val fragment = mFragmentList[i]
            transaction.apply {
                if (i == position) show(fragment) else hide(fragment)
            }
        }
        transaction.commitNow()

        // 更新 Tab 视图状态
        updateTabViews(position)
    }

    private fun updateTabViews(position: Int) {
        for (i in mTabItems.indices) {
            val isSelected = i == position
            val tabView = mTabItems[i]

            // Tab 图标（使用 SimpleMenuItem 支持红点角标）
            val iconRes = if (isSelected) tabSelectIcons[i] else tabIcons[i]
            val tabIcon = tabView.findViewById<SimpleMenuItem>(R.id.ivTabIcon)
            tabIcon?.let { item ->
                val drawable = ContextCompat.getDrawable(context, iconRes)
                item.setIcon(drawable)
            }

            // Tab 文字
            val textTv = tabView.findViewById<TextView>(R.id.tvTabText)
            textTv?.let { tv ->
                tv.setTextColor(if (isSelected) tabSelectColor else tabUnSelectColor)
                tv.setText(tabTexts[i])
            }
        }
    }
}
