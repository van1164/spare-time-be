package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.domain.Group
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.result.GroupMemberUpdateResult
import com.van1164.resttimebe.user.result.GroupsUpdateResult
import org.springframework.stereotype.Service

@Service
class GroupService (
    private val userReadService: UserReadService,
    private val userRepository: UserRepository
){
    fun getGroupList(userId: String): List<Group> {
        return userReadService.getById(userId).groups
    }

    fun getGroupById(userId: String, groupId: String): Group {
        return userReadService.getById(userId).groups.find { it.groupId == groupId }
            ?: throw GlobalExceptions.NotFoundException(GROUP_NOT_FOUND)
    }

    fun addGroupToUser(userId: String, groupName: String, userIdList: List<String>): Group {
        val user = userReadService.getById(userId)
        checkAllUsersExist(userId, userIdList)

        val newGroup = Group(groupName = groupName, userIdList = userIdList)
        val updatedGroups = user.groups + newGroup
        val updatedUser = user.copy(groups = updatedGroups)

        userRepository.save(updatedUser)
        return newGroup
    }

    fun addMembersToGroup(userId: String, groupId: String, userIdList: List<String>): GroupMemberUpdateResult {
        val user = userReadService.getById(userId)
        val group = getGroupById(user, groupId)
        checkAllUsersExist(userId, userIdList)

        val updatedGroup = group.copy(userIdList = group.userIdList + userIdList)
        val updatedGroups = user.groups.map { if (it.groupId == groupId) updatedGroup else it }
        val updatedUser = user.copy(groups = updatedGroups)

        userRepository.save(updatedUser)
        return GroupMemberUpdateResult(
            groupId = updatedGroup.groupId,
            previousTotalMembers = group.userIdList.size,
            previousMemberIdList = group.userIdList,
            currentTotalMembers = updatedGroup.userIdList.size,
            currentMemberIdList = updatedGroup.userIdList
        )
    }

    fun removeMembersFromGroup(userId: String, groupId: String, userIdList: List<String>): GroupMemberUpdateResult {
        val user = userReadService.getById(userId)
        val group = getGroupById(user, groupId)
        checkAllUsersExist(userId, userIdList)

        val updatedGroup = group.copy(userIdList = group.userIdList - userIdList.toSet())
        val updatedGroups = user.groups.map { if (it.groupId == groupId) updatedGroup else it }
        val updatedUser = user.copy(groups = updatedGroups)

        userRepository.save(updatedUser)
        return GroupMemberUpdateResult(
            groupId = updatedGroup.groupId,
            previousTotalMembers = group.userIdList.size,
            previousMemberIdList = group.userIdList,
            currentTotalMembers = updatedGroup.userIdList.size,
            currentMemberIdList = updatedGroup.userIdList
        )
    }

    fun removeGroup(userId: String, groupId: String): GroupsUpdateResult {
        val user = userReadService.getById(userId)

        val updatedGroups = user.groups.filter { it.groupId != groupId }
        val updatedUser = user.copy(groups = updatedGroups)

        userRepository.save(updatedUser)
        return GroupsUpdateResult(
            previousTotalGroups = user.groups.size,
            previousGroupList = user.groups,
            currentTotalGroups = updatedGroups.size,
            currentGroupList = updatedGroups
        )
    }

    private fun getGroupById(user: User, groupId: String): Group {
        return user.groups.find { it.groupId == groupId }
            ?: throw GlobalExceptions.NotFoundException(GROUP_NOT_FOUND)
    }

    private fun checkAllUsersExist(userId: String, memberIdList: List<String>) {
        if (userRepository.findAllById(memberIdList).size != memberIdList.size) {
            throw GlobalExceptions.NotFoundException(SOME_USERS_NOT_FOUND)
        }
    }
}