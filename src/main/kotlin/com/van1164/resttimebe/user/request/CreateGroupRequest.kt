package com.van1164.resttimebe.user.request

data class CreateGroupRequest(
    val groupName: String,
    val userIdList: List<String>,
)
