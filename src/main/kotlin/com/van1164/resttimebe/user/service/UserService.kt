package com.van1164.resttimebe.user

import com.van1164.resttimebe.user.service.FriendService
import com.van1164.resttimebe.user.service.GroupService
import com.van1164.resttimebe.user.service.UserReadService
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userReadService: UserReadService,
    private val friendService: FriendService,
    private val groupService: GroupService
) {
    // userRead
    fun getById(userId: String) = userReadService.getById(userId)

    // friend
    fun getFriendList(userId: String) = friendService.getFriendList(userId)
    fun getFriendById(userId: String, friendId: String) = friendService.getFriendById(userId, friendId)
    fun addFriend(userId: String, friendId: String, friendName: String?) = friendService.addFriend(userId, friendId, friendName)
    fun removeFriend(userId: String, friendId: String) = friendService.removeFriend(userId, friendId)

    // group
    fun getGroupList(userId: String) = groupService.getGroupList(userId)
    fun getGroupById(userId: String, groupId: String) = groupService.getGroupById(userId, groupId)
    fun addGroupToUser(userId: String, groupName: String, memberIdList: List<String>) = groupService.addGroupToUser(userId, groupName, memberIdList)
    fun addMembersToGroup(userId: String, groupId: String, memberIdList: List<String>) = groupService.addMembersToGroup(userId, groupId, memberIdList)
    fun removeMembersFromGroup(userId: String, groupId: String, memberIdList: List<String>) = groupService.removeMembersFromGroup(userId, groupId, memberIdList)
}