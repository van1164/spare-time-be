package com.van1164.resttimebe.schedule.repository

import com.van1164.resttimebe.domain.MultiDayParticipation
import org.springframework.data.mongodb.repository.MongoRepository

interface MultiDayParticipationRepository: MongoRepository<MultiDayParticipation, String> {
}