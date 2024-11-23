package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import java.time.LocalDateTime

interface ScheduleRepositoryCustom {
    fun findSchedules(userId: String, start: LocalDateTime, end: LocalDateTime): List<Schedule>
}