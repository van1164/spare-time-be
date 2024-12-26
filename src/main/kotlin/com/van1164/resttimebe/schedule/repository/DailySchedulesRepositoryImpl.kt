package com.van1164.resttimebe.schedule.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.LocalDate

class DailySchedulesRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : DailySchedulesRepositoryCustom {
    override fun upsertOne(userId: String, startDate: LocalDate, scheduleId: String) {
        val filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("partitionYear", startDate.year),
            Filters.eq("partitionMonth", startDate.monthValue)
        )
        val update = Updates.combine(
            Updates.setOnInsert("userId", userId),
            Updates.setOnInsert("partitionYear", startDate.year),
            Updates.setOnInsert("partitionMonth", startDate.monthValue),
            Updates.addToSet("schedules", scheduleId)
        )
        val options = UpdateOptions().upsert(true)

        updateOne(filter, update, options)
    }

    private fun updateOne(filter: Bson, update: Bson, options: UpdateOptions) {
        mongoTemplate.getCollection("daily_schedules").updateOne(filter, update, options)
    }
}