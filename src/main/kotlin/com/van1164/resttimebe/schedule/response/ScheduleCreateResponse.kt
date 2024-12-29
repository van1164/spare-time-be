package com.van1164.resttimebe.schedule.response

import com.mongodb.client.result.UpdateResult
import com.van1164.resttimebe.domain.MultiDayParticipation
import com.van1164.resttimebe.domain.Schedule

sealed class ScheduleCreateResponse(
    val schedule: Schedule,
    val dailyScheduleUpdateResult: UpdateResult? = null,
    val multiDayParticipation: MultiDayParticipation? = null
) {
    class DailyScheduleResponse(
        schedule: Schedule,
        dailyScheduleUpdateResult: UpdateResult
    ) : ScheduleCreateResponse(
        schedule = schedule,
        dailyScheduleUpdateResult = dailyScheduleUpdateResult
    )

    class MultiDayScheduleResponse(
        schedule: Schedule,
        multiDayParticipation: MultiDayParticipation
    ) : ScheduleCreateResponse(
        schedule = schedule,
        multiDayParticipation = multiDayParticipation
    )

    class RecurringScheduleResponse(
        schedule: Schedule
    ) : ScheduleCreateResponse(schedule)
}
