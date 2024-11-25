package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import java.time.LocalDateTime

interface ScheduleRepositoryCustom {
    fun findSchedules(userId: String, rangeStart: LocalDateTime, rangeEnd: LocalDateTime): List<Schedule>
}