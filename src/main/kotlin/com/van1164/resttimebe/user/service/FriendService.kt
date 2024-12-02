package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.domain.Friend
import com.van1164.resttimebe.user.repository.UserRepository
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
            ?: throw GlobalExceptions.NotFoundException(SOME_USERS_NOT_FOUND)
    }

    fun addFriend(userId: String, friendId: String, friendName: String?): Friend {
        val user = userReadService.getById(userId)
        val friend = userReadService.getById(friendId).let {
            Friend(it.id, friendName ?: it.name)
        }
        if (user.friends.any { it.id == friendId }) {
            throw GlobalExceptions.InternalErrorException(FRIEND_ALREADY_EXIST)
        }

        val updatedFriends = user.friends + friend
        val updatedUser = user.copy(friends = updatedFriends)
        userRepository.save(updatedUser)

        return friend
    }

    //TODO: 잘못된 friendId가 들어올 경우 예외처리하는 것을 검토
    fun removeFriend(userId: String, friendId: String): String {
        val user = userReadService.getById(userId)
        val updatedFriends = user.friends.filter { it.id != friendId }
        val updatedUser = user.copy(friends = updatedFriends)
        userRepository.save(updatedUser)

        return friendId
    }
}