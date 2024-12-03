package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions.*
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.FriendService
import com.van1164.resttimebe.util.UserIdHelper.Companion.validateAndGetId
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
        val userId = userRepository.save(createUser()).validateAndGetId()
        val (anotherId1, anotherId2) = userRepository.saveAll(listOf(createUser(), createUser()))
            .map {
                it.validateAndGetId()
            }
        friendService.addFriend(userId, anotherId1, null)
        friendService.addFriend(userId, anotherId2, null)

        val friendList = friendService.getFriendList(userId)

        assertThat(friendList).hasSize(2)
        assertThat(friendList.map { it.id }).containsExactlyInAnyOrder(anotherId1, anotherId2)
    }

    @Test
    fun `getFriendById should return correct friend successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val anotherId = userRepository.save(createUser()).validateAndGetId()
        friendService.addFriend(userId, anotherId, null)

        val friend = friendService.getFriendById(userId, anotherId)

        assertThat(friend.id).isEqualTo(anotherId)
    }

    @Test
    fun `getFriendById should throw exception when friend not found`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val anotherId = userRepository.save(createUser()).validateAndGetId()
        friendService.addFriend(userId, anotherId, null)

        assertThatThrownBy { friendService.getFriendById(userId, "notFound") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage(SOME_USERS_NOT_FOUND.message)
    }

    @Test
    fun `addFriend should add friend successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val anotherId = userRepository.save(createUser()).validateAndGetId()

        val friend = friendService.addFriend(userId, anotherId, null)

        assertThat(friend.id).isEqualTo(anotherId)
    }

    @Test
    fun `addFriend should throw exception when friend not found`() {
        val userId = userRepository.save(createUser()).validateAndGetId()

        assertThatThrownBy { friendService.addFriend(userId, "notFound", null) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage(USER_NOT_FOUND.message)
    }

    @Test
    fun `addFriend should throw exception when already friend`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val anotherId = userRepository.save(createUser()).validateAndGetId()
        friendService.addFriend(userId, anotherId, null)

        assertThatThrownBy { friendService.addFriend(userId, anotherId, null) }
            .isInstanceOf(InternalErrorException::class.java)
            .hasMessage(FRIEND_ALREADY_EXIST.message)
    }

    @Test
    fun `addFriend should use default friend name when friend name is null`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val another = userRepository.save(createUser())
        val anotherId = another.validateAndGetId()

        val friend = friendService.addFriend(userId, anotherId, null)

        assertThat(friend.displayName).isEqualTo(another.name)
    }

    @Test
    fun `addFriend should use custom friend name when friend name is not null`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val anotherId = userRepository.save(createUser()).validateAndGetId()

        val friend = friendService.addFriend(userId, anotherId, "customName")

        assertThat(friend.displayName).isEqualTo("customName")
    }

    @Test
    fun `removeFriend should remove friend successfully`() {
        val userId = userRepository.save(createUser()).validateAndGetId()
        val anotherId = userRepository.save(createUser()).validateAndGetId()
        friendService.addFriend(userId, anotherId, null)

        friendService.removeFriend(userId, anotherId)

        val friendList = friendService.getFriendList(userId)
        assertThat(friendList).isEmpty()
    }

    //TODO: 잘못된 friendId가 들어올 경우 예외처리하는 것을 검토 중
}