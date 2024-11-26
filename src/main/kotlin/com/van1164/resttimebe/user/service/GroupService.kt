package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.domain.Group
import com.van1164.resttimebe.user.UserRepository
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
            ?: throw RuntimeException("Group not found")
    }

    fun addGroupToUser(userId: String, groupName: String, memberIdList: List<String>): GroupsUpdateResult{
        val user = userReadService.getById(userId)
        val newGroup = Group(groupName = groupName, userIdList = memberIdList)
        if (userRepository.findAllById(memberIdList).size != memberIdList.size) {
            throw RuntimeException("Some members not found")
        }

        val updatedGroups = user.groups + newGroup
        val updatedUser = user.copy(groups = updatedGroups)

        userRepository.save(updatedUser)

        return GroupsUpdateResult(
            previousTotalGroups = user.groups.size,
            previousGroupList = user.groups,
            currentTotalGroups = updatedGroups.size,
            currentGroupList = updatedGroups
        )
    }

    fun addMembersToGroup(userId: String, groupId: String, memberIdList: List<String>): GroupMemberUpdateResult {
        val user = userReadService.getById(userId)
        val group = user.groups.find { it.groupId == groupId } ?: throw RuntimeException("Group not found")
        if (userRepository.findAllById(memberIdList).size != memberIdList.size) {
            throw RuntimeException("Some members not found")
        }

        val updatedGroup = group.copy(userIdList = group.userIdList + memberIdList)
        //TODO: 코드 중복 검사 필요
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

    fun removeMembersFromGroup(userId: String, groupId: String, memberIdList: List<String>): GroupMemberUpdateResult {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        val group = user.groups.find { it.groupId == groupId } ?: throw RuntimeException("Group not found")
        if (userRepository.findAllById(memberIdList).size != memberIdList.size) {
            throw RuntimeException("Some members not found")
        }

        val updatedGroup = group.copy(userIdList = group.userIdList - memberIdList.toSet())
        //TODO: 코드 중복 검사 필요
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
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
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
}