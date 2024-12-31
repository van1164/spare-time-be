package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Document(collection = "schedules")
data class Schedule(
    @Id
    val id: String? = null,
    val userId: String,
    val categoryId: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = startDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val repeatOptions: RepeatOptions? = null,
    val participants: Set<String>,
    val status: ScheduleStatus
) {
    val isDaily: Boolean
        get() = startDate == endDate
}

data class RepeatOptions(
    val interval: RepeatInterval,
    val frequency: Int = 1,
    val daysOfWeek: Set<DayOfWeek>? = null,
    val daysOfMonth: Set<Int>? = null,
    val daysOfYear: Set<Int>? = null,
    val endCondition: RepeatEndCondition
)


enum class RepeatInterval {
    DAILY, WEEKLY, MONTHLY, YEARLY
}


data class RepeatEndCondition(
    val type: EndConditionType,
    val repeatCount: Int? = null,
    val endDate: LocalDate? = null
)


enum class EndConditionType {
    UNTIL_DATE,
    AFTER_COUNT,
    INDEFINITE
}

enum class ScheduleStatus {
    CONFIRMED,
    CANCELLED,
    PENDING
}
