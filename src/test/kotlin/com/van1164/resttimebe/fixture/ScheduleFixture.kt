package com.van1164.resttimebe.fixture

import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.domain.User
import java.time.LocalDateTime
import java.util.*

class ScheduleFixture {
    companion object {
        fun createSchedule(user: User): Schedule {
            return Schedule (
                userId = user.id,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1),
                repeatType = RepeatType.NONE,
                participants = List(1) { user.id },
                status = ScheduleStatus.CONFIRMED,
            )
        }

        fun createSchedule(user: User, startTime: LocalDateTime, endTime: LocalDateTime): Schedule {
            return Schedule (
                userId = user.id,
                startTime = startTime,
                endTime = endTime,
                repeatType = RepeatType.NONE,
                participants = List(1) { user.id },
                status = ScheduleStatus.CONFIRMED,
            )
        }
    }
}