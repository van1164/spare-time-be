package com.van1164.resttimebe.common.response

data class ErrorResponse(
    val status: Int,
    val message: String,
    val code: String
)
