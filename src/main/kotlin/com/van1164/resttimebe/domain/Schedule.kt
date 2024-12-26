package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
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
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val repeatType: RepeatType,
    val participants: Set<String>,
    val status: ScheduleStatus
) {
    val isDaily: Boolean = startDate == endDate
}

enum class RepeatType {
    DAILY,    // 매일
    WEEKLY,   // 매주
    MONTHLY,  // 매월
    NONE      // 반복 없음
}

enum class ScheduleStatus {
    CONFIRMED,  // 약속 확정
    CANCELLED,  // 취소
    PENDING     // 요청중
}
