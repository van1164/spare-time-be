package com.van1164.resttimebe.user

import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.UserService
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
    /*
    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `getFriendList should return correct friend list successfully`() {
        val user = userRepository.save(createUser())
        val (another1, another2) = userRepository.saveAll(listOf(createUser(), createUser()))
        userService.addFriend(user.id, another1.id, null)
        userService.addFriend(user.id, another2.id, null)

        val friendList = userService.getFriendList(user.id)

        assertThat(friendList).hasSize(2)
        assertThat(friendList.map { it.id }).containsExactlyInAnyOrder(another1.id, another2.id)
    }

    @Test
    fun `addMembersToGroup should add members to group successfully`() {
        val user = userRepository.save(createUser())
        val (another1, another2) = userRepository.saveAll(listOf(createUser(), createUser()))
        val group = userService.addGroupToUser(user.id, "group1", emptyList())

        val groupWithMembers = userService.addMembersToGroup(user.id, group.groupId, listOf(another1.id, another2.id))

        assertThat(groupWithMembers.currentMemberIdList).hasSize(2)
        assertThat(groupWithMembers.currentMemberIdList.map { it }).containsExactlyInAnyOrder(another1.id, another2.id)
    }

     */
}