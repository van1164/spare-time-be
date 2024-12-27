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

    //TODO: 모든 부작용을 검증할 수 있도록 반환 객체 추가가 필요함
    fun create(userId: String, request: CreateScheduleRequest): Schedule {
        val saved = scheduleRepository.save(request.toDomain(userId))

        if (saved.repeatType == NONE) {
            if (saved.isDaily) {
                dailySchedulesRepository.upsertOne(userId, saved.startDate, saved.id!!)
            } else {
                multiDayRepository.save(
                    MultiDayParticipation(
                        userId = userId,
                        scheduleId = saved.id!!,
                        startDate = saved.startDate,
                        endDate = saved.endDate
                    )
                )
            }
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