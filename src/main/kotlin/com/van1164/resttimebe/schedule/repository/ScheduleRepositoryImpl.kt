package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.DailySchedules
import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.Schedule
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class ScheduleRepositoryImpl(
    val mongoTemplate: MongoTemplate
) : ScheduleRepositoryCustom {
    override fun getRecurringSchedules(
        userId: String,
        rangeStart: LocalDate,
        rangeEnd: LocalDate
    ): Set<Schedule> {
        return mongoTemplate.find(
            Query().addCriteria(
                Criteria().andOperator(
                    Criteria.where("repeatType")
                        .`in`(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY)
                        .andOperator(
                            Criteria.where("participants").`is`(userId),
                        )
                )
            ),
            Schedule::class.java
        )
            .toSet()
    }
}