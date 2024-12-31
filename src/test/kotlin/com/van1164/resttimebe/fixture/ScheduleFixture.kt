package com.van1164.resttimebe.fixture

import com.van1164.resttimebe.domain.*
import com.van1164.resttimebe.domain.EndConditionType.*
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import java.time.LocalDate

class ScheduleFixture {
    companion object {
        fun createSchedule(userId: String, startDate: String, endDate: String? = startDate): Schedule {
            return Schedule (
                userId = userId,
                startDate = LocalDate.parse(startDate),
                endDate = endDate?.let {LocalDate.parse(endDate) },
                repeatOptions = null,
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }

        fun createSchedule(userId: String, startDate: String, endDate: String? = startDate, repeatInterval: RepeatInterval, endConditionType: EndConditionType = INDEFINITE): Schedule {
            return Schedule (
                userId = userId,
                startDate = LocalDate.parse(startDate),
                endDate = endDate?.let {LocalDate.parse(endDate) },
                repeatOptions = createRepeatOptions(repeatInterval, endConditionType),
                participants = setOf(userId),
                status = ScheduleStatus.CONFIRMED,
            )
        }

        fun createScheduleRequest(
            userId: String,
            startDate: String,
            endDate: String? = startDate,
        ): CreateScheduleRequest {
            return CreateScheduleRequest(
                startDate = LocalDate.parse(startDate),
                endDate = endDate?.let { LocalDate.parse(endDate) },
                repeatOptions = null,
                participants = setOf(userId)
            )
        }

        fun createScheduleRequest(
            userId: String,
            startDate: String,
            endDate: String? = startDate,
            repeatInterval: RepeatInterval,
            endConditionType: EndConditionType = INDEFINITE
        ): CreateScheduleRequest {
            return CreateScheduleRequest(
                startDate = LocalDate.parse(startDate),
                endDate = endDate?.let { LocalDate.parse(endDate) },
                repeatOptions = createRepeatOptions(repeatInterval, endConditionType),
                participants = setOf(userId)
            )
        }

        private fun createRepeatOptions(interval: RepeatInterval, endConditionType: EndConditionType?): RepeatOptions {
            return RepeatOptions(
                interval = interval,
                endCondition = createEndCondition(endConditionType!!)
            )
        }

        private fun createEndCondition(type: EndConditionType): RepeatEndCondition {
            return when (type) {
                UNTIL_DATE -> RepeatEndCondition(
                    type = type,
                    endDate = LocalDate.now().plusMonths(1)
                )
                AFTER_COUNT -> RepeatEndCondition(
                    type = type,
                    repeatCount = 10
                )
                INDEFINITE -> RepeatEndCondition(type)
            }
        }
    }
}