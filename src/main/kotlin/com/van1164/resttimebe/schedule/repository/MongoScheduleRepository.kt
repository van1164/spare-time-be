package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

class MongoScheduleRepository (
    val mongoTemplate: MongoTemplate
) : ScheduleRepositoryCustom {
    override fun findSchedules(userId: String, start: LocalDateTime, end: LocalDateTime): List<Schedule> {
        return mongoTemplate.find(
            Query().addCriteria(
                Criteria.where("userId").`is`(userId)
                    .andOperator(
                        Criteria().orOperator(
                            Criteria.where("startTime").gte(start).lte(end),
                            Criteria.where("endTime").gte(start).lte(end)
                        )
                    )
            ),
            Schedule::class.java
        )
    }
}