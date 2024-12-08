package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions.*
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.FriendService
import com.van1164.resttimebe.util.DatabaseIdHelper.Companion.validateAndGetId
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
    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `getFriendList should return correct friend list successfully`() {
        val user = userRepository.save(createUser())
        val (other1, other2) = userRepository.saveAll(listOf(createUser(), createUser()))
        friendService.addFriend(user.userId, other1.userId)
        friendService.addFriend(user.userId, other2.userId)

        val friendList = friendService.getFriendList(user.userId)

        assertThat(friendList).hasSize(2)
        assertThat(friendList.map { it.id }).containsExactlyInAnyOrder(other1.userId, other2.userId)
    }

    @Test
    fun `getFriendById should return correct friend successfully`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.userId, another.userId)

        val friend = friendService.getFriendById(user.userId, another.userId)

        assertThat(friend.id).isEqualTo(another.userId)
    }

    @Test
    fun `getFriendById should throw exception when friend not found`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.userId, another.userId, null)

        assertThatThrownBy { friendService.getFriendById(user.userId, "notFound") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage(SOME_USERS_NOT_FOUND.message)
    }

    @Test
    fun `addFriend should add friend successfully`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())

        val friend = friendService.addFriend(user.userId, another.userId)

        assertThat(friend.id).isEqualTo(another.userId)
    }

    @Test
    fun `addFriend should throw exception when friend not found`() {
        val user = userRepository.save(createUser())

        assertThatThrownBy { friendService.addFriend(user.userId, "notFound") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage(USER_NOT_FOUND.message)
    }

    @Test
    fun `addFriend should throw exception when already friend`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.userId, another.userId)

        assertThatThrownBy { friendService.addFriend(user.userId, another.userId) }
            .isInstanceOf(InternalErrorException::class.java)
            .hasMessage(FRIEND_ALREADY_EXIST.message)
    }

    @Test
    fun `addFriend should use default friend name when friend name is null`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())

        val friend = friendService.addFriend(user.userId, another.userId)

        assertThat(friend.displayName).isEqualTo(another.name)
    }

    @Test
    fun `addFriend should use custom friend name when friend name is not null`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())

        val friend = friendService.addFriend(user.userId, another.userId, "customName")

        assertThat(friend.displayName).isEqualTo("customName")
    }

    @Test
    fun `removeFriend should remove friend successfully`() {
        val user = userRepository.save(createUser())
        val another = userRepository.save(createUser())
        friendService.addFriend(user.userId, another.userId)

        friendService.removeFriend(user.userId, another.userId)

        val friendList = friendService.getFriendList(user.userId)
        assertThat(friendList).isEmpty()
    }

    //TODO: 잘못된 friendId가 들어올 경우 예외처리하는 것을 검토 중
}