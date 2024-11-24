package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ScheduleRepositoryImpl (
    val mongoTemplate: MongoTemplate
) : ScheduleRepositoryCustom {
    override fun findSchedules(userId: String, rangeStart: LocalDateTime, rangeEnd: LocalDateTime): List<Schedule> {
        return mongoTemplate.find(
            Query().addCriteria(
                Criteria.where("userId").`is`(userId)
                    .orOperator(
                        Criteria().orOperator(
                            Criteria.where("startTime").gte(rangeStart).lte(rangeEnd),
                            Criteria.where("endTime").gte(rangeStart).lte(rangeEnd)
                        )
                    )
            ),
            Schedule::class.java
        )
    }
}