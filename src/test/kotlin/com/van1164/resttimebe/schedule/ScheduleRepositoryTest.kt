package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.RepeatType.*
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.domain.ScheduleStatus.*
import com.van1164.resttimebe.fixture.ScheduleFixture
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.Month
import java.time.Month.*
import java.time.Year

@DataMongoTest
class ScheduleRepositoryTest @Autowired constructor (
    private val scheduleRepository: ScheduleRepository,
){
    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
    }

    @Test
    fun `getRecurringSchedules should return schedules matching repeat type and date criteria`() {
        val dbUserId = "testUser"
        val (userId, year, month) = SearchCondition(dbUserId, 2024, DECEMBER)
        val schedules = listOf(
            ScheduleFixture.createSchedule(dbUserId, "2024-12-01", "2024-12-31", DAILY),
            ScheduleFixture.createSchedule(dbUserId, "2024-11-15", "2024-12-05", WEEKLY),
            ScheduleFixture.createSchedule(dbUserId, "2024-12-10", "2024-12-15", NONE)
        )
        scheduleRepository.saveAll(schedules)

        val recurringSchedules = scheduleRepository.getRecurringSchedules(userId, year, month)

        assertThat(recurringSchedules).hasSize(2)
        assertThat(recurringSchedules.map { it.repeatType }).containsOnly(DAILY, WEEKLY)
    }

    private data class SearchCondition(
        val userId: String,
        val year: Year,
        val month: Month
    ) {
        constructor(userId: String, year: Int, month: Month) : this(userId, Year.of(year), month)
    }
}