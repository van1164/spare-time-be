package com.van1164.resttimebe.schedule.request

import com.van1164.resttimebe.domain.Category
import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.RepeatType.*
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.domain.ScheduleStatus.*
import java.time.LocalDateTime

data class CreateScheduleRequest(
    val categoryId: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val repeatType: RepeatType = NONE,
    val participants: Set<String>,
    val status: ScheduleStatus = PENDING
) {
    fun toDomain(userId: String): Schedule {
        return Schedule(
            userId = userId,
            categoryId = categoryId,
            startTime = startTime,
            endTime = endTime,
            repeatType = repeatType,
            participants = participants,
            status = status
        )
    }
}
