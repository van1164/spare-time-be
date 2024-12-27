package com.van1164.resttimebe.schedule

import com.mongodb.client.model.*
import com.van1164.resttimebe.common.exception.ErrorCode
import com.van1164.resttimebe.common.exception.GlobalExceptions.*
import com.van1164.resttimebe.domain.MultiDayParticipation
import com.van1164.resttimebe.domain.RepeatType.*
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.schedule.repository.DailySchedulesRepository
import com.van1164.resttimebe.schedule.repository.MultiDayRepository
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import com.van1164.resttimebe.schedule.response.ScheduleCreateResponse
import com.van1164.resttimebe.schedule.response.ScheduleCreateResponse.*
import com.van1164.resttimebe.schedule.response.ScheduleReadResponse
import org.bson.Document
import org.bson.conversions.Bson
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Month
import java.time.Year

@Service
@Transactional
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val dailySchedulesRepository: DailySchedulesRepository,
    private val multiDayRepository: MultiDayRepository,
    private val mongoTemplate: MongoTemplate
) {
    fun getSchedules(
        userId: String,
        year: Year,
        month: Month
    ): ScheduleReadResponse {
        val dailyScheduleIds = dailySchedulesRepository.getDailyScheduleIds(userId, year, month)
        val multiDayScheduleIds = multiDayRepository.getMultiDayScheduleIds(userId, year, month)
        val scheduleIds = dailyScheduleIds + multiDayScheduleIds

        val schedules = scheduleRepository.findAllById(scheduleIds)
        val recurringSchedules = scheduleRepository.getRecurringSchedules(userId, year, month)

        return ScheduleReadResponse(
            dailySchedules = schedules.filter { it.isDaily }.toSet(),
            multiDaySchedules = schedules.filter { !it.isDaily }.toSet(),
            recurringSchedules = recurringSchedules
        )
    }

    fun getById(scheduleId: String): Schedule {
        return scheduleRepository.findById(scheduleId).orElseThrow {
            throw NotFoundException(ErrorCode.SCHEDULE_NOT_FOUND)
        }
    }

    fun create(userId: String, request: CreateScheduleRequest): ScheduleCreateResponse {
        val saved = scheduleRepository.save(request.toDomain(userId))

        return when {
            saved.isDailySchedule() ->
                DailyScheduleResult(
                    dailyScheduleUpdateResult = dailySchedulesRepository.upsertOne(userId, saved.startDate, saved.id!!),
                    schedule = saved
                )

            saved.isMultiDaySchedule() ->
                MultiDayScheduleResult(
                    multiDayParticipation = multiDayRepository.save(
                        MultiDayParticipation(
                            userId = userId,
                            scheduleId = saved.id!!,
                            startDate = saved.startDate,
                            endDate = saved.endDate
                        )
                    ),
                    schedule = saved
                )

            else ->
                RecurringScheduleResult(schedule = saved)
        }
    }

    fun update(scheduleId: String, request: CreateScheduleRequest): Schedule {
        val found = getById(scheduleId)

        if (found.repeatType == NONE) {
            if (found.isDaily) {
                val dailyScheduleResult = dailySchedulesRepository.upsertOne(found.userId, found.startDate, scheduleId)
            } else {
                val multiDayResult = multiDayRepository.save(
                    MultiDayParticipation(
                        userId = found.userId,
                        scheduleId = scheduleId,
                        startDate = request.startDate,
                        endDate = request.endDate
                    )
                )
            }
        }

        val updated = scheduleRepository.save(
            found.copy(
                categoryId = request.categoryId,
                startDate = request.startDate,
                endDate = request.endDate,
                startTime = request.startTime,
                endTime = request.endTime,
                repeatType = request.repeatType,
                participants = request.participants,
                status = request.status
            )
        )
        TODO("To be implemented")
    }

    fun delete(scheduleId: String) {

    }

    private fun removeDailySchedules(
        userId: String,
        startDate: LocalDate,
        schedule: Schedule
    ): List<WriteModel<Document>> {
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

    private fun Schedule.isDailySchedule(): Boolean = this.repeatType == NONE && this.isDaily
    private fun Schedule.isMultiDaySchedule(): Boolean = this.repeatType == NONE && !this.isDaily
}