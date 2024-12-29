package com.van1164.resttimebe.schedule.request

import com.van1164.resttimebe.domain.Category
import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.RepeatType.*
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.ScheduleStatus
import com.van1164.resttimebe.domain.ScheduleStatus.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CreateScheduleRequest(
    val categoryId: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = startDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val repeatType: RepeatType = NONE,
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
            repeatType = repeatType,
            participants = participants,
            status = status
        )
    }
}
