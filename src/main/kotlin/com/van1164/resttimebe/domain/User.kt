package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("users")
data class User(
    @Id val id: String,
    val name : String,
    val friends: List<Friend>,
    val groups: List<Group>,
)


data class Friend(
    val id : String,
    val displayName : String,
)

data class Group(
    val groupId : String = UUID.randomUUID().toString(),
    val groupName : String,
    val userIdList : List<String>,
)