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
    override fun getDailyScheduleList(
        userId: String,
        rangeStart: LocalDate,
        rangeEnd: LocalDate
    ): Set<String> {
        return mongoTemplate.find(
            Query().addCriteria(
                Criteria().andOperator(
                    Criteria.where("userId").`is`(userId),
                    Criteria().orOperator(
                        // Case 1: Start year == partitionYear == End year
                        Criteria().andOperator(
                            Criteria.where("partitionYear").`is`(rangeStart.year),
                            Criteria.where("partitionYear").`is`(rangeEnd.year),
                            Criteria.where("partitionMonth").gte(rangeStart.monthValue),
                            Criteria.where("partitionMonth").lte(rangeEnd.monthValue)
                        ),
                        // Case 2: Start year == partitionYear < End year
                        Criteria().andOperator(
                            Criteria.where("partitionYear").`is`(rangeStart.year),
                            Criteria.where("partitionYear").lt(rangeEnd.year),
                            Criteria.where("partitionMonth").gte(rangeStart.monthValue)
                        ),
                        // Case 3: Start year < partitionYear == End year
                        Criteria().andOperator(
                            Criteria.where("partitionYear").`is`(rangeEnd.year),
                            Criteria.where("partitionYear").gt(rangeStart.year),
                            Criteria.where("partitionMonth").lte(rangeEnd.monthValue)
                        ),
                        // Case 4: rangeStart.year < partitionYear < rangeEnd.year
                        Criteria().andOperator(
                            Criteria.where("partitionYear").gt(rangeStart.year),
                            Criteria.where("partitionYear").lt(rangeEnd.year)
                        )
                    )
                )
            ),
            DailySchedules::class.java
        )
            .flatMap { it.schedules }
            .toSet()
    }

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