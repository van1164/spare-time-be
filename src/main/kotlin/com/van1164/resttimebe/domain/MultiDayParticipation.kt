package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "multi_day_participation")
data class MultiDayParticipation (
    @Id
    val id: String? = null,
    val userId: String,
    val scheduleId: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)