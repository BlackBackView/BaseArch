package com.bbv.base.net.exception

/**
 * 网络请求错误类型
 */
enum class ErrorKind {
    /** 网络 IO 错误 */
    NETWORK,

    /** 非 200 HTTP 状态码 */
    HTTP,

    /** 服务端返回的业务错误 */
    SERVER,

    /** 未知内部错误 */
    UNEXPECTED,

    /** 数据解析错误 */
    DATA_PARSE
}
