package com.bbv.base.net.exception

import com.bbv.base.net.CommonResponse

/**
 * Retrofit 请求异常
 *
 * @property errorKind 错误类型
 * @property commonResponse 服务端返回的错误响应（可能为 null）
 * @property runtimeException 原始运行时异常（可能为 null）
 */
class RetrofitException(
    val errorKind: ErrorKind,
    val commonResponse: CommonResponse? = null,
    val runtimeException: RuntimeException? = null
) : RuntimeException(runtimeException)
