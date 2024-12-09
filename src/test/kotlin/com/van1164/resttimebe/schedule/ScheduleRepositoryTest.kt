package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ScheduleRepositoryTest @Autowired constructor (
    private val userRepository: UserRepository
){
    @Test
    fun `database connection should be successful`() {
        val user = createUser()

        val foundUser = userRepository.save(user)

        assertThat(foundUser.id).isNotNull()
    }
}