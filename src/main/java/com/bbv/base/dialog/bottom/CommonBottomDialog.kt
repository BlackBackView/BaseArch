package com.bbv.base.dialog.bottom

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlin.properties.Delegates

class CommonBottomDialog<T : ViewDataBinding> : BaseBottomSheetDialog() {

    private lateinit var binding: T

    private var action: ((T, Dialog?) -> Unit)? = null

    fun setAction(action: (T, Dialog?) -> Unit) {
        this.action = action
    }

    private var layoutId by Delegates.notNull<Int>()

    fun setLayout(@LayoutRes layoutId: Int) {
        this.layoutId = layoutId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        action?.invoke(binding, dialog)
    }
}
