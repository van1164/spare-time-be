package com.van1164.resttimebe.common.response

import com.van1164.resttimebe.common.exception.ErrorCode

data class ErrorResponse(
    val status: Int,
    val message: String,
    val code: String
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status,
                message = errorCode.message,
                code = errorCode.code
            )
        }
    }
}
