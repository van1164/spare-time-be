package com.van1164.resttimebe.common.exception

class GlobalExceptions {
    open class GlobalException(val errorCode: ErrorCode): RuntimeException(errorCode.message)
    class NotFoundException(errorCode: ErrorCode): GlobalException(errorCode)
    class InternalErrorException(errorCode: ErrorCode): GlobalException(errorCode)
    class IllegalStateException(errorCode: ErrorCode): GlobalException(errorCode)
}