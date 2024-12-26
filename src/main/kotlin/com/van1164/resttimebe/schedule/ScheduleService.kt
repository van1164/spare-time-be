package com.van1164.resttimebe.schedule

import com.mongodb.client.model.*
import com.van1164.resttimebe.common.exception.ErrorCode
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.common.exception.GlobalExceptions.*
import com.van1164.resttimebe.domain.MultiDayParticipation
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.schedule.repository.DailySchedulesRepository
import com.van1164.resttimebe.schedule.repository.MultiDayParticipationRepository
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import org.bson.Document
import org.bson.conversions.Bson
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Service
@Transactional
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val dailySchedulesRepository: DailySchedulesRepository,
    private val multiDayParticipationRepository: MultiDayParticipationRepository,
    private val mongoTemplate: MongoTemplate
) {
    fun getSchedules(
        userId: String,
        rangeStart: LocalDateTime,
        rangeEnd: LocalDateTime
    ): List<Schedule> {

    }

    fun getById(scheduleId: String): Schedule {
        return scheduleRepository.findById(scheduleId).orElseThrow {
            throw NotFoundException(ErrorCode.SCHEDULE_NOT_FOUND)
        }
    }

    fun create(userId: String, request: CreateScheduleRequest): Schedule {
        val saved = scheduleRepository.save(request.toDomain(userId))

        if (saved.isDaily) {
            dailySchedulesRepository.upsertOne(userId, saved.startDate, saved.id!!)
        } else {
            multiDayParticipationRepository.save(
                MultiDayParticipation(
                    userId = userId,
                    scheduleId = saved.id!!,
                    startDate = saved.startDate,
                    endDate = saved.endDate
                )
            )
        }

        return saved
    }

    fun update(scheduleId: String, request: CreateScheduleRequest): Schedule {

    }

    fun delete(scheduleId: String) {

    }

    private fun removeDailySchedules(userId: String, startDate: LocalDate, schedule: Schedule): List<WriteModel<Document>> {
        val filter: Bson = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("partitionYear", startDate.year),
            Filters.eq("partitionMonth", startDate.monthValue)
        )
        val pullUpdate: Bson = Updates.combine(Updates.pull("schedules", schedule.id))
        val deleteCondition: Bson = Filters.and(filter, Filters.size("schedules", 0))
        return listOf(
            UpdateOneModel(filter, pullUpdate), // 업데이트 작업
            DeleteOneModel(deleteCondition) // 삭제 작업
        )
    }
}