package com.bbv.base.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

class CommonDialog : BaseDialog() {

    private var mLayoutId: Int? = null

    private var mContentView: View? = null

    fun setLayoutRes(@LayoutRes layoutId: Int, action: ((View, Dialog?) -> Unit)) = apply {
        this.mLayoutId = layoutId
        setViewCreate(action)
    }

    fun setContentView(view: View, action: ((View, Dialog?) -> Unit)) = apply {
        this.mContentView = view
        setViewCreate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = mLayoutId
        return when {
            layoutId != null -> {
                inflater.inflate(layoutId, container, false)
            }
            mContentView != null -> {
                mContentView
            }
            else -> {
                super.onCreateView(inflater, container, savedInstanceState)
            }
        }
    }
}
