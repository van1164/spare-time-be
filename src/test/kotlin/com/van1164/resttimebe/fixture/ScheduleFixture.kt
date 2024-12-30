package com.van1164.resttimebe.fixture

import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.RepeatType.NONE
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import java.time.LocalDate

class ScheduleFixture {
    companion object {
        fun createSchedule(userId: String, startDate: String, endDate: String? = startDate, repeatType: RepeatType = NONE): Schedule {
            return Schedule (
                userId = userId,
                startDate = LocalDate.parse(startDate),
                endDate = endDate?.let {LocalDate.parse(endDate) },
                repeatType = repeatType,
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }
    }
}