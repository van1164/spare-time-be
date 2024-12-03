package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.GroupService
import com.van1164.resttimebe.util.DatabaseIdHelper.Companion.validateAndGetId
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GroupServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val groupService: GroupService
) {
    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `getGroupList should return all groups successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val group1 = groupService.addGroupToUser(userId, "group1", emptyList())
        val group2 = groupService.addGroupToUser(userId, "group2", emptyList())

        val groups = groupService.getGroupList(userId)

        assertThat(groups).hasSize(2)
        assertThat(groups[0].groupId).isEqualTo(group1.groupId)
        assertThat(groups[1].groupId).isEqualTo(group2.groupId)
    }

    @Test
    fun `getGroupById should return group successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val group = groupService.addGroupToUser(userId, "group1", emptyList())

        val foundGroup = groupService.getGroupById(userId, group.groupId)

        assertThat(foundGroup.groupId).isEqualTo(group.groupId)
    }

    @Test
    fun `getGroupById should throw exception when group not found`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        groupService.addGroupToUser(userId, "group1", emptyList())

        Assertions.assertThatThrownBy {
            groupService.getGroupById(userId, "notFoundGroupId")
        }.isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(GROUP_NOT_FOUND.message)
    }

    @Test
    fun `addGroupToUser should add group successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()

        val newGroup = groupService.addGroupToUser(userId, "group1", emptyList())

        assertThat(newGroup.groupName).isEqualTo("group1")
        assertThat(newGroup.userIdList).isEmpty()
    }

    @Test
    fun `addMembersToGroup should add members to group successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val group = groupService.addGroupToUser(userId, "group1", emptyList())
        val (member1Id, member2Id) = userRepository.saveAll(listOf(createUser(), createUser())).map {
            it.validateAndGetId()
        }

        val result =
            groupService.addMembersToGroup(userId, group.groupId, listOf(member1Id, member2Id))

        assertThat(result.groupId).isEqualTo(group.groupId)
        assertThat(result.previousTotalMembers).isEqualTo(0)
        assertThat(result.previousMemberIdList).isEmpty()
        assertThat(result.currentTotalMembers).isEqualTo(2)
        assertThat(result.currentMemberIdList).containsExactlyInAnyOrder(member1Id, member2Id)
    }

    @Test
    fun `addMembersToGroup should throw exception when some members not found`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val group = groupService.addGroupToUser(userId, "group1", emptyList())
        val memberId = userRepository.save(createUser()).validateAndGetId()

        assertThatThrownBy {
            groupService.addMembersToGroup(
                userId,
                group.groupId,
                listOf(memberId, "notFoundMemberId")
            )
        }.isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(SOME_USERS_NOT_FOUND.message)
    }

    @Test
    fun `removeMembersFromGroup should remove members from group successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val (member1Id, member2Id) = userRepository.saveAll(listOf(createUser(), createUser())).map {
            it.validateAndGetId()
        }
        val group = groupService.addGroupToUser(userId, "group1", listOf(member1Id, member2Id))

        val result = groupService.removeMembersFromGroup(userId, group.groupId, listOf(member1Id))

        assertThat(result.groupId).isEqualTo(group.groupId)
        assertThat(result.previousTotalMembers).isEqualTo(2)
        assertThat(result.previousMemberIdList).containsExactlyInAnyOrder(member1Id, member2Id)
        assertThat(result.currentTotalMembers).isEqualTo(1)
        assertThat(result.currentMemberIdList).containsExactly(member2Id)
    }

    @Test
    fun `removeMembersFromGroup should throw exception when some members not found`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val (member1Id, member2Id) = userRepository.saveAll(listOf(createUser(), createUser())).map {
            it.validateAndGetId()
        }
        val group = groupService.addGroupToUser(userId, "group1", listOf(member1Id, member2Id))

        assertThatThrownBy {
            groupService.removeMembersFromGroup(
                userId,
                group.groupId,
                listOf(member1Id, "notFoundMemberId")
            )
        }.isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(SOME_USERS_NOT_FOUND.message)
    }

    @Test
    fun `removeGroup should remove group successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val group = groupService.addGroupToUser(userId, "group1", emptyList())

        val result = groupService.removeGroup(userId, group.groupId)

        assertThat(result.previousTotalGroups).isEqualTo(1)
        assertThat(result.previousGroupList).containsExactly(group)
        assertThat(result.currentTotalGroups).isEqualTo(0)
        assertThat(result.currentGroupList).isEmpty()
    }

}
