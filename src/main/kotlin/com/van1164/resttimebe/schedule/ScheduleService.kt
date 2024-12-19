package com.van1164.resttimebe.schedule

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.van1164.resttimebe.common.exception.ErrorCode
import com.van1164.resttimebe.common.exception.GlobalExceptions.NotFoundException
import com.van1164.resttimebe.domain.RepeatType.NONE
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
        val oneTimeScheduleList = scheduleRepository.getOneTimeScheduleList(userId, rangeStart, rangeEnd)
        val oneTimeSchedules = scheduleRepository.findAllById(oneTimeScheduleList)
        val recurringSchedules = scheduleRepository.getRecurringSchedules(userId, rangeStart, rangeEnd)

        return oneTimeSchedules + recurringSchedules
    }

    fun getById(scheduleId: String): Schedule {
        return scheduleRepository.findById(scheduleId).orElseThrow {
            throw NotFoundException(ErrorCode.SCHEDULE_NOT_FOUND)
        }
    }

    fun create(userId: String, request: CreateScheduleRequest): Schedule {
        val savedSchedule = scheduleRepository.save(request.toDomain(userId))

        if (request.repeatType == NONE) {
            insertOneTimeSchedules(request.participants, savedSchedule)
        }

        return savedSchedule
    }

    fun update(scheduleId: String, request: CreateScheduleRequest): Schedule {
        val found = getById(scheduleId)
        if (found.repeatType != NONE && request.repeatType == NONE) {
             insertOneTimeSchedules(request.participants, found)
        }
        else if (found.repeatType == NONE && request.repeatType != NONE) {
            removeOneTimeSchedules(request.participants, found)
        }
        return scheduleRepository.save(
            Schedule(
                id = found.id,
                userId = found.userId,
                category = request.category,
                startTime = request.startTime,
                endTime = request.endTime,
                repeatType = request.repeatType,
                participants = request.participants,
                status = request.status
            )
        )
    }

    fun delete(scheduleId: String) {
        val schedule = getById(scheduleId)
        removeOneTimeSchedules(schedule.participants, schedule)
        scheduleRepository.deleteById(scheduleId)
    }

    private fun insertOneTimeSchedules(userIdList: Set<String>, schedule: Schedule) {
        val operations = mutableListOf<UpdateOneModel<Document>>()
        var current = YearMonth.from(schedule.startTime)
        val end = YearMonth.from(schedule.endTime)

        for (userId in userIdList) {
            while (current <= end) {
                val partitionYear = current.year
                val partitionMonth = current.monthValue

                val filter = Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("partitionYear", partitionYear),
                    Filters.eq("partitionMonth", partitionMonth)
                )
                val update = Updates.combine(
                    Updates.setOnInsert("userId", userId),
                    Updates.setOnInsert("partitionYear", partitionYear),
                    Updates.setOnInsert("partitionMonth", partitionMonth),
                    Updates.addToSet("schedules", schedule.id)
                )
                operations.add(UpdateOneModel(filter, update, UpdateOptions().upsert(true)))

                current = current.plusMonths(1)
            }
        }

        if (operations.isNotEmpty()) {
            mongoTemplate.db.getCollection("one_time_schedules")
                .bulkWrite(operations)
        }
    }

    private fun syncUpdatedOneTimeSchedules(userIdList: Set<String>, schedule: Schedule) {
        val operations = mutableListOf<UpdateOneModel<Document>>()

        val originalRange = YearMonth.from(schedule.startTime)..YearMonth.from(schedule.endTime)
        val updatedRange = YearMonth.from(schedule.startTime)..YearMonth.from(schedule.endTime)

        for (userId in userIdList) {
            for (month in getYearMonthSequence(originalRange.start, originalRange.endInclusive)) {
                if (month !in updatedRange) {
                    val filter = Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("partitionYear", month.year),
                        Filters.eq("partitionMonth", month.monthValue)
                    )
                    val update = Updates.pull("schedules", schedule.id)
                    operations.add(UpdateOneModel(filter, update))
                }
            }

            for (month in getYearMonthSequence(updatedRange.start, updatedRange.endInclusive)) {
                if (month !in originalRange) {
                    val filter = Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("partitionYear", month.year),
                        Filters.eq("partitionMonth", month.monthValue)
                    )
                    val update = Updates.combine(
                        Updates.setOnInsert("userId", userId),
                        Updates.setOnInsert("partitionYear", month.year),
                        Updates.setOnInsert("partitionMonth", month.monthValue),
                        Updates.addToSet("schedules", schedule.id)
                    )
                    operations.add(UpdateOneModel(filter, update, UpdateOptions().upsert(true)))
                }
            }
        }


        if (operations.isNotEmpty()) {
            mongoTemplate.db.getCollection("one_time_schedules")
                .bulkWrite(operations)
        }
    }

    private fun removeOneTimeSchedules(userIdList: Set<String>, schedule: Schedule) {
        val operations = mutableListOf<UpdateOneModel<Document>>()
        val range = YearMonth.from(schedule.startTime)..YearMonth.from(schedule.endTime)

        for (userId in userIdList) {
            for (month in getYearMonthSequence(range.start, range.endInclusive)) {
                val filter = Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("partitionYear", month.year),
                    Filters.eq("partitionMonth", month.monthValue)
                )
                val update = Updates.pull("schedules", schedule.id)
                operations.add(UpdateOneModel(filter, update))
            }
        }

        if (operations.isNotEmpty()) {
            mongoTemplate.db.getCollection("one_time_schedules")
                .bulkWrite(operations)
        }
    }

    private fun getYearMonthSequence(start: YearMonth, end: YearMonth): Sequence<YearMonth> {
        return generateSequence(start) { it.plusMonths(1) }.takeWhile { it <= end }
    }

}