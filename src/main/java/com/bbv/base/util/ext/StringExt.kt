package com.bbv.base.util.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Base64
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Pattern


//<editor-fold desc="BigDecimal  ">
fun BigDecimal?.toPriceScale(scale: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
    return this?.setScale(scale, roundingMode)?.toPlainString() ?: "0.00"
}

fun BigDecimal?.toPriceInt(roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
    return this?.setScale(0, roundingMode)?.toPlainString() ?: "0"
}

fun BigDecimal?.toPriceStripTrailingZeros(scale: Int = 0): String {
    return this?.setScale(scale, RoundingMode.HALF_UP)?.stripTrailingZeros()?.toPlainString() ?: "0"
}
//</editor-fold>



//<editor-fold desc="String    ">
fun String?.toBigDecimalOrZERO(): BigDecimal {
    return this?.toBigDecimalOrNull() ?: BigDecimal.ZERO
}

fun String?.toBigDecimalOrTEN(): BigDecimal {
    return this?.toBigDecimalOrNull() ?: BigDecimal.TEN
}

fun String?.toBigDecimalOrONE(): BigDecimal {
    return this?.toBigDecimalOrNull() ?: BigDecimal.ONE
}


fun String?.toPriceScale(scale: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
   return this.toBigDecimalOrZERO().toPriceScale(scale, roundingMode)
}

fun String?.toPriceInt(roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
   return this.toBigDecimalOrZERO().toPriceInt(roundingMode)
}

fun String?.toPriceStripTrailingZeros(scale: Int = 0): String {
   return this.toBigDecimalOrZERO().toPriceStripTrailingZeros(scale)
}

//</editor-fold>



//<editor-fold desc="Double   ">
fun Double?.toBigDecimalOrZERO(): BigDecimal {
    return this?.toBigDecimal() ?: BigDecimal.ZERO
}

fun Double?.toBigDecimalOrTEN(): BigDecimal {
    return this?.toBigDecimal()?: BigDecimal.TEN
}

fun Double?.toBigDecimalOrONE(): BigDecimal {
    return this?.toBigDecimal() ?: BigDecimal.ONE
}


fun Double?.toPriceScale(scale: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
    return this.toBigDecimalOrZERO().toPriceScale(scale, roundingMode)
}

fun Double?.toPriceInt(roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
    return this.toBigDecimalOrZERO().toPriceInt(roundingMode)
}

fun Double?.toPriceStripTrailingZeros(scale: Int = 0): String {
    return this.toBigDecimalOrZERO().toPriceStripTrailingZeros(scale)
}

//</editor-fold>



//<editor-fold desc="Float   ">
fun Float?.toBigDecimalOrZERO(): BigDecimal {
    return this?.toBigDecimal() ?: BigDecimal.ZERO
}

fun Float?.toBigDecimalOrTEN(): BigDecimal {
    return this?.toBigDecimal()?: BigDecimal.TEN
}

fun Float?.toBigDecimalOrONE(): BigDecimal {
    return this?.toBigDecimal() ?: BigDecimal.ONE
}



fun Float?.toPriceScale(scale: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
    return this.toBigDecimalOrZERO().toPriceScale(scale, roundingMode)
}

fun Float?.toPriceInt(roundingMode: RoundingMode = RoundingMode.HALF_UP): String {
    return this.toBigDecimalOrZERO().toPriceInt(roundingMode)
}

fun Float?.toPriceStripTrailingZeros(scale: Int = 0): String {
    return this.toBigDecimalOrZERO().toPriceStripTrailingZeros(scale)
}
//</editor-fold>





fun Any.toGson(): String {
    return try {
        Gson().toJson(this)
    } catch (e: Exception) {
        com.bbv.base.util.Lg.e("toGson error: ${e.message}")
        "Gson解析失败"
    }
}

fun Any?.toStringOrNull(): String? {
    return if (this?.toString().isNullOrEmpty()) null else this?.toString()
}


/**
 * bitmap转为base64
 * @param bitmap
 * @return
 */
fun bitmapToBase64(bitmap: Bitmap?): String? {
    var result: String? = null
    var baos: ByteArrayOutputStream? = null
    try {
        if (bitmap != null) {
            baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val bitmapBytes = baos.toByteArray()
            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            if (baos != null) {
                baos.flush()
                baos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return result
}


/**
 * base64转为bitmap
 * @return
 */
fun base64ToBitmap(base64Data: String?): Bitmap? {
    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}


//<editor-fold desc="手机号正则">
/**
 * 手机号正则表达式
 */
private val NUMBER_PATTERN = Pattern.compile("^(1)\\d{10}$")

/**
 * 是否是手机号
 */
fun String?.isNotPhone(): Boolean {
    return !this.isPhone()
}

fun String?.isPhone(): Boolean {
    if (TextUtils.isEmpty(this)) return false
    val m = NUMBER_PATTERN.matcher(this?:"")
    return m.matches()
}

fun showPhoneHide(phone: String): String {
    if (phone.isNotEmpty()) {
        val length = phone.length
        if (length >= 11) {
            val begin = phone.substring(0, 3)
            val end = phone.substring(length - 3, length)
            return "$begin*****$end"
        }
    }
    return phone
}
//</editor-fold>
