package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.FriendService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FriendServiceTest @Autowired constructor(
    private val friendService: FriendService,
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
        friendService.addFriend(user.id, another1.id, null)
        friendService.addFriend(user.id, another2.id, null)

        val friendList = friendService.getFriendList(user.id)

        assertThat(friendList).hasSize(2)
        assertThat(friendList.map { it.id }).containsExactlyInAnyOrder(another1.id, another2.id)
    }

    @Test
    fun `getFriendById should return correct friend successfully`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.id, another.id, null)

        val friend = friendService.getFriendById(user.id, another.id)

        assertThat(friend.id).isEqualTo(another.id)
    }

    @Test
    fun `getFriendById should throw exception when friend not found`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.id, another.id, null)

        assertThatThrownBy { friendService.getFriendById(user.id, "notFound") }
            .isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(SOME_USERS_NOT_FOUND.message)
    }

    @Test
    fun `addFriend should add friend successfully`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())

        val friend = friendService.addFriend(user.id, another.id, null)

        assertThat(friend.id).isEqualTo(another.id)
    }

    @Test
    fun `addFriend should throw exception when friend not found`() {
        val user = userRepository.save(createUser())

        assertThatThrownBy { friendService.addFriend(user.id, "notFound", null) }
            .isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(USER_NOT_FOUND.message)
    }

    @Test
    fun `addFriend should throw exception when already friend`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.id, another.id, null)

        assertThatThrownBy { friendService.addFriend(user.id, another.id, null) }
            .isInstanceOf(GlobalExceptions.InternalErrorException::class.java)
            .hasMessage(FRIEND_ALREADY_EXIST.message)
    }

    @Test
    fun `addFriend should use default friend name when friend name is null`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())

        val friend = friendService.addFriend(user.id, another.id, null)

        assertThat(friend.displayName).isEqualTo(another.name)
    }

    @Test
    fun `addFriend should use custom friend name when friend name is not null`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())

        val friend = friendService.addFriend(user.id, another.id, "customName")

        assertThat(friend.displayName).isEqualTo("customName")
    }

    @Test
    fun `removeFriend should remove friend successfully`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.id, another.id, null)

        friendService.removeFriend(user.id, another.id)

        val friendList = friendService.getFriendList(user.id)
        assertThat(friendList).isEmpty()
    }

    //TODO: 잘못된 friendId가 들어올 경우 예외처리하는 것을 검토 중
*/
}