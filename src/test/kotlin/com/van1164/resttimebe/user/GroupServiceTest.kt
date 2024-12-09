package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.GROUP_NOT_FOUND
import com.van1164.resttimebe.common.exception.ErrorCode.SOME_USERS_NOT_FOUND
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.GroupService
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
        val user = userRepository.save(createUser())
        val group1 = groupService.addGroupToUser(user.userId, "group1", emptyList())
        val group2 = groupService.addGroupToUser(user.userId, "group2", emptyList())

        val groups = groupService.getGroupList(user.userId)

        assertThat(groups).hasSize(2)
        assertThat(groups[0].groupId).isEqualTo(group1.groupId)
        assertThat(groups[1].groupId).isEqualTo(group2.groupId)
    }

    @Test
    fun `getGroupById should return group successfully`() {
        val user = userRepository.save(createUser())
        val group = groupService.addGroupToUser(user.userId, "group1", emptyList())

        val foundGroup = groupService.getGroupById(user.userId, group.groupId)

        assertThat(foundGroup.groupId).isEqualTo(group.groupId)
    }

    @Test
    fun `getGroupById should throw exception when group not found`() {
        val user = userRepository.save(createUser())
        groupService.addGroupToUser(user.userId, "group1", emptyList())

        Assertions.assertThatThrownBy {
            groupService.getGroupById(user.userId, "notFoundGroupId")
        }.isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(GROUP_NOT_FOUND.message)
    }

    @Test
    fun `addGroupToUser should add group successfully`() {
        val user = userRepository.save(createUser())

        val newGroup = groupService.addGroupToUser(user.userId, "group1", emptyList())

        assertThat(newGroup.groupName).isEqualTo("group1")
        assertThat(newGroup.userIdList).isEmpty()
    }

    @Test
    fun `addMembersToGroup should add members to group successfully`() {
        val user = userRepository.save(createUser())
        val group = groupService.addGroupToUser(user.userId, "group1", emptyList())
        val (member1, member2) = userRepository.saveAll(listOf(createUser(), createUser()))

        val result =
            groupService.addMembersToGroup(user.userId, group.groupId, listOf(member1.userId, member2.userId))

        assertThat(result.groupId).isEqualTo(group.groupId)
        assertThat(result.previousTotalMembers).isEqualTo(0)
        assertThat(result.previousMemberIdList).isEmpty()
        assertThat(result.currentTotalMembers).isEqualTo(2)
        assertThat(result.currentMemberIdList).containsExactlyInAnyOrder(member1.userId, member2.userId)
    }

    @Test
    fun `addMembersToGroup should throw exception when some members not found`() {
        val user = userRepository.save(createUser())
        val group = groupService.addGroupToUser(user.userId, "group1", emptyList())
        val memberId = userRepository.save(createUser()).userId

        assertThatThrownBy {
            groupService.addMembersToGroup(
                user.userId,
                group.groupId,
                listOf(memberId, "notFoundMemberId")
            )
        }.isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(SOME_USERS_NOT_FOUND.message)
    }

    @Test
    fun `removeMembersFromGroup should remove members from group successfully`() {
        val user = userRepository.save(createUser())
        val (member1, member2) = userRepository.saveAll(listOf(createUser(), createUser()))
        val group = groupService.addGroupToUser(user.userId, "group1", listOf(member1.userId, member2.userId))

        val result = groupService.removeMembersFromGroup(user.userId, group.groupId, listOf(member1.userId))

        assertThat(result.groupId).isEqualTo(group.groupId)
        assertThat(result.previousTotalMembers).isEqualTo(2)
        assertThat(result.previousMemberIdList).containsExactlyInAnyOrder(member1.userId, member2.userId)
        assertThat(result.currentTotalMembers).isEqualTo(1)
        assertThat(result.currentMemberIdList).containsExactly(member2.userId)
    }

    @Test
    fun `removeMembersFromGroup should throw exception when some members not found`() {
        val user = userRepository.save(createUser())
        val (member1, member2) = userRepository.saveAll(listOf(createUser(), createUser()))
        val group = groupService.addGroupToUser(user.userId, "group1", listOf(member1.userId, member2.userId))

        assertThatThrownBy {
            groupService.removeMembersFromGroup(
                user.userId,
                group.groupId,
                listOf(member1.userId, "notFoundMemberId")
            )
        }.isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(SOME_USERS_NOT_FOUND.message)
    }

    @Test
    fun `removeGroup should remove group successfully`() {
        val user = userRepository.save(createUser())
        val group = groupService.addGroupToUser(user.userId, "group1", emptyList())

        val result = groupService.removeGroup(user.userId, group.groupId)

        assertThat(result.previousTotalGroups).isEqualTo(1)
        assertThat(result.previousGroupList).containsExactly(group)
        assertThat(result.currentTotalGroups).isEqualTo(0)
        assertThat(result.currentGroupList).isEmpty()
    }

}
