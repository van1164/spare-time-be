package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("users")
data class User(
    @Id val id: String,
    val name : String,
    val friends: List<Friend> = emptyList(),
    val groups: List<Group> = emptyList(),
    val categories: List<Category> = emptyList(),
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

data class Category(
    val categoryId : String = UUID.randomUUID().toString(),
    val categoryName : String,
    val userId : String,
    val color : String,
)
