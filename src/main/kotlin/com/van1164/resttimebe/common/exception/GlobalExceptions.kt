package com.van1164.resttimebe.common.exception

class GlobalExceptions {
    open class GlobalException(val errorCode: ErrorCode): RuntimeException()
    class NotFoundException(errorCode: ErrorCode): GlobalException(errorCode)
}