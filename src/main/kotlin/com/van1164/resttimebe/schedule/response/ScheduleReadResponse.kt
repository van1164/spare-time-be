package com.van1164.resttimebe.schedule.response

import com.van1164.resttimebe.domain.Schedule

data class ScheduleReadResponse(
    val dailySchedules: List<Schedule>,
    val multiDaySchedules: List<Schedule>,
    val recurringSchedules: List<Schedule>
)
