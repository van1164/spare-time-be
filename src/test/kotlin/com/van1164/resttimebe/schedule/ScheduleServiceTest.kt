package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.common.exception.ErrorCode.SCHEDULE_NOT_FOUND
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.domain.RepeatType.NONE
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

    @Test
    fun `getSchedules should return schedules within date ranges`() {
        val user = userRepository.save(createUser())
        val schedule1 = createSchedule(
            user,
            LocalDate.now().minusDays(5).atStartOfDay(),
            LocalDate.now().minusDays(4).atStartOfDay()
        )
        val schedule2 = createSchedule(
            user,
            LocalDate.now().minusDays(1).atStartOfDay(),
            LocalDate.now().atStartOfDay()
        )
        scheduleRepository.saveAll(setOf(schedule1, schedule2))

        val schedules = scheduleService.getSchedules(
            user.userId,
            LocalDate.now().minusDays(2).atStartOfDay(),
            LocalDate.now().plusDays(1).atStartOfDay()
        )

        assertThat(schedules).hasSize(1)
        assertThat(schedules[0].userId).isEqualTo(schedule2.userId)
        assertThat(schedules[0].startTime).isEqualTo(schedule2.startTime)
        assertThat(schedules[0].endTime).isEqualTo(schedule2.endTime)
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
    fun `insertOneTimeSchedules should save inverted index in collection`() {
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
    fun `insertOneTimeSchedules should save inverted index via bulk write`() {
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
    fun `delete should delete schedule successfully`() {
        val user = userRepository.save(createUser())
        val scheduleId = scheduleRepository.save(createSchedule(user)).validateAndGetId()

        scheduleService.delete(scheduleId)

        assertThat(scheduleRepository.findById(scheduleId)).isEmpty
    }
}