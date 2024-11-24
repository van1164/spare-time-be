package com.van1164.resttimebe.fixture

import com.van1164.resttimebe.domain.User
import java.util.*

class UserFixture {
    companion object {
        fun createUser(): User {
            return User (
                id = UUID.randomUUID().toString(),
                name = "test",
                friends = emptyList(),
                groups = emptyList(),
            )
        }
    }
}