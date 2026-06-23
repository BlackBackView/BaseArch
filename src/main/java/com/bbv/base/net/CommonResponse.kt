package com.bbv.base.net

/**
 * 通用 API 响应体
 * 需与服务端约定的字段名一致（code / error / msg / result）
 */
data class CommonResponse(
    val code: String,
    val error: String?,
    val msg: String,
    val result: Any?
)
