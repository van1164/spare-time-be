package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.schedule.repository.DailySchedulesRepository
import com.van1164.resttimebe.schedule.repository.ScheduleRepository
import com.van1164.resttimebe.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.stream.Stream

@SpringBootTest
class ScheduleServiceTest @Autowired constructor(
    private val scheduleService: ScheduleService,
    private val dailySchedulesRepository: DailySchedulesRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
) {
    @BeforeEach
    fun setUp() {
        dailySchedulesRepository.deleteAll()
        scheduleRepository.deleteAll()
        userRepository.deleteAll()
    }

    @ParameterizedTest
    @MethodSource("provideSchedulesAndDateRanges")
    fun `getSchedules should check if schedule is within date range`(
        testName: String,
        scheduleStartTime: LocalDateTime,
        scheduleEndTime: LocalDateTime,
        rangeStart: LocalDateTime,
        rangeEnd: LocalDateTime,
        hasOverlap: Boolean
    ) {

    }


    @Test
    fun `getById should return schedule successfully`() {

    }

    @Test
    fun `getById should throw exception when schedule not found`() {

    }

    @Test
    fun `create should create schedule successfully`() {

    }

    @Test
    fun `upsertOneTimeSchedules should save inverted index in collection`() {

    }

    @Test
    fun `upsertOneTimeSchedules should save inverted index via bulk write`() {

    }

    @Test
    fun `update should update schedule successfully`() {

    }

    @Test
    fun `update should handle transition from regular schedule to repeat schedule`() {

    }

    @Test
    fun `update should add new participants and their schedules`() {

    }

    @Test
    fun `update should remove participants and their schedules`() {

    }

    @Test
    fun `update should modify time range for participants`() {

    }

    @Test
    fun `update should handle no changes gracefully`() {

    }

    @Test
    fun `update should bulk write changes to the database`() {

    }


    @Test
    fun `delete should delete schedule successfully`() {

    }

    companion object {
        @JvmStatic
        fun provideSchedulesAndDateRanges(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "Case 1: Same year",
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 1: Same year - Excluded",
                LocalDateTime.of(2023, 4, 1, 0, 0),
                LocalDateTime.of(2023, 4, 30, 23, 59),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                false
            ),
            Arguments.of(
                "Case 2: Different years",
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2022, 12, 31, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                true
            ),
            Arguments.of(
                "Case 2: Different years",
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 1, 31, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                true
            ),
            Arguments.of(
                "Case 2: Excluded",
                LocalDateTime.of(2023, 3, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                false
            ),
            Arguments.of(
                "Case 3: Extended range",
                LocalDateTime.of(2023, 6, 1, 0, 0),
                LocalDateTime.of(2023, 6, 30, 23, 59),
                LocalDateTime.of(2022, 12, 1, 0, 0),
                LocalDateTime.of(2024, 1, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 4: Specific months",
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 2, 28, 23, 59),
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 4: Specific months",
                LocalDateTime.of(2023, 3, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 4: Excluded",
                LocalDateTime.of(2023, 4, 1, 0, 0),
                LocalDateTime.of(2023, 4, 30, 23, 59),
                LocalDateTime.of(2023, 2, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59),
                false
            ),
            Arguments.of(
                "Case 5: Full year range",
                LocalDateTime.of(2023, 6, 1, 0, 0),
                LocalDateTime.of(2023, 6, 30, 23, 59),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                true
            ),
            Arguments.of(
                "Case 6: Included",
                LocalDateTime.of(2023, 12, 31, 23, 59),
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                true
            ),
        )
    }
}
