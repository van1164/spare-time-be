package com.van1164.resttimebe.fixture

import com.van1164.resttimebe.common.exception.ErrorCode.USER_ID_NOT_INITIALIZED
import com.van1164.resttimebe.common.exception.GlobalExceptions.IllegalStateException
import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.domain.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleFixture {
    companion object {
        fun createSchedule(userId: String, startDate: String, endDate: String, repeatType: RepeatType): Schedule {
            return Schedule (
                userId = userId,
                startDate = LocalDate.parse(startDate),
                endDate = LocalDate.parse(endDate),
                repeatType = repeatType,
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }
    }
}