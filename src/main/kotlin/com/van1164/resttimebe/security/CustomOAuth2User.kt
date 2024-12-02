package com.van1164.resttimebe.security

import org.springframework.security.oauth2.core.user.OAuth2User

data class CustomOAuth2User(
    val oauth2User: OAuth2User,
    val userId : String
) : OAuth2User by oauth2User {

}