package com.van1164.resttimebe.schedule.request

import com.van1164.resttimebe.domain.RepeatOptions
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.domain.ScheduleStatus.PENDING
import java.time.LocalDate
import java.time.LocalTime

data class CreateScheduleRequest(
    val categoryId: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = startDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val repeatOptions: RepeatOptions? = null,
    val participants: Set<String>,
    val status: ScheduleStatus = PENDING
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
