package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.DailySchedules
import org.springframework.data.mongodb.repository.MongoRepository

interface DailySchedulesRepository: MongoRepository<DailySchedules, String>, DailySchedulesRepositoryCustom {
}