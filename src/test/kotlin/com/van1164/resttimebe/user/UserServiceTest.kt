package com.van1164.resttimebe.user

import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.UserService
import com.van1164.resttimebe.util.DatabaseIdHelper.Companion.validateAndGetId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository
) {
    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `getFriendList should return correct friend list successfully`() {
        val user = userRepository.save(createUser())
        val (other1, other2) = userRepository.saveAll(listOf(createUser(), createUser()))
        userService.addFriend(user.userId, other1.userId, null)
        userService.addFriend(user.userId, other2.userId, null)

        val friendList = userService.getFriendList(user.userId)

        assertThat(friendList).hasSize(2)
        assertThat(friendList.map { it.id }).containsExactlyInAnyOrder(other1.userId, other2.userId)
    }

    @Test
    fun `addMembersToGroup should add members to group successfully`() {
        val user = userRepository.save(createUser())
        val (other1, other2) = userRepository.saveAll(listOf(createUser(), createUser()))
        val group = userService.addGroupToUser(user.userId, "group1", emptyList())

        val groupWithMembers = userService.addMembersToGroup(user.userId, group.groupId, listOf(other1.userId, other2.userId))

        assertThat(groupWithMembers.currentMemberIdList).hasSize(2)
        assertThat(groupWithMembers.currentMemberIdList.map { it }).containsExactlyInAnyOrder(other1.userId, other2.userId)
    }

}