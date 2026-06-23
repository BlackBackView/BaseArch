package com.bbv.base.net

/**
 * 网络层全局配置
 */
object NetConfig {
    /** 服务端返回的成功 code */
    const val RESPONSE_CODE_SUCCESS = "200"

    /** 全局错误码列表（用于统一处理） */
    val GLOBAL_ERROR_CODE_LIST = mutableListOf<Int>()

    /** 全局请求头 */
    val HEADERS = mutableMapOf<String, Any>()
}
