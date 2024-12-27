package com.van1164.resttimebe.schedule.response

import com.mongodb.client.result.UpdateResult
import com.van1164.resttimebe.domain.MultiDayParticipation
import com.van1164.resttimebe.domain.Schedule

sealed class ScheduleCreateResponse {
    data class DailyScheduleResult(
        val dailyScheduleUpdateResult: UpdateResult,
        val schedule: Schedule
    ) : ScheduleCreateResponse() {
        val multiDayParticipation: MultiDayParticipation? = null
    }

    data class MultiDayScheduleResult(
        val multiDayParticipation: MultiDayParticipation,
        val schedule: Schedule
    ) : ScheduleCreateResponse() {
        val dailyScheduleResult: UpdateResult? = null
    }

    data class RecurringScheduleResult(
        val schedule: Schedule
    ) : ScheduleCreateResponse() {
        val dailyScheduleResult: UpdateResult? = null
        val multiDayParticipation: MultiDayParticipation? = null
    }
}
