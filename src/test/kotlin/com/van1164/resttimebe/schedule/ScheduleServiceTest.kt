package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.fixture.ScheduleFixture.Companion.createSchedule
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import com.van1164.resttimebe.user.UserRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class ScheduleServiceTest @Autowired constructor(
    private val scheduleService: ScheduleService,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
) {
    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `findSchedules should return schedules within date ranges`() {
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
        scheduleRepository.saveAll(listOf(schedule1, schedule2))

        val schedules = scheduleService.findSchedules(
            user.id,
            LocalDate.now().minusDays(2).atStartOfDay(),
            LocalDate.now().plusDays(1).atStartOfDay()
        )

        assertThat(schedules).hasSize(1)
        assertThat(schedules[0].userId).isEqualTo(schedule2.userId)
        assertThat(schedules[0].startTime).isEqualTo(schedule2.startTime)
        assertThat(schedules[0].endTime).isEqualTo(schedule2.endTime)
    }

    @Test
    fun `update should update schedule successfully`() {
        val user = userRepository.save(createUser())
        val schedule = scheduleRepository.save(createSchedule(user))

        val updatedSchedule = scheduleService.update(
            schedule.id!!, CreateScheduleRequest(
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
        val schedule = scheduleRepository.save(createSchedule(user))

        scheduleService.delete(schedule.id!!)

        assertThat(scheduleRepository.findById(schedule.id!!)).isEmpty
    }
}