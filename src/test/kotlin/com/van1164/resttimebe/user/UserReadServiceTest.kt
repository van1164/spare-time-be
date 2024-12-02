package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions
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

        val foundUser = userReadService.getById(user.id)

        assertThat(foundUser.id).isEqualTo(user.id)
    }

    @Test
    fun `getById should throw exception when user not found`() {
        userRepository.save(createUser())

        assertThatThrownBy { userReadService.getById("notFound") }
            .isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(USER_NOT_FOUND.message)
    }
}