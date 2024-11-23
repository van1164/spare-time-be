package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import java.time.LocalDateTime

class ScheduleService (
    private val scheduleRepository: ScheduleRepository,
) {
    fun findSchedules(userId: String, start: LocalDateTime, end: LocalDateTime): List<Schedule> {
        return scheduleRepository.findSchedules(userId, start, end)
    }

    fun getById(scheduleId: String): Schedule {
        return scheduleRepository.findById(scheduleId).orElseThrow {
            RuntimeException("Schedule not found with id: $scheduleId")
        }
    }

    fun create(user: User, request: CreateScheduleRequest): Schedule {
        return scheduleRepository.save(
            request.toDomain(user.id)
        )
    }

    fun update(scheduleId: String, request: CreateScheduleRequest): Schedule {
        val schedule = getById(scheduleId)
        return scheduleRepository.save(
            Schedule(
                id = schedule.id,
                userId = schedule.userId,
                startTime = request.startTime,
                endTime = request.endTime,
                repeatType = request.repeatType,
                participants = request.participants,
                status = request.status
            )
        )
    }

    fun delete(scheduleId: String) {
        scheduleRepository.deleteById(scheduleId)
    }
}