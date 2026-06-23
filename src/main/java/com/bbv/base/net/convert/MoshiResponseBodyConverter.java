package com.bbv.base.net.convert;

import com.bbv.base.net.CommonResponse;
import com.bbv.base.net.NetConfig;
import com.bbv.base.net.exception.RetrofitException;
import com.bbv.base.net.exception.RetrofitExceptionFactory;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ByteString;
import retrofit2.Converter;

/**
 * Moshi 响应体转换器
 * <p>
 * 自动将 API 响应按 {@link CommonResponse} 解析。
 * 当 code == NetConfig.RESPONSE_CODE_SUCCESS 时，提取 result 字段；
 * 否则抛出 {@link RetrofitException}。
 * 支持通过 {@link WithoutStepParse} 注解跳过包装解析。
 */
public final class MoshiResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private static final ByteString UTF8_BOM = ByteString.decodeHex("EFBBBF");
    private static final Object OBJECT = new Object();
    private static final List EMPTY_LIST = new ArrayList();

    private final JsonAdapter<T> adapter;
    private final JsonAdapter<CommonResponse> wrapperAdapter;
    private final boolean withOutParse;

    public MoshiResponseBodyConverter(JsonAdapter<T> adapter, Moshi moshi, boolean withOutParse) {
        this.adapter = adapter;
        this.wrapperAdapter = moshi.adapter(CommonResponse.class);
        this.withOutParse = withOutParse;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        BufferedSource source = value.source();
        try {
            // Skip UTF-8 BOM if present
            if (source.rangeEquals(0, UTF8_BOM)) {
                source.skip(UTF8_BOM.size());
            }
            JsonReader reader = JsonReader.of(source);
            T result;
            try {
                if (withOutParse) {
                    result = adapter.fromJson(reader);
                } else {
                    CommonResponse wrapper = wrapperAdapter.fromJson(reader);
                    if (!NetConfig.RESPONSE_CODE_SUCCESS.equals(wrapper.getCode())) {
                        throw RetrofitExceptionFactory.INSTANCE.createServerError(wrapper);
                    } else if (wrapper.getResult() == null) {
                        // Retrofit 不允许返回 null，返回空对象/列表替代
                        try {
                            return (T) EMPTY_LIST;
                        } catch (Exception e) {
                            return (T) OBJECT;
                        }
                    } else {
                        result = adapter.fromJsonValue(wrapper.getResult());
                    }
                }
            } catch (RuntimeException e) {
                if (e instanceof RetrofitException) {
                    throw e;
                } else {
                    throw RetrofitExceptionFactory.INSTANCE.createParseError(e);
                }
            }
            if (reader.peek() != JsonReader.Token.END_DOCUMENT) {
                throw new JsonDataException("JSON document was not fully consumed.");
            }
            return result;
        } catch (RuntimeException e) {
            if (e instanceof RetrofitException) {
                throw e;
            } else {
                throw RetrofitExceptionFactory.INSTANCE.createNetworkError(e);
            }
        } finally {
            value.close();
        }
    }
}
