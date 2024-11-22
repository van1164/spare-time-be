package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "friend_add_history")
data class FriendAddHistory(
    @Id
    val id: String? = null,
    val senderId: String,
    val receiverId: String,
    val status: FriendRequestStatus
)

enum class FriendRequestStatus {
    PENDING,   // 미확인
    REJECTED,  // 거절
    ACCEPTED   // 수락
}