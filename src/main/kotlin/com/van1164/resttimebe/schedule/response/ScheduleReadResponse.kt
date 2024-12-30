package com.van1164.resttimebe.schedule.response

import com.van1164.resttimebe.domain.Schedule

data class ScheduleReadResponse(
    val dailySchedules: Set<Schedule>,
    val multiDaySchedules: Set<Schedule>,
    val recurringSchedules: Set<Schedule>
)
