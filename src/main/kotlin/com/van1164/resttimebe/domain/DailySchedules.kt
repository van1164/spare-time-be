package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "daily_schedules")
data class DailySchedules (
    @Id
    val id: String? = null,
    val userId: String,
    val schedules: Set<String>,
    val partitionYear: Int,
    val partitionMonth: Int
)

