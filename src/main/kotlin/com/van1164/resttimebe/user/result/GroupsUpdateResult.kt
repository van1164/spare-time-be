package com.van1164.resttimebe.user.result

import com.van1164.resttimebe.domain.Group

data class GroupsUpdateResult(
    val previousTotalGroups: Int,
    val previousGroupList: List<Group>,
    val currentTotalGroups: Int,
    val currentGroupList: List<Group>
)
