package com.bbv.base.util


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Moshi
import org.koin.java.KoinJavaComponent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @Description:
 * @Author: 
 * @Date: 2025/1/8 23:02
 */

fun Context.getSp(): SharedPreferences{
    return getSharedPreferences(packageName, Context.MODE_PRIVATE)
}

fun Context.spPut(key: String, value: Any): SharedPreferences.Editor{
    return getSp().edit().apply {
        when(value){
            is Int -> putInt(key, value)
            is String -> putString(key, value)
            is Long -> putLong(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Set<*> -> putStringSet(key, value as Set<String>)
        }
    }
}

inline fun <reified T>Context.spPutJsonCommit(moshi: Moshi, key: String, value: T){
    spPutCommit(key, moshi.adapter(T::class.java).toJson(value))
}

inline fun <reified T>Context.spPutJsonApply(moshi: Moshi, key: String, value: T){
    spPutApply(key, moshi.adapter(T::class.java).toJson(value))
}

inline fun <reified T>Context.spGetJson(moshi: Moshi, key: String): T?{
    return spGetString(key)?.let { moshi.adapter(T::class.java).fromJson(it) }
}

fun Context.spPutCommit(key: String, value: Any){
    spPut(key, value).commit()
}

fun Context.spPutApply(key: String, value: Any){
    spPut(key, value).apply()
}

fun Context.spGetInt(key: String, default: Int = 0): Int{
    return getSp().getInt(key, default)
}

fun Context.spGetString(key: String, default: String? = null): String?{
    return getSp().getString(key, default)
}

fun Context.spGetBoolean(key: String, default: Boolean = false): Boolean{
    return getSp().getBoolean(key, default)
}

fun Context.spGetFloat(key: String, default: Float = 0f): Float{
    return getSp().getFloat(key, default)
}

fun Context.spGetLong(key: String, default: Long = 0L): Long{
    return getSp().getLong(key, default)
}

fun Context.spGetStringSet(key: String, default: Set<String>? = null): Set<String>?{
    return getSp().getStringSet(key, default)
}

fun Fragment.spPutCommit(key: String, value: Any){
    context?.spPutCommit(key, value)
}

fun Fragment.spPutApply(key: String, value: Any){
    context?.spPutApply(key, value)
}

fun Fragment.spGetInt(key: String, default: Int = 0): Int?{
    return context?.spGetInt(key, default)
}

fun Fragment.spGetString(key: String, default: String? = null): String?{
    return context?.spGetString(key, default)
}

fun Fragment.spGetBoolean(key: String, default: Boolean = false): Boolean?{
    return context?.spGetBoolean(key, default)
}

fun Fragment.spGetFloat(key: String, default: Float = 0f): Float?{
    return context?.spGetFloat(key, default)
}

fun Fragment.spGetLong(key: String, default: Long = 0L): Long?{
    return context?.spGetLong(key, default)
}

fun Fragment.spGetStringSet(key: String, default: Set<String>? = null): Set<String>?{
    return context?.spGetStringSet(key, default)
}

inline fun <reified T>Fragment.spPutJsonCommit(moshi: Moshi, key: String, value: T){
    spPutCommit(key, moshi.adapter(T::class.java).toJson(value))
}

inline fun <reified T>Fragment.spPutJsonApply(moshi: Moshi, key: String, value: T){
    spPutApply(key, moshi.adapter(T::class.java).toJson(value))
}

inline fun <reified T>Fragment.spGetJson(moshi: Moshi, key: String): T?{
    return spGetString(key)?.let { moshi.adapter(T::class.java).fromJson(it) }
}

class SpDelegate<T>(private val defaultValue: T, private val key: String? = null): ReadWriteProperty<Any, T>{
    private val mApp: Application by lazy {
        KoinJavaComponent.get<Application>(Application::class.java)
    }
    private var mValue: T? = null
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (mValue == null){
            val finalKey = key?: property.name
            val spValue = when(defaultValue){
                is Int -> mApp.spGetInt(finalKey, defaultValue)
                is String -> mApp.spGetString(finalKey, defaultValue)
                is Long -> mApp.spGetLong(finalKey, defaultValue)
                is Boolean -> mApp.spGetBoolean(finalKey, defaultValue)
                is Float -> mApp.spGetFloat(finalKey, defaultValue)
                is Set<*> -> mApp.spGetStringSet(finalKey, defaultValue.map { it.toString() }.toSet())
                else -> throw IllegalStateException("Unsupported type")
            } as T
            mValue = spValue
        }
        return mValue!!
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        val finalKey = key?: property.name
        if (value is Int || value is String || value is Long || value is Boolean || value is Float || value is Set<*>){
            mApp.spPutApply(finalKey, value)
        }else{
            throw IllegalStateException("Unsupported type")
        }
        mValue = value
    }

}

class SpMutableLiveData<T>(defaultValue: T, private val key: String): MutableLiveData<T>(){
    private val mApp: Application by lazy {
        KoinJavaComponent.get<Application>(Application::class.java)
    }
    init {
        val spValue = when(defaultValue){
            is Int -> mApp.spGetInt(key, defaultValue)
            is String -> mApp.spGetString(key, defaultValue)
            is Long -> mApp.spGetLong(key, defaultValue)
            is Boolean -> mApp.spGetBoolean(key, defaultValue)
            is Float -> mApp.spGetFloat(key, defaultValue)
            is Set<*> -> mApp.spGetStringSet(key, defaultValue.map { it.toString() }.toSet())
            else -> throw IllegalStateException("Unsupported type")
        } as T
        super.setValue(spValue)
    }

    private fun spSetValue(value: T){
        if (value is Int || value is String || value is Long || value is Boolean || value is Float || value is Set<*>){
            mApp.spPutApply(key, value)
        }else{
            throw IllegalStateException("Unsupported type")
        }
    }

    override fun setValue(value: T) {
        super.setValue(value)
        spSetValue(value)
    }

    override fun postValue(value: T) {
        super.postValue(value)
        spSetValue(value)
    }
}
