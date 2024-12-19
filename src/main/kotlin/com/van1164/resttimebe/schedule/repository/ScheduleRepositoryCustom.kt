package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import java.time.LocalDateTime

interface ScheduleRepositoryCustom {
    fun getOneTimeScheduleList(userId: String, rangeStart: LocalDateTime, rangeEnd: LocalDateTime): Set<String>
    fun getRecurringSchedules(userId: String, rangeStart: LocalDateTime, rangeEnd: LocalDateTime): Set<Schedule>
}