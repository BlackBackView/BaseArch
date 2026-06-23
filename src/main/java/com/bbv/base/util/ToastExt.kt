package com.bbv.base.util


import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * @Description:
 * @Author: 
 * @Date: 2024/12/25 1:21
 */


/**
 * Display the simple Toast message with the [Toast.LENGTH_SHORT] duration.
 *
 * @param messageId the message text resource.
 */
fun Fragment.toast(@StringRes messageId: Int) = activity?.toast(activity!!.getString(messageId))

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param messageId the message text resource.
 */
fun Fragment.longToast(@StringRes messageId: Int) = activity?.longToast(activity!!.getString(messageId))

/**
 * Display the simple Toast message with the [Toast.LENGTH_SHORT] duration.
 *
 * @param message the message text.
 */
fun Fragment.toast(message: CharSequence) = activity?.toast(message)

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param message the message text.
 */
fun Fragment.longToast(message: CharSequence) = activity?.longToast(message)

/**
 * context toast
 */
fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 *  context long toast
 */
fun Context.longToast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
