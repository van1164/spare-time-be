package com.van1164.resttimebe.schedule

import com.van1164.resttimebe.domain.Schedule
import org.springframework.data.mongodb.repository.MongoRepository

interface ScheduleRepository : MongoRepository<Schedule, String> {
}