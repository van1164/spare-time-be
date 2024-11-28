package com.van1164.resttimebe.schedule.controller

import com.van1164.resttimebe.common.ApiResponse
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.schedule.ScheduleService
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/schedule")
class ScheduleController(
    private val scheduleService: ScheduleService
) {
    @GetMapping
    fun getSchedules(
        @AuthenticationPrincipal user: User,
        @RequestParam rangeStart: LocalDateTime,
        @RequestParam rangeEnd: LocalDateTime
    ): ResponseEntity<ApiResponse<List<Schedule>>> {
        return ResponseEntity.ok()
            .body(
                ApiResponse.with(
                    HttpStatus.OK,
                    "",
                    scheduleService.getSchedules(user.id, rangeStart, rangeEnd)
                )
            )
    }

    @GetMapping("/{schedule_id}")
    fun getScheduleDetails(
        @AuthenticationPrincipal user: User,
        @Parameter(hidden = true)
        @PathVariable(name = "schedule_id") scheduleId: String
    ): ResponseEntity<ApiResponse<Schedule>> {
        return ResponseEntity.ok()
            .body(
                ApiResponse.with(
                    HttpStatus.OK,
                    "",
                    scheduleService.getById(scheduleId)
                )
            )
    }

    @PostMapping
    fun createSchedule(
        @AuthenticationPrincipal user: User,
        @RequestBody request: CreateScheduleRequest
    ): ResponseEntity<ApiResponse<Schedule>> {
        return ResponseEntity.ok()
            .body(
                ApiResponse.with(
                    HttpStatus.OK,
                    "",
                    scheduleService.create(user.id, request)
                )
            )
    }

    @PutMapping("/{schedule_id}")
    fun updateSchedule(
        @AuthenticationPrincipal user: User,
        @PathVariable(name = "schedule_id") scheduleId: String,
        @RequestBody request: CreateScheduleRequest
    ): ResponseEntity<ApiResponse<Schedule>> {
        return ResponseEntity.ok()
            .body(
                ApiResponse.with(
                    HttpStatus.OK,
                    "",
                    scheduleService.update(scheduleId, request)
                )
            )
    }

    @DeleteMapping("/{schedule_id}")
    fun deleteSchedule(
        @AuthenticationPrincipal user: User,
        @PathVariable(name = "schedule_id") scheduleId: String
    ): ResponseEntity<ApiResponse<String>> {
        scheduleService.delete(scheduleId)
        return ResponseEntity.ok()
            .body(
                ApiResponse.with(
                    HttpStatus.OK,
                    "",
                    scheduleId
                )
            )
    }
}