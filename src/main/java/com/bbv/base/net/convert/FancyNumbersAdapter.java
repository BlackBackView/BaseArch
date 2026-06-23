package com.bbv.base.net.convert;

import androidx.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Moshi 数字解析适配器
 * <p>
 * 解决 Moshi 默认将 Object 类型中的数字一律解析为 Double 的问题。
 * 适配后：有小数位 → Double，无小数位 → Int/Long。
 * <p>
 * 参考：<a href="https://gist.github.com/swankjesse/0331f24df1aa25454eaae04e49fe2f19">官方方案</a>
 */
public final class FancyNumbersAdapter extends JsonAdapter<Object> {

    public static final Factory FACTORY = new Factory() {
        @Override
        public JsonAdapter<?> create(
                Type type, Set<? extends Annotation> annotations, Moshi moshi) {
            if (type != Object.class) return null;
            if (!annotations.isEmpty()) return null;
            JsonAdapter<Object> delegate = moshi.nextAdapter(this, Object.class, annotations);
            return new FancyNumbersAdapter(delegate);
        }
    };

    private final JsonAdapter<Object> delegate;

    FancyNumbersAdapter(JsonAdapter<Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable Object fromJson(JsonReader reader) throws IOException {
        if (reader.peek() != JsonReader.Token.NUMBER) {
            return delegate.fromJson(reader);
        }

        BigDecimal decimal = new BigDecimal(reader.nextString());
        if (decimal.scale() > 0) {
            return decimal.doubleValue();
        }
        try {
            return decimal.intValueExact();
        } catch (ArithmeticException e1) {
            try {
                return decimal.longValueExact();
            } catch (ArithmeticException e2) {
                return decimal.doubleValue();
            }
        }
    }

    @Override
    public void toJson(JsonWriter writer, @Nullable Object value) throws IOException {
        delegate.toJson(writer, value);
    }
}
