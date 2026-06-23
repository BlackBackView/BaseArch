package com.bbv.base.web

import com.bbv.base.mvvm.BaseViewModel

class WebPageViewModel : BaseViewModel() {

    private var mViewModelAction: ViewModelAction? = null

    fun setViewModelAction(action: ViewModelAction) {
        mViewModelAction = action
    }

    interface ViewModelAction
}
