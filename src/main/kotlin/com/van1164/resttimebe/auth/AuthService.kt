package com.van1164.resttimebe.auth

import com.van1164.resttimebe.common.exception.ErrorCode
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.security.JwtUtil
import com.van1164.resttimebe.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
) {
    fun createNewToken(refreshToken: String): String {
        val userId = jwtUtil.extractUsername(refreshToken)
        return userRepository.findByUserId(userId).let { user ->
            if (user == null) throw GlobalExceptions.NotFoundException(ErrorCode.USER_NOT_FOUND)
            return@let jwtUtil.generateJwtToken(userId)
        }
    }
}