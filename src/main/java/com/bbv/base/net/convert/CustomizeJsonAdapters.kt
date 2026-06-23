package com.bbv.base.net.convert

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableDouble
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.databinding.ObservableLong
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Moshi 自定义适配器集合
 *
 * 支持在 Moshi 中直接解析/序列化 DataBinding 的 Observable 类型和 LiveData 类型。
 * 这样可以在 API 响应中直接映射到可观察字段，便于 DataBinding 绑定。
 */
class CustomizeJsonAdapters {

    // ==================== Observable 类型适配器 ====================

    @FromJson
    fun observableBooleanFromJson(reader: JsonReader): ObservableBoolean? {
        return tryObservableDataNull(reader,
            { ObservableBoolean(reader.nextBoolean()) },
            ObservableBoolean(false))
    }

    @ToJson
    fun observableBooleanToJson(writer: JsonWriter, value: ObservableBoolean?) {
        writer.value(value?.get())
    }

    @FromJson
    fun observableIntFromJson(reader: JsonReader): ObservableInt? {
        return tryObservableDataNull(reader,
            { ObservableInt(reader.nextInt()) },
            ObservableInt(0))
    }

    @ToJson
    fun observableIntToJson(writer: JsonWriter, value: ObservableInt?) {
        writer.value(value?.get())
    }

    @FromJson
    fun observableLongFromJson(reader: JsonReader): ObservableLong? {
        return tryObservableDataNull(reader,
            { ObservableLong(reader.nextLong()) },
            ObservableLong(0L))
    }

    @ToJson
    fun observableLongToJson(writer: JsonWriter, value: ObservableLong?) {
        writer.value(value?.get())
    }

    @FromJson
    fun observableDoubleFromJson(reader: JsonReader): ObservableDouble? {
        return tryObservableDataNull(reader,
            { ObservableDouble(reader.nextDouble()) },
            ObservableDouble(0.0))
    }

    @ToJson
    fun observableDoubleToJson(writer: JsonWriter, value: ObservableDouble?) {
        writer.value(value?.get())
    }

    @FromJson
    fun observableStringFromJson(reader: JsonReader): ObservableField<String>? {
        return tryObservableDataNull(reader,
            { ObservableField(reader.nextString()) },
            ObservableField(""))
    }

    @ToJson
    fun observableStringToJson(writer: JsonWriter, value: ObservableField<String>?) {
        writer.value(value?.get())
    }

    // ==================== LiveData 类型适配器 ====================

    @FromJson
    fun liveDataBooleanFromJson(reader: JsonReader): MutableLiveData<Boolean>? {
        return tryMutableLiveDataNull(reader,
            { MutableLiveData(reader.nextBoolean()) },
            MutableLiveData(false))
    }

    @ToJson
    fun liveDataBooleanToJson(writer: JsonWriter, value: MutableLiveData<Boolean>?) {
        writer.value(value?.value)
    }

    @FromJson
    fun liveDataIntFromJson(reader: JsonReader): MutableLiveData<Int>? {
        return tryMutableLiveDataNull(reader,
            { MutableLiveData(reader.nextInt()) },
            MutableLiveData(0))
    }

    @ToJson
    fun liveDataIntToJson(writer: JsonWriter, value: MutableLiveData<Int>?) {
        writer.value(value?.value)
    }

    @FromJson
    fun liveDataLongFromJson(reader: JsonReader): MutableLiveData<Long>? {
        return tryMutableLiveDataNull(reader,
            { MutableLiveData(reader.nextLong()) },
            MutableLiveData(0L))
    }

    @ToJson
    fun liveDataLongToJson(writer: JsonWriter, value: MutableLiveData<Long>?) {
        writer.value(value?.value)
    }

    @FromJson
    fun liveDataDoubleFromJson(reader: JsonReader): MutableLiveData<Double>? {
        return tryMutableLiveDataNull(reader,
            { MutableLiveData(reader.nextDouble()) },
            MutableLiveData(0.0))
    }

    @ToJson
    fun liveDataDoubleToJson(writer: JsonWriter, value: MutableLiveData<Double>?) {
        writer.value(value?.value)
    }

    @FromJson
    fun liveDataStringFromJson(reader: JsonReader): MutableLiveData<String>? {
        return tryMutableLiveDataNull(reader,
            { MutableLiveData(reader.nextString()) },
            MutableLiveData(""))
    }

    @ToJson
    fun liveDataStringToJson(writer: JsonWriter, value: MutableLiveData<String>?) {
        writer.value(value?.value)
    }

    // ==================== 内部辅助 ====================

    private fun <T> tryMutableLiveDataNull(
        reader: JsonReader,
        block: () -> MutableLiveData<T>,
        defaultValue: MutableLiveData<T>? = null
    ): MutableLiveData<T>? {
        return try {
            block.invoke()
        } catch (e: Exception) {
            // 需要把 null 消耗掉，否则 Moshi 会报错
            MutableLiveData(reader.nextNull<T>())
        }
    }

    private fun <T> tryObservableDataNull(
        reader: JsonReader,
        block: () -> T,
        defaultValue: T? = null
    ): T? {
        return try {
            block.invoke()
        } catch (e: Exception) {
            reader.nextNull<T>()
        }
    }
}
