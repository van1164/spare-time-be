package com.van1164.resttimebe.user

import com.van1164.resttimebe.domain.Friend
import com.van1164.resttimebe.domain.Group
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.user.request.CreateGroupRequest
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getFriendList(userId: String): List<Friend> {
        return findById(userId).friends
    }

    fun findById(userId: String): User {
        return userRepository.findById(userId).orElseThrow {
            RuntimeException("User not found with id: $userId")
        }
    }

    fun addGroupToUser(userId: String, request: CreateGroupRequest): User {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        val newGroup = Group(groupName = request.groupName, userIdList = request.userIdList)

        val updatedGroups = user.groups + newGroup
        val updatedUser = user.copy(groups = updatedGroups)

        return userRepository.save(updatedUser)
    }

    fun addFriendToGroup(userId: String, groupId: String, friendId: String): User {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        val group = user.groups.find { it.groupId == groupId } ?: throw RuntimeException("Group not found")
        val friend = user.friends.find { it.id == friendId } ?: throw RuntimeException("Friend not found")

        val updatedGroup = group.copy(userIdList = group.userIdList + friendId)
        val updatedGroups = user.groups.map { if (it.groupId == groupId) updatedGroup else it }
        val updatedUser = user.copy(groups = updatedGroups)

        return userRepository.save(updatedUser)
    }

    fun removeFriendFromGroup(userId: String, groupId: String, friendId: String): User {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        val group = user.groups.find { it.groupId == groupId } ?: throw RuntimeException("Group not found")

        val updatedGroup = group.copy(userIdList = group.userIdList - friendId)
        val updatedGroups = user.groups.map { if (it.groupId == groupId) updatedGroup else it }
        val updatedUser = user.copy(groups = updatedGroups)

        return userRepository.save(updatedUser)
    }

    fun removeGroup(userId: String, groupId: String): User {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        val updatedGroups = user.groups.filter { it.groupId != groupId }
        val updatedUser = user.copy(groups = updatedGroups)

        return userRepository.save(updatedUser)
    }
}