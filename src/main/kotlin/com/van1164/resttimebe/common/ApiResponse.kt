package com.van1164.resttimebe.common

data class ApiResponse<T> (
  val status: Int = 200,
  val message: String = "",
  val data: T? = null
) {

  companion object {
    fun <T> with(message: String, data: T?): ApiResponse<T> {
      return ApiResponse(message = message, data = data)
    }
  }
}