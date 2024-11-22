package com.van1164.resttimebe.schedule.request

import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import java.time.LocalDateTime

data class CreateScheduleRequest(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val repeatType: RepeatType,
    val participants: List<String>,
    val status: ScheduleStatus = ScheduleStatus.PENDING
) {
    fun toDomain(userId: String): Schedule {
        return Schedule(
            userId = userId,
            startTime = startTime,
            endTime = endTime,
            repeatType = repeatType,
            participants = participants,
            status = status
        )
    }
}
