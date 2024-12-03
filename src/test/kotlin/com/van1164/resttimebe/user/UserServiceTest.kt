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
        val userId = userRepository.save(createUser()).validateAndGetId()
        val (another1Id, another2Id) = userRepository.saveAll(listOf(createUser(), createUser())).map {
            it.validateAndGetId()
        }
        userService.addFriend(userId, another1Id, null)
        userService.addFriend(userId, another2Id, null)

        val friendList = userService.getFriendList(userId)

        assertThat(friendList).hasSize(2)
        assertThat(friendList.map { it.id }).containsExactlyInAnyOrder(another1Id, another2Id)
    }

    @Test
    fun `addMembersToGroup should add members to group successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val (another1Id, another2Id) = userRepository.saveAll(listOf(createUser(), createUser())).map {
            it.validateAndGetId()
        }
        val group = userService.addGroupToUser(userId, "group1", emptyList())

        val groupWithMembers = userService.addMembersToGroup(userId, group.groupId, listOf(another1Id, another2Id))

        assertThat(groupWithMembers.currentMemberIdList).hasSize(2)
        assertThat(groupWithMembers.currentMemberIdList.map { it }).containsExactlyInAnyOrder(another1Id, another2Id)
    }

}