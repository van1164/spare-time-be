package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.OneTimeSchedules
import org.springframework.data.mongodb.repository.MongoRepository

interface OneTimeSchedulesRepository: MongoRepository<OneTimeSchedules, String>