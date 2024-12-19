package com.van1164.resttimebe.schedule

import com.mongodb.client.model.*
import com.van1164.resttimebe.common.exception.ErrorCode
import com.van1164.resttimebe.common.exception.GlobalExceptions.NotFoundException
import com.van1164.resttimebe.domain.RepeatType.NONE
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import org.bson.Document
import org.bson.conversions.Bson
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
        val oneTimeScheduleList =
            scheduleRepository.getOneTimeScheduleList(userId, rangeStart, rangeEnd)
        val oneTimeSchedules = scheduleRepository.findAllById(oneTimeScheduleList)
        val recurringSchedules =
            scheduleRepository.getRecurringSchedules(userId, rangeStart, rangeEnd)

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
            val operations = mutableListOf<UpdateOneModel<Document>>()
            request.participants.forEach { participant ->
                getYearMonthSequence(YearMonth.from(request.startTime), YearMonth.from(request.endTime)).forEach { time ->
                    operations.add(upsertOneTimeSchedules(participant, time, savedSchedule))
                }
            }
            if (operations.isNotEmpty()) {
                mongoTemplate.db.getCollection("one_time_schedules")
                    .bulkWrite(operations)
            }
        }

        return savedSchedule
    }

    fun update(scheduleId: String, request: CreateScheduleRequest): Schedule {
        val found = getById(scheduleId)
        val operations = mutableListOf<WriteModel<Document>>()

        // 시간 범위 계산
        val originalRange = YearMonth.from(found.startTime)..YearMonth.from(found.endTime)
        val updatedRange = YearMonth.from(request.startTime)..YearMonth.from(request.endTime)
        val toRemoveTimes = getYearMonthSequence(originalRange.start, originalRange.endInclusive).filter { it !in updatedRange }
        val toAddTimes = getYearMonthSequence(updatedRange.start, updatedRange.endInclusive).filter { it !in originalRange }

        (found.participants - request.participants).forEach { participant ->
            getYearMonthSequence(originalRange.start, originalRange.endInclusive).forEach { time ->
                removeOneTimeSchedules(participant, time, found).forEach { operation ->
                    operations.add(operation)
                }
            }
        }

        request.participants.forEach { participant ->
            val participantInOriginal = participant in found.participants
            // timesToAdd 초기화
            val timesToAdd: Sequence<YearMonth> = if (participantInOriginal) {
                toAddTimes
            } else {
                generateSequence(updatedRange.start) { current ->
                    if (current < updatedRange.endInclusive) current.plusMonths(1) else null
                }
            }

            // timesToRemove 초기화
            val timesToRemove: Sequence<YearMonth> = if (participantInOriginal) {
                toRemoveTimes
            } else {
                emptySequence<YearMonth>()
            }

            timesToRemove.forEach { time ->
                removeOneTimeSchedules(participant, time, found).forEach { operation ->
                    operations.add(operation)
                }
            }

            timesToAdd.forEach { time ->
                operations.add(upsertOneTimeSchedules(participant, time, found))
            }
        }

        if (operations.isNotEmpty()) {
            mongoTemplate.db.getCollection("one_time_schedules")
                .bulkWrite(operations)
        }

        return scheduleRepository.save(
            found.copy(
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
        val operations = mutableListOf<WriteModel<Document>>()
        schedule.participants.forEach { participant ->
            getYearMonthSequence(YearMonth.from(schedule.startTime), YearMonth.from(schedule.endTime)).forEach { time ->
                removeOneTimeSchedules(participant, time, schedule).forEach { operation ->
                    operations.add(operation)
                }
            }
        }
        if (operations.isNotEmpty()) {
            mongoTemplate.db.getCollection("one_time_schedules")
                .bulkWrite(operations)
        }
        scheduleRepository.deleteById(scheduleId)
    }

    private fun upsertOneTimeSchedules(userId: String, time: YearMonth, schedule: Schedule): UpdateOneModel<Document> {
            val filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("partitionYear", time.year),
                Filters.eq("partitionMonth", time.monthValue),
            )
            val update = Updates.combine(
                Updates.setOnInsert("userId", userId),
                Updates.setOnInsert("partitionYear", time.year),
                Updates.setOnInsert("partitionMonth", time.monthValue),
                Updates.addToSet("schedules", schedule.id)
            )
            return UpdateOneModel<Document>(filter, update, UpdateOptions().upsert(true))
    }

    private fun removeOneTimeSchedules(userId: String, time: YearMonth, schedule: Schedule): List<WriteModel<Document>> {
        val filter: Bson = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("partitionYear", time.year),
            Filters.eq("partitionMonth", time.monthValue)
        )
        val pullUpdate: Bson = Updates.combine(Updates.pull("schedules", schedule.id))
        val deleteCondition: Bson = Filters.and(filter, Filters.size("schedules", 0))

        return listOf(
            UpdateOneModel(filter, pullUpdate), // 업데이트 작업
            DeleteOneModel(deleteCondition) // 삭제 작업
        )
    }

    private fun getYearMonthSequence(start: YearMonth, end: YearMonth): Sequence<YearMonth> {
        return generateSequence(start) { it.plusMonths(1) }.takeWhile { it <= end }
    }

}