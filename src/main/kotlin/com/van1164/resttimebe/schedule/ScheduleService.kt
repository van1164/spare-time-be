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
import com.van1164.resttimebe.schedule.request.UpdateScheduleRequest
import com.van1164.resttimebe.schedule.response.ScheduleCreateResponse
import com.van1164.resttimebe.schedule.response.ScheduleCreateResponse.*
import com.van1164.resttimebe.schedule.response.ScheduleReadResponse
import com.van1164.resttimebe.schedule.response.ScheduleUpdateResponse
import org.bson.Document
import org.bson.conversions.Bson
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth

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

        when {
            saved.isDailySchedule() ->
                return DailyScheduleResponse(
                    dailyScheduleUpdateResult = dailySchedulesRepository.upsertOne(userId, saved.startDate, saved.id!!),
                    schedule = saved
                )

            saved.isMultiDaySchedule() ->
                return MultiDayScheduleResponse(
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
                return RecurringScheduleResponse(schedule = saved)
        }
    }

    //TODO: 반환 객체 점검
    fun update(scheduleId: String, request: UpdateScheduleRequest): ScheduleUpdateResponse {
        val found = getById(scheduleId)

        if (found.repeatType == NONE && request.repeatType != NONE) {
            removeParticipantsFromSchedule(found)
            return ScheduleUpdateResponse(
                scheduleRepository.save(request.toDomain(scheduleId))
            )
        }

        val removedParticipants = found.participants - request.participants
        if (removedParticipants.isNotEmpty()) {
            removeParticipantsFromSchedule(found, removedParticipants)
        }

        upsertParticipantsToSchedule(found, request, request.participants)

        return ScheduleUpdateResponse(
            scheduleRepository.save(request.toDomain(scheduleId))
        )
    }

    fun delete(scheduleId: String) {
        val found = getById(scheduleId)
        removeParticipantsFromSchedule(found)
        scheduleRepository.deleteById(scheduleId)
    }

    private fun removeParticipantsFromSchedule(schedule: Schedule, participants: Set<String> = schedule.participants) {
        val operations = mutableListOf<WriteModel<Document>>()

        when {
            schedule.isDailySchedule() -> {
                participants.forEach { participant ->
                    operations.addAll(prepareDailyScheduleRemoval(participant, schedule.startDate, schedule))
                }
                executeBulkWrite("daily_schedules", operations)
            }

            schedule.isMultiDaySchedule() -> {
                participants.forEach { participant ->
                    operations.add(prepareMultiDayParticipationRemoval(participant, schedule))
                }
                executeBulkWrite("multi_day_participation", operations)
            }
        }
    }

    private fun upsertParticipantsToSchedule(schedule: Schedule, request: UpdateScheduleRequest, participants: Set<String>) {
        val operations = mutableListOf<WriteModel<Document>>()

        when {
            schedule.isDailySchedule() -> {
                val isYearMonthUpdated = isYearMonthUpdated(schedule, request)
                participants.forEach { participant ->
                    if (isYearMonthUpdated) {
                        operations.addAll(prepareDailyScheduleRemoval(participant, schedule.startDate, schedule))
                    }
                    operations.add(prepareDailyScheduleUpsert(participant, schedule.startDate, schedule))
                }
                executeBulkWrite("daily_schedules", operations)
            }

            schedule.isMultiDaySchedule() -> {
                participants.forEach { participant ->
                    operations.add(prepareMultiDayParticipationUpsert(participant, schedule))
                }
                executeBulkWrite("multi_day_participation", operations)
            }
        }
    }

    private fun executeBulkWrite(collectionName: String, operations: List<WriteModel<Document>>) {
        if (operations.isNotEmpty()) {
            mongoTemplate.getCollection(collectionName).bulkWrite(operations)
        }
    }

    private fun prepareDailyScheduleUpsert(
        userId: String,
        startDate: LocalDate,
        schedule: Schedule
    ): WriteModel<Document> {
        val filter: Bson = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("partitionYear", startDate.year),
            Filters.eq("partitionMonth", startDate.monthValue)
        )
        val update: Bson = Updates.combine(
            Updates.setOnInsert("userId", userId),
            Updates.setOnInsert("partitionYear", startDate.year),
            Updates.setOnInsert("partitionMonth", startDate.monthValue),
            Updates.addToSet("schedules", schedule.id)
        )
        val options: UpdateOptions = UpdateOptions().upsert(true)
        return UpdateOneModel(filter, update, options)
    }

    private fun prepareMultiDayParticipationUpsert(
        userId: String,
        schedule: Schedule
    ): WriteModel<Document> {
        val filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("scheduleId", schedule.id)
        )
        val update = Updates.combine(
            Updates.setOnInsert("userId", userId),
            Updates.setOnInsert("scheduleId", schedule.id),
            Updates.setOnInsert("startDate", schedule.startDate),
            Updates.setOnInsert("endDate", schedule.endDate)
        )
        val options = UpdateOptions().upsert(true)
        return UpdateOneModel(filter, update, options)
    }

    private fun prepareDailyScheduleRemoval(
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

    private fun prepareMultiDayParticipationRemoval(
        userId: String,
        schedule: Schedule
    ): WriteModel<Document> {

        val filter: Bson = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("scheduleId", schedule.id),
        )
        return DeleteOneModel(filter)
    }

    private fun Schedule.isDailySchedule(): Boolean = this.repeatType == NONE && this.isDaily
    private fun Schedule.isMultiDaySchedule(): Boolean = this.repeatType == NONE && !this.isDaily
    private fun isYearMonthUpdated(old: Schedule, new: UpdateScheduleRequest): Boolean {
        val oldYearMonth = YearMonth.of(old.startDate.year, old.startDate.month)
        val newYearMonth = YearMonth.of(new.startDate.year, new.startDate.month)
        return oldYearMonth != newYearMonth
    }
}