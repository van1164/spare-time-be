package com.van1164.resttimebe.schedule

import com.mongodb.client.model.*
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
            insertOneTimeSchedules(request.participants, savedSchedule)
        }

        return savedSchedule
    }

    fun update(scheduleId: String, request: CreateScheduleRequest): Schedule {
        val found = getById(scheduleId)
        val operations = mutableListOf<UpdateOneModel<Document>>()

        // 시간 범위 계산
        val originalRange = YearMonth.from(found.startTime)..YearMonth.from(found.endTime)
        val updatedRange = YearMonth.from(request.startTime)..YearMonth.from(request.endTime)
        val toRemoveTimes = getYearMonthSequence(originalRange.start, originalRange.endInclusive).filter { it !in updatedRange }
        val toAddTimes = getYearMonthSequence(updatedRange.start, updatedRange.endInclusive).filter { it !in originalRange }

        (found.participants - request.participants).forEach { participant ->
            for (month in getYearMonthSequence(originalRange.start, originalRange.endInclusive)) {
                val filter = Filters.and(
                    Filters.eq("userId", participant),
                    Filters.eq("partitionYear", month.year),
                    Filters.eq("partitionMonth", month.monthValue)
                )
                val update = Updates.pull("schedules", found.id)
                operations.add(UpdateOneModel(filter, update))
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

            // 삭제 작업 추가
            timesToRemove.forEach { time ->
                val filter = Filters.and(
                    Filters.eq("userId", participant),
                    Filters.eq("partitionYear", time.year),
                    Filters.eq("partitionMonth", time.monthValue)
                )
                val update = Updates.pull("schedules", found.id)
                operations.add(UpdateOneModel(filter, update))
            }

            // 생성/업데이트 작업 추가
            timesToAdd.forEach { time ->
                val filter = Filters.and(
                    Filters.eq("userId", participant),
                    Filters.eq("partitionYear", time.year),
                    Filters.eq("partitionMonth", time.monthValue)
                )
                val update = Updates.combine(
                    Updates.setOnInsert("userId", participant),
                    Updates.setOnInsert("partitionYear", time.year),
                    Updates.setOnInsert("partitionMonth", time.monthValue),
                    Updates.addToSet("schedules", found.id)
                )
                operations.add(UpdateOneModel(filter, update, UpdateOptions().upsert(true)))
            }
        }

        // 최적화된 연산 실행
        if (operations.isNotEmpty()) {
            mongoTemplate.db.getCollection("one_time_schedules")
                .bulkWrite(operations)
        }

        // 최종 저장
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