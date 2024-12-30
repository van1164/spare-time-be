package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.MultiDayParticipation
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.sql.Date
import java.time.Month
import java.time.Year

class MultiDayRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : MultiDayRepositoryCustom {
    override fun getMultiDayScheduleIds(userId: String, year: Year, month: Month): Set<String> {
        val yearMonthStart: Date = Date.valueOf(year.atMonth(month).atDay(1))
        val yearMonthEnd: Date = Date.valueOf(year.atMonth(month).atEndOfMonth())

        val query = Query().addCriteria(
            Criteria().orOperator(
                Criteria().andOperator(
                    Criteria.where("userId").`is`(userId),
                    Criteria.where("startDate").gte(yearMonthStart),
                    Criteria.where("startDate").lte(yearMonthEnd)
                ),
                Criteria().andOperator(
                    Criteria.where("userId").`is`(userId),
                    Criteria.where("startDate").lt(yearMonthStart),
                    Criteria.where("endDate").lte(yearMonthEnd)
                ),
                Criteria().andOperator(
                    Criteria.where("userId").`is`(userId),
                    Criteria.where("startDate").lte(yearMonthEnd),
                    Criteria.where("endDate").gt(yearMonthEnd),
                )
            )
        )

        return mongoTemplate.find(query, MultiDayParticipation::class.java)
            .map { it.scheduleId }
            .toSet()
    }
}