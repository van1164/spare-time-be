package com.van1164.resttimebe.user.result

data class GroupMemberUpdateResult(
    val groupId: String,
    val previousTotalMembers: Int,
    val previousMemberIdList: List<String>,
    val currentTotalMembers: Int,
    val currentMemberIdList: List<String>
)
