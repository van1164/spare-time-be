package com.van1164.resttimebe.security

import com.van1164.resttimebe.common.exception.ErrorCode
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.UserService
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val httpSession: HttpSession
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): CustomOAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oauth2User = delegate.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val userNameAttributeNameKey =
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
        val userNameAttributeNameValue = oauth2User.attributes[userNameAttributeNameKey] as String?
            ?: run { throw GlobalExceptions.InternalErrorException(ErrorCode.SOCIAL_EMAIL_LOAD_FAIL) }
        val email = oauth2User.attributes["email"] as String?
            ?: run { throw GlobalExceptions.InternalErrorException(ErrorCode.SOCIAL_EMAIL_LOAD_FAIL) }
        val name = oauth2User.attributes["name"] as String?
            ?: run { throw GlobalExceptions.InternalErrorException(ErrorCode.SOCIAL_NAME_LOAD_FAIL) }

        return customOAuth2User(
            registrationId,
            email,
            name,
            oauth2User.attributes,
            userNameAttributeNameKey,
            userNameAttributeNameValue
        )
    }

    fun customOAuth2User(
        registrationId: String,
        email: String,
        name: String,
        attributes: MutableMap<String, Any>? = null,
        userNameAttributeNameKey: String = "sub",
        userNameAttributeNameValue :String
    ): CustomOAuth2User {
        val userId = "$registrationId:$email"

        val user = findOrSave(userId, email, name)
        val attr = attributes ?: let {
            mutableMapOf<String, Any>("email" to email, "name" to name,userNameAttributeNameKey to userNameAttributeNameValue)
        }

        return CustomOAuth2User(
            oauth2User = DefaultOAuth2User(
                setOf(SimpleGrantedAuthority(user.role.key)),
                attr,
                userNameAttributeNameKey
            ),
            userId = userId
        )
    }


    fun customOAuth2UserForApple(
        appleId: String,
        email: String,
        name: String,
        attributes: MutableMap<String, Any>? = null,
        userNameAttributeNameKey: String = "sub",
        userNameAttributeNameValue :String
    ): CustomOAuth2User {
        val userId = "apple:$appleId"

        val user = findOrSave(userId, email, name)
        val attr = attributes ?: let {
            mutableMapOf<String, Any>("email" to email, "name" to name,userNameAttributeNameKey to userNameAttributeNameValue)
        }
        return CustomOAuth2User(
            oauth2User = DefaultOAuth2User(
                setOf(SimpleGrantedAuthority(user.role.key)),
                attr,
                userNameAttributeNameKey
            ),
            userId = userId
        )
    }

    private fun findOrSave(userId: String, email: String, name: String): User {
        val user = userRepository.findByUserId(userId)
            ?: User(
                email = email,
                name = name,
                userId = userId,
                displayName = name,

            )
        return userRepository.save(user)
    }
}