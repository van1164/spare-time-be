package com.van1164.resttimebe.schedule

import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.WriteModel
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.YearMonth

@Service
@Transactional
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val mongoTemplate: MongoTemplate
) {
    fun getSchedules(
        userId: String,
        rangeStart: LocalDateTime,
        rangeEnd: LocalDateTime
    ): List<Schedule> {

    }

    fun getById(scheduleId: String): Schedule {

    }

    fun create(userId: String, request: CreateScheduleRequest): Schedule {

    }

    fun update(scheduleId: String, request: CreateScheduleRequest): Schedule {

    }

    fun delete(scheduleId: String) {

    }

    private fun upsertOneTimeSchedules(userId: String, time: YearMonth, schedule: Schedule): UpdateOneModel<Document> {

    }

    private fun removeOneTimeSchedules(userId: String, time: YearMonth, schedule: Schedule): List<WriteModel<Document>> {

    }

}