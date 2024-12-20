package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.common.exception.ErrorCode.SCHEDULE_NOT_FOUND
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.RepeatType.*
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.fixture.ScheduleFixture.Companion.createSchedule
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.schedule.repository.OneTimeSchedulesRepository
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.util.DatabaseIdHelper.Companion.validateAndGetId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

@SpringBootTest
class ScheduleServiceTest @Autowired constructor(
    private val scheduleService: ScheduleService,
    private val oneTimeSchedulesRepository: OneTimeSchedulesRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
) {
    @BeforeEach
    fun setUp() {
        oneTimeSchedulesRepository.deleteAll()
        scheduleRepository.deleteAll()
        userRepository.deleteAll()
    }

    @ParameterizedTest
    @MethodSource("provideSchedulesAndDateRanges")
    fun `getSchedules should check if schedule is within date range`(
        testName: String,
        scheduleStartTime: LocalDateTime,
        scheduleEndTime: LocalDateTime,
        rangeStart: LocalDateTime,
        rangeEnd: LocalDateTime,
        hasOverlap: Boolean
    ) {
        println("Running test: $testName")

        val user = userRepository.save(createUser())
        val request = CreateScheduleRequest(
            startTime = scheduleStartTime,
            endTime = scheduleEndTime,
            repeatType = NONE,
            participants = setOf(user.userId)
        )

        scheduleService.create(user.userId, request)

        val schedules = scheduleService.getSchedules(
            user.userId,
            rangeStart,
            rangeEnd
        )

        if (hasOverlap) {
            assertEquals(1, schedules.size)
            assertEquals(user.userId, schedules[0].userId)
        } else {
            assertTrue(schedules.isEmpty())
        }
    }


    @Test
    fun `getById should return schedule successfully`() {
        val user = userRepository.save(createUser())
        val schedule = scheduleRepository.save(createSchedule(user))
        val scheduleId = schedule.validateAndGetId()

        val foundSchedule = scheduleService.getById(scheduleId)

        assertThat(foundSchedule.id).isEqualTo(schedule.id)
        assertThat(foundSchedule.userId).isEqualTo(schedule.userId)
        assertThat(foundSchedule.startTime.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(schedule.startTime.truncatedTo(ChronoUnit.SECONDS))
        assertThat(foundSchedule.endTime.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(schedule.endTime.truncatedTo(ChronoUnit.SECONDS))
        assertThat(foundSchedule.repeatType).isEqualTo(schedule.repeatType)
        assertThat(foundSchedule.participants).isEqualTo(schedule.participants)
        assertThat(foundSchedule.status).isEqualTo(schedule.status)
    }

    @Test
    fun `getById should throw exception when schedule not found`() {
        assertThatThrownBy { scheduleService.getById("not-found") }
            .isInstanceOf(GlobalExceptions.NotFoundException::class.java)
            .hasMessage(SCHEDULE_NOT_FOUND.message)
    }

    @Test
    fun `create should create schedule successfully`() {
        val user = userRepository.save(createUser())
        val request = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().plusMonths(3).atStartOfDay(),
            repeatType = NONE,
            participants = setOf(user.userId)
        )

        val createdSchedule = scheduleService.create(user.userId, request)

        assertThat(createdSchedule.userId).isEqualTo(user.userId)
        assertThat(createdSchedule.startTime).isEqualTo(request.startTime)
        assertThat(createdSchedule.endTime).isEqualTo(request.endTime)
        assertThat(createdSchedule.repeatType).isEqualTo(request.repeatType)
        assertThat(createdSchedule.participants).isEqualTo(request.participants)
        assertThat(createdSchedule.status).isEqualTo(ScheduleStatus.PENDING)
    }

    @Test
    fun `upsertOneTimeSchedules should save inverted index in collection`() {
        // given
        val user = userRepository.save(createUser())
        val request = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().plusMonths(3).atStartOfDay(),
            repeatType = NONE,
            participants = setOf(user.userId)
        )

        // when
        val savedSchedule = scheduleService.create(user.userId, request)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()
        assertThat(oneTimeSchedules[0].userId).isEqualTo(user.userId)
        assertThat(oneTimeSchedules).hasSize(4)
        assertThat(oneTimeSchedules[0].schedules.contains(savedSchedule.id)).isTrue()
    }

    @Test
    fun `upsertOneTimeSchedules should save inverted index via bulk write`() {
        // given
        val user = userRepository.save(createUser())
        val request1 = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().plusMonths(3).atStartOfDay(),
            repeatType = NONE,
            participants = setOf(user.userId)
        )
        val request2 = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().plusMonths(5).atStartOfDay(),
            repeatType = NONE,
            participants = setOf(user.userId)
        )

        // when
        val savedSchedule1 = scheduleService.create(user.userId, request1)
        val savedSchedule2 = scheduleService.create(user.userId, request2)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()
        assertThat(oneTimeSchedules[0].userId).isEqualTo(user.userId)
        assertThat(oneTimeSchedules).hasSize(6)
        assertThat(oneTimeSchedules[0].schedules.containsAll(setOf(savedSchedule1.id, savedSchedule2.id))).isTrue()
    }

    @Test
    fun `update should update schedule successfully`() {
        val user = userRepository.save(createUser())
        val schedule = scheduleRepository.save(createSchedule(user))
        val scheduleId = schedule.validateAndGetId()

        val updatedSchedule = scheduleService.update(
            scheduleId, CreateScheduleRequest(
                startTime = schedule.startTime.plusDays(1),
                endTime = schedule.endTime.plusDays(1),
                repeatType = schedule.repeatType,
                participants = schedule.participants,
                status = schedule.status
            )
        )

        assertThat(updatedSchedule.id).isEqualTo(schedule.id)
        assertThat(updatedSchedule.userId).isEqualTo(schedule.userId)
        assertThat(updatedSchedule.startTime).isEqualTo(schedule.startTime.plusDays(1))
        assertThat(updatedSchedule.endTime).isEqualTo(schedule.endTime.plusDays(1))
        assertThat(updatedSchedule.repeatType).isEqualTo(schedule.repeatType)
        assertThat(updatedSchedule.participants).isEqualTo(schedule.participants)
        assertThat(updatedSchedule.status).isEqualTo(schedule.status)
    }

    @Test
    fun `update should handle transition from regular schedule to repeat schedule`() {
        // given
        val user = userRepository.save(createUser("user"))
        val createRequest = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().atStartOfDay().plusHours(3),
            repeatType = NONE, // 기존에는 반복 일정이 아님
            participants = setOf(user.userId),
            status = ScheduleStatus.PENDING
        )
        val schedule = scheduleService.create(user.userId, createRequest)

        val updateRequest = CreateScheduleRequest(
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            repeatType = DAILY, // 반복 일정으로 변경
            participants = schedule.participants,
            status = schedule.status
        )

        // when
        val updatedSchedule = scheduleService.update(schedule.id!!, updateRequest)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()

        // 반복 일정으로 변경된 경우 기존 one-time 스케줄 삭제 확인
        assertThat(oneTimeSchedules).isEmpty()

        // 스케줄 업데이트 확인
        assertThat(updatedSchedule.id).isEqualTo(schedule.id)
        assertThat(updatedSchedule.repeatType).isEqualTo(DAILY)
        assertThat(updatedSchedule.startTime).isEqualTo(updateRequest.startTime)
        assertThat(updatedSchedule.endTime).isEqualTo(updateRequest.endTime)
        assertThat(updatedSchedule.participants).isEqualTo(updateRequest.participants)
        assertThat(updatedSchedule.status).isEqualTo(updateRequest.status)
    }

    @Test
    fun `update should add new participants and their schedules`() {
        // given
        val user1 = userRepository.save(createUser("user1"))
        val user2 = userRepository.save(createUser("user2"))
        val createRequest = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().atStartOfDay().plusHours(3),
            repeatType = NONE,
            participants = setOf(user1.userId)
        )
        val schedule = scheduleService.create(user1.userId, createRequest)

        val request = CreateScheduleRequest(
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            repeatType = schedule.repeatType,
            participants = setOf(user1.userId, user2.userId),
            status = schedule.status
        )

        // when
        val updatedSchedule = scheduleService.update(schedule.id!!, request)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()
        assertThat(oneTimeSchedules).hasSize(2) // 2 participants now
        assertThat(oneTimeSchedules.any { it.userId == user2.userId }).isTrue()
        assertThat(updatedSchedule.participants).containsExactlyInAnyOrder(user1.userId, user2.userId)
    }

    @Test
    fun `update should remove participants and their schedules`() {
        // given
        val user1 = userRepository.save(createUser("user1"))
        val user2 = userRepository.save(createUser("user2"))
        val createRequest = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().atStartOfDay().plusHours(3),
            repeatType = NONE,
            participants = setOf(user1.userId, user2.userId)
        )
        val schedule = scheduleService.create(user1.userId, createRequest)

        val updateRequest = CreateScheduleRequest(
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            repeatType = schedule.repeatType,
            participants = setOf(user1.userId), // Remove user2
            status = schedule.status
        )

        // when
        val updatedSchedule = scheduleService.update(schedule.id!!, updateRequest)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()
        assertThat(oneTimeSchedules).hasSize(1) // Only user1 should remain
        assertThat(oneTimeSchedules.any { it.userId == user2.userId }).isFalse()
        assertThat(updatedSchedule.participants).containsExactly(user1.userId)
    }

    @Test
    fun `update should modify time range for participants`() {
        // given
        val user = userRepository.save(createUser("user"))
        val schedule = scheduleRepository.save(
            createSchedule(user, startTime = LocalDate.now().atStartOfDay(), endTime = LocalDate.now().plusMonths(1).atStartOfDay())
        )
        val scheduleId = schedule.validateAndGetId()

        val request = CreateScheduleRequest(
            startTime = LocalDate.now().plusMonths(1).atStartOfDay(),
            endTime = LocalDate.now().plusMonths(2).atStartOfDay(),
            repeatType = schedule.repeatType,
            participants = schedule.participants,
            status = schedule.status
        )

        // when
        val updatedSchedule = scheduleService.update(scheduleId, request)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()
        assertThat(oneTimeSchedules).hasSize(1) // Only 1 participant, but time range updated
        assertThat(updatedSchedule.startTime).isEqualTo(request.startTime)
        assertThat(updatedSchedule.endTime).isEqualTo(request.endTime)
    }

    @Test
    fun `update should handle no changes gracefully`() {
        // given
        val user = userRepository.save(createUser("user"))
        val createRequest = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().atStartOfDay().plusHours(3),
            repeatType = NONE,
            participants = setOf(user.userId)
        )
        val schedule = scheduleService.create(user.userId, createRequest)

        val updateRequest = CreateScheduleRequest(
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            repeatType = schedule.repeatType,
            participants = schedule.participants, // No changes in participants
            status = schedule.status
        )

        // when
        val updatedSchedule = scheduleService.update(schedule.id!!, updateRequest)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()
        assertThat(oneTimeSchedules).hasSize(1)
        assertThat(updatedSchedule.participants).isEqualTo(schedule.participants)
    }

    @Test
    fun `update should bulk write changes to the database`() {
        // given
        val user1 = userRepository.save(createUser("user1"))
        val user2 = userRepository.save(createUser("user2"))
        val createRequest = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().atStartOfDay().plusHours(3),
            repeatType = NONE,
            participants = setOf(user1.userId)
        )
        val schedule = scheduleService.create(user1.userId, createRequest)
        val scheduleId = schedule.validateAndGetId()

        val updateRequest = CreateScheduleRequest(
            startTime = schedule.startTime.minusMonths(1),
            endTime = schedule.endTime.plusMonths(1),
            repeatType = schedule.repeatType,
            participants = setOf(user1.userId, user2.userId),
            status = schedule.status
        )

        // when
        val updatedSchedule = scheduleService.update(scheduleId, updateRequest)

        // then
        val oneTimeSchedules = oneTimeSchedulesRepository.findAll()
        assertThat(oneTimeSchedules).hasSize(6)
        // Verify bulk write success
    }


    @Test
    fun `delete should delete schedule successfully`() {
        val user = userRepository.save(createUser())
        val createRequest = CreateScheduleRequest(
            startTime = LocalDate.now().atStartOfDay(),
            endTime = LocalDate.now().atStartOfDay().plusHours(3),
            repeatType = NONE,
            participants = setOf(user.userId)
        )
        val schedule = scheduleService.create(user.userId, createRequest)

        scheduleService.delete(schedule.id!!)

        assertThat(scheduleRepository.findById(schedule.id!!)).isEmpty
        assertThat(oneTimeSchedulesRepository.findAll()).isEmpty()
    }

    companion object {
        @JvmStatic
        fun provideSchedulesAndDateRanges(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "Case 1: Same year",
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 1: Same year - Excluded",
                LocalDateTime.of(2023, 4, 1, 0, 0),
                LocalDateTime.of(2023, 4, 30, 23, 59),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                false
            ),
            Arguments.of(
                "Case 2: Different years",
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2022, 12, 31, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                true
            ),
            Arguments.of(
                "Case 2: Different years",
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 1, 31, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                true
            ),
            Arguments.of(
                "Case 2: Excluded",
                LocalDateTime.of(2023, 3, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                false
            ),
            Arguments.of(
                "Case 3: Extended range",
                LocalDateTime.of(2023, 6, 1, 0, 0),
                LocalDateTime.of(2023, 6, 30, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2024, 1, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 4: Specific months",
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 4: Specific months",
                LocalDateTime.of(2023, 3, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 4: Excluded",
                LocalDateTime.of(2023, 4, 1, 0, 0),
                LocalDateTime.of(2023, 4, 30, 23, 59),
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                false
            ),
            Arguments.of(
                "Case 5: Full year range",
                LocalDateTime.of(2023, 6, 1, 0, 0),
                LocalDateTime.of(2023, 6, 30, 23, 59),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 6: Included",
                LocalDateTime.of(2023, 12, 31, 23, 59),
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                true
            ),
        )
    }
}
