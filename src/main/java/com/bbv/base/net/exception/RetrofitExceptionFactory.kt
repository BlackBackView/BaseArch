package com.bbv.base.net.exception

import com.bbv.base.net.CommonResponse

/**
 * RetrofitException 工厂
 */
object RetrofitExceptionFactory {

    fun createServerError(commonResponse: CommonResponse): RetrofitException {
        return RetrofitException(ErrorKind.SERVER, commonResponse = commonResponse)
    }

    fun createParseError(runtimeException: RuntimeException): RetrofitException {
        return RetrofitException(ErrorKind.DATA_PARSE, runtimeException = runtimeException)
    }

    fun createNetworkError(runtimeException: RuntimeException): RetrofitException {
        return RetrofitException(ErrorKind.NETWORK, runtimeException = runtimeException)
    }
}
