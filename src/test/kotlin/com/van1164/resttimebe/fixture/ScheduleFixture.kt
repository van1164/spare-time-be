package com.van1164.resttimebe.fixture

import com.van1164.resttimebe.common.exception.ErrorCode.USER_ID_NOT_INITIALIZED
import com.van1164.resttimebe.common.exception.GlobalExceptions.IllegalStateException
import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.domain.User
import java.time.LocalDate
import java.time.LocalDateTime

class ScheduleFixture {
    companion object {
        fun createSchedule(user: User): Schedule {
            val userId = user.userId
            return Schedule (
                userId = userId,
                startTime = LocalDate.now().atStartOfDay(),
                endTime = LocalDate.now().atStartOfDay().plusHours(1),
                repeatType = RepeatType.NONE,
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }

        fun createSchedule(user: User, participants: Set<String> = setOf(user.userId)): Schedule {
            return Schedule(
                userId = user.userId,
                startTime = LocalDate.now().atStartOfDay(),
                endTime = LocalDate.now().plusMonths(3).atStartOfDay(),
                repeatType = RepeatType.NONE,
                participants = participants,
                status = ScheduleStatus.PENDING
            )
        }

        fun createSchedule(user: User, startTime: LocalDateTime, endTime: LocalDateTime): Schedule {
            val userId = user.userId
            return Schedule (
                userId = userId,
                startTime = startTime,
                endTime = endTime,
                repeatType = RepeatType.NONE,
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }
    }
}