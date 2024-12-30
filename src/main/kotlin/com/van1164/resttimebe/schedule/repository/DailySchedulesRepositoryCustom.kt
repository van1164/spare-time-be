package com.van1164.resttimebe.schedule.repository

import com.mongodb.client.result.UpdateResult
import java.time.LocalDate
import java.time.Month
import java.time.Year

interface DailySchedulesRepositoryCustom {
    fun upsertOne(userId: String, startDate: LocalDate, scheduleId: String): UpdateResult
    fun getDailyScheduleIds(userId: String, year: Year, month: Month): Set<String>
}