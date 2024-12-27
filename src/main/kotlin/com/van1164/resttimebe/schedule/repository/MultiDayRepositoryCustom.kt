package com.van1164.resttimebe.schedule.repository

import java.time.Month
import java.time.Year

interface MultiDayRepositoryCustom {
    fun getMultiDayScheduleIds(userId: String, year: Year, month: Month): Set<String>
}