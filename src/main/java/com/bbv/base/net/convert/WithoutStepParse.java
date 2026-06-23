package com.bbv.base.net.convert;

import com.bbv.base.net.CommonResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 标记注解：跳过 {@link CommonResponse} 包装解析，直接解析原始 JSON。
 * <p>
 * 用于某些特殊接口（如直接返回一个简单 JSON 对象，而非标准包装格式）。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WithoutStepParse {
}
