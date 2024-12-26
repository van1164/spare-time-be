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
        fun createSchedule(user: User): Schedule {
            val userId = user.userId
            return Schedule (
                userId = userId,
                startDate = LocalDate.now(),
                startTime = LocalDate.now().atStartOfDay().toLocalTime(),
                endTime = LocalDate.now().atStartOfDay().plusHours(1).toLocalTime(),
                repeatType = RepeatType.NONE,
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }

        fun createSchedule(user: User, startTime: LocalTime, endTime: LocalTime): Schedule {
            val userId = user.userId
            return Schedule (
                userId = userId,
                startDate = LocalDate.now(),
                startTime = startTime,
                endTime = endTime,
                repeatType = RepeatType.NONE,
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }
    }
}