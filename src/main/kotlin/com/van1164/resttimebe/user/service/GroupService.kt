package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.domain.Group
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.user.UserRepository
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
            ?: throw RuntimeException("Group not found")
    }

    fun addGroupToUser(userId: String, groupName: String, memberIdList: List<String>): User {
        val user = userReadService.getById(userId)
        val newGroup = Group(groupName = groupName, userIdList = memberIdList)
        if (userRepository.findAllById(memberIdList).size != memberIdList.size) {
            throw RuntimeException("Some members not found")
        }

        val updatedGroups = user.groups + newGroup
        val updatedUser = user.copy(groups = updatedGroups)

        return userRepository.save(updatedUser)
    }

    //TODO: 반환 타입 점검 필요

    fun addMembersToGroup(userId: String, groupId: String, memberIdList: List<String>): User {
        val user = userReadService.getById(userId)
        val group = user.groups.find { it.groupId == groupId } ?: throw RuntimeException("Group not found")
        if (userRepository.findAllById(memberIdList).size != memberIdList.size) {
            throw RuntimeException("Some members not found")
        }

        val updatedGroup = group.copy(userIdList = group.userIdList + memberIdList)
        val updatedGroups = user.groups.map { if (it.groupId == groupId) updatedGroup else it }
        val updatedUser = user.copy(groups = updatedGroups)

        return userRepository.save(updatedUser)
    }

    fun removeMembersFromGroup(userId: String, groupId: String, memberIdList: List<String>): User {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        val group = user.groups.find { it.groupId == groupId } ?: throw RuntimeException("Group not found")
        if (userRepository.findAllById(memberIdList).size != memberIdList.size) {
            throw RuntimeException("Some members not found")
        }

        val updatedGroup = group.copy(userIdList = group.userIdList - memberIdList.toSet())
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