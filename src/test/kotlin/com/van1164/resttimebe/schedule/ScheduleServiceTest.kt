package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.common.exception.ErrorCode.SCHEDULE_NOT_FOUND
import com.van1164.resttimebe.common.exception.GlobalExceptions.NotFoundException
import com.van1164.resttimebe.domain.RepeatInterval.DAILY
import com.van1164.resttimebe.fixture.ScheduleFixture.Companion.createSchedule
import com.van1164.resttimebe.fixture.ScheduleFixture.Companion.createScheduleRequest
import com.van1164.resttimebe.schedule.repository.DailySchedulesRepository
import com.van1164.resttimebe.schedule.repository.MultiDayRepository
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Month
import java.time.Month.JANUARY
import java.time.Year

@SpringBootTest
class ScheduleServiceTest @Autowired constructor(
    private val scheduleService: ScheduleService,
    private val scheduleRepository: ScheduleRepository,
    private val dailySchedulesRepository: DailySchedulesRepository,
    private val multiDayRepository: MultiDayRepository
) {

    @BeforeEach
    fun setUp() {
        dailySchedulesRepository.deleteAll()
        multiDayRepository.deleteAll()
        scheduleRepository.deleteAll()
    }

    @Test
    fun `getSchedules retrieves daily, multi-day, and recurring schedules for the user`() {
        val dbUserId = "testUser"
        val (userId, year, month) = SearchCondition(dbUserId, 2024, JANUARY)
        val requests = listOf(
            createScheduleRequest(dbUserId, "2024-01-20", "2024-01-20"),
            createScheduleRequest(dbUserId, "2024-01-10", "2024-01-13"),
            createScheduleRequest(dbUserId, "2024-01-05", "2024-01-05", DAILY),
        )
        val (dailySchedule, multiDaySchedule, recurringSchedule) = requests.map {
            scheduleService.create(dbUserId, it).schedule
        }

        val response = scheduleService.getSchedules(userId, year, month)

        assertEquals(setOf(dailySchedule), response.dailySchedules)
        assertEquals(setOf(multiDaySchedule), response.multiDaySchedules)
        assertEquals(setOf(recurringSchedule), response.recurringSchedules)
    }

    @Test
    fun `getById retrieves the correct schedule`() {
        val saved = scheduleRepository.save(createSchedule("testUser", "2024-01-15"))

        val result = scheduleService.getById(saved.id!!)

        assertEquals(saved, result)
    }

    @Test
    fun `create adds a daily schedule and updates dailySchedulesRepository`() {
        val userId = "testUser"
        val request = createScheduleRequest(userId, "2024-01-15")

        val response = scheduleService.create(userId, request)

        val savedSchedule = scheduleService.getById(response.schedule.id!!)
        assertNotNull(savedSchedule)
        assertTrue(
            dailySchedulesRepository.getDailyScheduleIds(userId, Year.of(2024), JANUARY)
                .contains(savedSchedule.id)
        )
    }

    @Test
    fun `getSchedules returns empty response when no schedules exist`() {
        val dbUserId = "testUser"
        val (userId, year, month) = SearchCondition(dbUserId, 2024, JANUARY)

        val response = scheduleService.getSchedules(userId, year, month)

        assertTrue(response.dailySchedules.isEmpty())
        assertTrue(response.multiDaySchedules.isEmpty())
        assertTrue(response.recurringSchedules.isEmpty())
    }

    @Test
    fun `getSchedules includes multi-day schedules that start in the given month`() {
        val dbUserId = "testUser"
        val (userId, year, month) = SearchCondition(dbUserId, 2024, JANUARY)
        scheduleService.create(userId, createScheduleRequest(userId, "2024-01-31", "2024-02-01"))

        val response = scheduleService.getSchedules(userId, year, month)

        assertEquals(1, response.multiDaySchedules.size) // multi-day 일정 포함 여부 확인
    }

    @Test
    fun `getById throws NotFoundException for non-existent schedule`() {
        val nonExistentId = "nonExistentId"

        val exception = assertThrows<NotFoundException> {
            scheduleService.getById(nonExistentId)
        }

        assertEquals(SCHEDULE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `create adds a multi-day schedule and updates multiDayRepository`() {
        val userId = "testUser"
        val request = createScheduleRequest(userId, "2024-01-10", "2024-01-13")

        val response = scheduleService.create(userId, request)

        val savedSchedule = scheduleService.getById(response.schedule.id!!)
        assertNotNull(savedSchedule)
        assertTrue(
            multiDayRepository.getMultiDayScheduleIds(userId, Year.of(2024), JANUARY)
                .contains(savedSchedule.id)
        )
    }

    @Test
    fun `delete removes the schedule and associated participants`() {
        val userId = "testUser"
        val schedule =
            scheduleService.create(userId, createScheduleRequest(userId, "2024-01-15")).schedule

        scheduleService.delete(schedule.id!!)

        assertFalse(scheduleRepository.existsById(schedule.id!!))
        assertTrue(
            dailySchedulesRepository.getDailyScheduleIds(userId, Year.of(2024), JANUARY).isEmpty()
        )
    }

    @Test
    fun `removeParticipantsFromSchedule correctly removes participants from daily schedule`() {
        val userId = "testUser"
        val schedule = scheduleService.create(userId, createScheduleRequest(userId, "2024-01-15")).schedule

        scheduleService.delete(schedule.id!!)

        assertTrue(
            dailySchedulesRepository.getDailyScheduleIds(userId, Year.of(2024), JANUARY).isEmpty()
        )
    }

    @Test
    fun `removeParticipantsFromSchedule correctly removes participants from multi-day schedule`() {
        val userId = "testUser"
        val schedule = scheduleService.create(userId, createScheduleRequest(userId, "2024-01-10", "2024-01-13")).schedule

        scheduleService.delete(schedule.id!!)

        assertTrue(
            multiDayRepository.getMultiDayScheduleIds(userId, Year.of(2024), JANUARY).isEmpty()
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
