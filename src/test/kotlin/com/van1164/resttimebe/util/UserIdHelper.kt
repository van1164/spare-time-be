package com.van1164.resttimebe.util

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions.*
import com.van1164.resttimebe.domain.User

class UserIdHelper {
    companion object {
        fun User?.validateAndGetId(): String {
            return this?.id ?: throw IllegalStateException(USER_ID_NOT_INITIALIZED)
        }
    }
}