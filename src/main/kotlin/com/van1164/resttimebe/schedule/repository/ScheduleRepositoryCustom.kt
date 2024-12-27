package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import java.time.LocalDate

interface ScheduleRepositoryCustom {
    fun getRecurringSchedules(userId: String, rangeStart: LocalDate, rangeEnd: LocalDate): Set<Schedule>
}