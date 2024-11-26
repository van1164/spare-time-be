package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.domain.Friend
import com.van1164.resttimebe.user.UserRepository
import org.springframework.stereotype.Service

@Service
class FriendService(
    private val userReadService: UserReadService,
    private val userRepository: UserRepository
) {
    fun getFriendList(userId: String): List<Friend> {
        return userReadService.getById(userId).friends
    }

    fun getFriendById(userId: String, friendId: String): Friend {
        return userReadService.getById(userId).friends.find { it.id == friendId }
            ?: throw RuntimeException("Friend not found")
    }

    fun addFriend(userId: String, friendId: String, friendName: String?): Friend {
        val user = userReadService.getById(userId)
        val friend = userReadService.getById(friendId).let {
            Friend(it.id, friendName ?: it.name)
        }
        if (user.friends.any { it.id == friendId }) {
            throw RuntimeException("Already friend")
        }

        val updatedFriends = user.friends + friend
        val updatedUser = user.copy(friends = updatedFriends)
        userRepository.save(updatedUser)

        return friend
    }

    fun removeFriend(userId: String, friendId: String): String {
        val user = userReadService.getById(userId)
        val updatedFriends = user.friends.filter { it.id != friendId }
        val updatedUser = user.copy(friends = updatedFriends)
        userRepository.save(updatedUser)

        return friendId
    }
}