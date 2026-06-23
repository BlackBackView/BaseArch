package com.bbv.base.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * 稳定的 Fragment Tab 宿主容器
 *
 * 作为 [AbstractTabBuild] 的宿主容器出现在 Activity 布局中，
 * 主要负责标识 Fragment 内容区域的位置和类型。实际的 Fragment 切换逻辑
 * 由 [AbstractTabBuild] 通过 FragmentTransaction#show/hide 管理。
 */
class StableFragmentTabHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr)
