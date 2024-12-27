package com.van1164.resttimebe.schedule.response

import com.mongodb.client.result.UpdateResult
import com.van1164.resttimebe.domain.MultiDayParticipation
import com.van1164.resttimebe.domain.Schedule

sealed class ScheduleCreateResponse {
    data class DailyScheduleResponse(
        val dailyScheduleUpdateResult: UpdateResult,
        val schedule: Schedule
    ) : ScheduleCreateResponse() {
        val multiDayParticipation: MultiDayParticipation? = null
    }

    data class MultiDayScheduleResponse(
        val multiDayParticipation: MultiDayParticipation,
        val schedule: Schedule
    ) : ScheduleCreateResponse() {
        val dailyScheduleResult: UpdateResult? = null
    }

    data class RecurringScheduleResponse(
        val schedule: Schedule
    ) : ScheduleCreateResponse() {
        val dailyScheduleResult: UpdateResult? = null
        val multiDayParticipation: MultiDayParticipation? = null
    }
}
