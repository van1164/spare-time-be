package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import java.time.Month
import java.time.Year

interface ScheduleRepositoryCustom {
    fun getRecurringSchedules(userId: String, year: Year, month: Month): Set<Schedule>
}