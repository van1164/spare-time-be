package com.van1164.resttimebe.common.exception

import com.van1164.resttimebe.common.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(GlobalExceptions.NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotfoundException(
        e: GlobalExceptions.NotFoundException
    ) : ResponseEntity<ApiResponse<Nothing>> {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.with(
                HttpStatus.NOT_FOUND,
                e.message ?: "",
                null
            )
        )
    }
}