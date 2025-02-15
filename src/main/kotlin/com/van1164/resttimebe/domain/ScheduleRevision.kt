package com.van1164.resttimebe.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Document(collection = "schedule_revisions")
data class ScheduleRevision(
    @Id
    val id: String? = null,
    val scheduleId: String,
    val targetDate: LocalDate,
    val isDeleted: Boolean = false,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val participants: Set<String>,
)

/**
 * TODO
 * 수정일자의 재정의는 이 일정, 이 일정 및 향후 일정, 모든 일정의 선택 옵션을 가진다.
 * 반복 일정 중 다음의 수정 요건이 생기면 어떡할까?
 * targetDate 가 있는데,
 * startDate 를 targetDate 이전으로 수정하고,
 * startDate 와 targetDate 사이에 있는 3의 일정을 선택하여, 이 일정 및 향후 일정으로 수정하면?
 * revision 은 유지하고, 그 이외를 모두 수정하는 게 맞다.
 * 그런데 이와 동시에 targetDate 에 해당하는 날짜가 수정 후의 날짜에 포함되면 어떻게 해야 할까?
 * 분기 처리가 필요하지 싶다.
 * targetDate 가 쿼리한 날짜 이후이면서, startDate 가 targetDate 이전이면, revision 을 유지하고, 그 이외를 수정한다.
 * 조회 시에 쿼리 이전의 revision 의 targetDate 는 어떻게 하나? 그냥 쿼리할 시에는 원본을 무시하고 revision 이 조회될 텐데.
 * 처음에 revision 을 생성할 때, targetDate 보다 이전으로 startDate 를 만든다면, 2 개의 revision 을 만든다. 하나는 targetDate = startDate 인 revision,
 * 다른 하나는 targetDate = targetDate 이면서, isDeleted 만 true 인 revision 이다.
 * 이렇게 하면, 수정이 여러 번 일어나도 불일치는 없어진다.
 * 하지만, targetDate 에 같은 성격의 일정 2개가 겹칠 때는 또 문제가 된다.
 *
 * 그래서 전체적인 설계에 변경이 필요할지도 모른다.
 */
