package com.van1164.resttimebe.schedule
import com.van1164.resttimebe.schedule.repository.DailySchedulesRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.LocalDate
import java.time.Month
import java.time.Year
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataMongoTest
class DailySchedulesRepositoryImplTest @Autowired constructor(
    private val mongoTemplate: MongoTemplate,
    private val dailySchedulesRepository: DailySchedulesRepository
) {


    @BeforeEach
    fun setUp() {
        dailySchedulesRepository.deleteAll()
    }

    @Test
    fun `upsertOne insert a new record if it doesn't exist`() {
        val userId = "user1"
        val startDate = LocalDate.of(2024, Month.DECEMBER, 28)
        val scheduleId = "schedule1"

        dailySchedulesRepository.upsertOne(userId, startDate, scheduleId)

        val result = dailySchedulesRepository.getDailyScheduleIds(userId, Year.of(2024), Month.DECEMBER)
        assertEquals(setOf(scheduleId), result)
    }

    @Test
    fun `upsertOne update an existing record by adding scheduleId`() {
        val userId = "user1"
        val startDate = LocalDate.of(2024, Month.DECEMBER, 28)
        val existingScheduleId = "schedule1"
        val newScheduleId = "schedule2"
        dailySchedulesRepository.upsertOne(userId, startDate, existingScheduleId)

        dailySchedulesRepository.upsertOne(userId, startDate, newScheduleId)

        val result = dailySchedulesRepository.getDailyScheduleIds(userId, Year.of(2024), Month.DECEMBER)
        assertEquals(setOf(existingScheduleId, newScheduleId), result)
    }

    @Test
    fun `getDailyScheduleIds return empty set if no record exists`() {
        val userId = "user1"
        val year = Year.of(2024)
        val month = Month.DECEMBER

        val result = dailySchedulesRepository.getDailyScheduleIds(userId, year, month)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty test`() {
        assertTrue(true)
    }
}
