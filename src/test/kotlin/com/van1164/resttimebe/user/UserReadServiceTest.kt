package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.SOME_USERS_NOT_FOUND
import com.van1164.resttimebe.common.exception.ErrorCode.USER_NOT_FOUND
import com.van1164.resttimebe.common.exception.GlobalExceptions.NotFoundException
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.UserReadService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserReadServiceTest @Autowired constructor(
    private val userReadService: UserReadService,
    private val userRepository: UserRepository
) {
    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `getById should return correct user successfully`() {
        val user = userRepository.save(createUser())

        val foundUser = userReadService.getById(user.userId)

        assertThat(foundUser.id).isNotNull()
        assertThat(foundUser.userId).isEqualTo(user.userId)
    }

    @Test
    fun `getById should throw exception when user not found`() {
        userRepository.save(createUser())

        assertThatThrownBy { userReadService.getById("notFound") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage(USER_NOT_FOUND.message)
    }

    @Test
    fun `getAllByUserIdList should return correct users successfully`() {
        val user1 = userRepository.save(createUser())
        val user2 = userRepository.save(createUser())
        val user3 = userRepository.save(createUser())

        val users = userReadService.getAllByUserIdList(listOf(user1.userId, user2.userId, user3.userId))

        assertThat(users).hasSize(3)
        assertThat(users.map { it.userId }).containsExactlyInAnyOrder(user1.userId, user2.userId, user3.userId)
    }

}