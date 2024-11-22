package com.van1164.resttimebe.user

import com.van1164.resttimebe.domain.Friend
import com.van1164.resttimebe.domain.User
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {


    fun getFriendList(userId: String): List<Friend> {
        return findById(userId).friends
    }

    fun findById(userId: String): User {
        return userRepository.findById(userId).orElseThrow {
            RuntimeException("User not found with id: $userId")
        }
    }
}