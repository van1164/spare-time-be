package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.domain.MultiDayParticipation
import com.van1164.resttimebe.schedule.repository.MultiDayRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.Month
import java.time.Month.JANUARY
import java.time.Month.MARCH
import java.time.Year
import kotlin.test.assertEquals

@SpringBootTest
class MultiDayRepositoryImplTest @Autowired constructor(
    private val multiDayRepository: MultiDayRepository
){

    @BeforeEach
    fun setUp() {
        multiDayRepository.deleteAll()
    }

    @Test
    fun `getMultiDayScheduleIds retrieves schedule IDs for the specified user within the same month`() {
        val dbUserId = "testUser"
        val (userId, year, month) = SearchCondition(dbUserId, 2024, JANUARY)
        val participation = listOf(
            createMultiDayParticipation("schedule1", dbUserId, "2024-01-28", "2024-01-31"),
            createMultiDayParticipation("schedule2", dbUserId, "2024-02-10", "2024-02-15"),
            createMultiDayParticipation("schedule3", dbUserId, "2024-01-01", "2024-01-02")
        )
        multiDayRepository.saveAll(participation)

        val result = multiDayRepository.getMultiDayScheduleIds(userId, year, month)

        assertEquals(setOf("schedule1", "schedule3"), result)
    }

    @Test
    fun `getMultiDayScheduleIds returns an empty set when no schedules are found for the specified user`() {
        val (userId, year, month) = SearchCondition("testUser", 2024, MARCH)

        val result = multiDayRepository.getMultiDayScheduleIds(userId, year, month)

        assertEquals(emptySet<String>(), result)
    }

    private fun createMultiDayParticipation(
        scheduleId: String,
        userId: String,
        startDate: String,
        endDate: String
    ): MultiDayParticipation {
        return MultiDayParticipation(
            scheduleId = scheduleId,
            userId = userId,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate)
        )
    }

    private data class SearchCondition(
        val userId: String,
        val year: Year,
        val month: Month
    ) {
        constructor(userId: String, year: Int, month: Month) : this(userId, Year.of(year), month)
    }
}
