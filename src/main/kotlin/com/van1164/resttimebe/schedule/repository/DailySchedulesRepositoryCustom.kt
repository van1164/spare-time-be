package com.van1164.resttimebe.schedule.repository

import java.time.LocalDate

interface DailySchedulesRepositoryCustom {
    fun upsertOne(userId: String, startDate: LocalDate, scheduleId: String)
}