package com.van1164.resttimebe.user.repository

import com.van1164.resttimebe.domain.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findByUserId(userId: String): User?

}