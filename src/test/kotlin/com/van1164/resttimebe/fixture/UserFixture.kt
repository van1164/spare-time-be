package com.van1164.resttimebe.fixture

import com.van1164.resttimebe.domain.User
import java.util.*

class UserFixture {
    companion object {
        fun createUser(): User {
            return User (
                name = "test",
                userId = UUID.randomUUID().toString(),
                email = "test@test.com",
                displayName = "testDisplay",
                friends = emptyList(),
                groups = emptyList(),
            )
        }
        fun createUser(name: String): User {
            return User (
                name = name,
                userId = UUID.randomUUID().toString(),
                email = "test@test.com",
                displayName = "testDisplay",
                friends = emptyList(),
                groups = emptyList(),
            )
        }
    }
}