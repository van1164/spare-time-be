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

    //TODO: 코드 중복 점검 필요
    fun addFriend(userId: String, friendId: String, friendName: String?): Friend {
        val user = userReadService.getById(userId)
        val friend = userReadService.getById(friendId)
        if (user.friends.any { it.id == friendId }) {
            throw RuntimeException("Already friend")
        }

        val updatedFriends = user.friends + Friend(friend.id, friendName?: friend.name)
        val updatedUser = user.copy(friends = updatedFriends)
        userRepository.save(updatedUser)

        return Friend(friend.id, friendName?: friend.name)
    }

    //TODO: 반환 타입 점검 필요
    fun removeFriend(userId: String, friendId: String) {
        val user = userReadService.getById(userId)
        val updatedFriends = user.friends.filter { it.id != friendId }
        val updatedUser = user.copy(friends = updatedFriends)
        userRepository.save(updatedUser)
    }
}