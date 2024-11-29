package com.van1164.resttimebe.common.exception

import com.van1164.resttimebe.common.response.ApiResponse
import com.van1164.resttimebe.common.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(GlobalExceptions.NotFoundException::class)
    fun handleNotfoundException(
        e: GlobalExceptions.NotFoundException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.valueOf(e.errorCode.status)).body(
            ErrorResponse.of(e.errorCode)
        )
    }

    @ExceptionHandler(GlobalExceptions.InternalErrorException::class)
    fun handleInternalErrorException(
        e: GlobalExceptions.InternalErrorException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.valueOf(e.errorCode.status)).body(
            ErrorResponse.of(e.errorCode)
        )
    }
}