package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.DailySchedules
import com.van1164.resttimebe.domain.RepeatType
import com.van1164.resttimebe.domain.RepeatType.*
import com.van1164.resttimebe.domain.Schedule
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year

@Repository
class ScheduleRepositoryImpl(
    val mongoTemplate: MongoTemplate
) : ScheduleRepositoryCustom {
    override fun getRecurringSchedules(
        userId: String,
        year: Year,
        month: Month
    ): Set<Schedule> {
        val yearMonthStart: Date = Date.valueOf(year.atMonth(month).atDay(1))
        val yearMonthEnd: Date = Date.valueOf(year.atMonth(month).atEndOfMonth())

        val query = Query().addCriteria(
            Criteria.where("repeatType").ne(NONE)
                .orOperator(
                    Criteria().andOperator(
                        Criteria.where("startDate").gte(yearMonthStart),
                        Criteria.where("startDate").lte(yearMonthEnd)
                    ),
                    Criteria().andOperator(
                        Criteria.where("startDate").lt(yearMonthStart),
                        Criteria.where("endDate").lte(yearMonthEnd)
                    ),
                    Criteria().andOperator(
                        Criteria.where("startDate").lte(yearMonthEnd),
                        Criteria.where("endDate").gt(yearMonthEnd),
                    )
                )
                .and("participants").`is`(userId)
        )

        return mongoTemplate.find(query, Schedule::class.java).toSet()
    }
}