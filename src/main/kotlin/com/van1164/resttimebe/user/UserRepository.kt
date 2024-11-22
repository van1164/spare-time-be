package com.van1164.resttimebe.user

import com.van1164.resttimebe.domain.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {

}