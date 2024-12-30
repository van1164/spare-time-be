package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.Schedule
import org.springframework.data.mongodb.repository.MongoRepository

interface ScheduleRepository : ScheduleRepositoryCustom, MongoRepository<Schedule, String>