package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserReadService (
    private val userRepository: UserRepository
) {
    fun getById(userId: String): User {
        return userRepository.findByUserId(userId) ?: throw GlobalExceptions.NotFoundException(USER_NOT_FOUND)
    }
}