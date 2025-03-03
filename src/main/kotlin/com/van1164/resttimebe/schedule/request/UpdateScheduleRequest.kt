package com.van1164.resttimebe.schedule.request

import com.van1164.resttimebe.domain.RepeatOptions
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import java.time.LocalDate
import java.time.LocalTime

data class UpdateScheduleRequest(
    val categoryId: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val repeatOptions: RepeatOptions?,
    val participants: Set<String>,
    val status: ScheduleStatus
) {
    fun toDomain(userId: String): Schedule {
        return Schedule(
            userId = userId,
            categoryId = categoryId,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            repeatOptions = repeatOptions,
            participants = participants,
            status = status
        )
    }
}
