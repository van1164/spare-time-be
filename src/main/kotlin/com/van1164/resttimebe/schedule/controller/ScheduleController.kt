package com.van1164.resttimebe.schedule.controller

import com.van1164.resttimebe.common.exception.ErrorCode.*
import com.van1164.resttimebe.common.exception.GlobalExceptions.*
import com.van1164.resttimebe.common.response.ApiResponse
import com.van1164.resttimebe.domain.Schedule
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.schedule.ScheduleService
import com.van1164.resttimebe.schedule.request.CreateScheduleRequest
import com.van1164.resttimebe.security.CustomUserDetails
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
        @AuthenticationPrincipal user: CustomUserDetails,
        @RequestParam rangeStart: LocalDateTime,
        @RequestParam rangeEnd: LocalDateTime
    ): ResponseEntity<ApiResponse<List<Schedule>>> {
        user.id ?: throw IllegalStateException(USER_ID_NOT_INITIALIZED)
        return ResponseEntity.ok()
            .body(
                ApiResponse.with(
                    HttpStatus.OK,
                    "",
                    scheduleService.getSchedules(user.loginId, rangeStart, rangeEnd)
                )
            )
    }

    @GetMapping("/{schedule_id}")
    fun getScheduleDetails(
        @AuthenticationPrincipal user: CustomUserDetails,
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
        @AuthenticationPrincipal user: CustomUserDetails,
        @RequestBody request: CreateScheduleRequest
    ): ResponseEntity<ApiResponse<Schedule>> {
        return ResponseEntity.ok()
            .body(
                ApiResponse.with(
                    HttpStatus.OK,
                    "",
                    scheduleService.create(user.loginId, request)
                )
            )
    }

    @PutMapping("/{schedule_id}")
    fun updateSchedule(
        @AuthenticationPrincipal user: CustomUserDetails,
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
        @AuthenticationPrincipal user: CustomUserDetails,
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