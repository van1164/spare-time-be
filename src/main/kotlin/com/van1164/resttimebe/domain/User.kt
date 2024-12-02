package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("users")
data class User(
    @Id val id: String? = null,
    val name : String,
    @Indexed(unique = true) val userId: String,
    val displayName: String,
    val email : String,
    val friends: List<Friend> = emptyList(),
    val groups: List<Group> = emptyList(),
    var role: Role = Role.USER,
    var fcmToken: String? = null,
)
enum class Role(val key: String, val title: String) {
    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN","관리자")
}

data class Friend(
    val id : String,
    val displayName : String,
)

data class Group(
    val groupId : String = UUID.randomUUID().toString(),
    val groupName : String,
    val userIdList : List<String>,
)