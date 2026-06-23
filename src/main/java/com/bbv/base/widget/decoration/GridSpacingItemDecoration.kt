package com.bbv.base.widget.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * GridLayoutManager 统一间距装饰器
 *
 * - 水平相邻 item 之间间距 = [spacing] / 2（左右各一半，由 ItemDecoration 切割）
 * - 垂直相邻 item 之间间距 = [spacing]
 * - 最外列不需要外边距（由父容器 padding 控制）
 *
 * 用法：
 * ```kotlin
 * mRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, dp8))
 * ```
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
        if (position == RecyclerView.NO_POSITION) return

        val column = position % spanCount
        val halfSpacing = spacing / 2

        // 水平：每列左右各分一半间距，第一列和最后一列的外侧由 includeEdge 控制
        outRect.left = if (includeEdge || column > 0) halfSpacing else 0
        outRect.right = if (includeEdge || column < spanCount - 1) halfSpacing else 0

        // 垂直：每行底部间距，第一行顶部由 includeEdge 控制
        val row = position / spanCount
        outRect.bottom = spacing
        if (includeEdge && row == 0) {
            outRect.top = spacing
        }
    }
}
