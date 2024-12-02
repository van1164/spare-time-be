package com.van1164.resttimebe.security

import com.van1164.resttimebe.domain.User
import com.van1164.resttimebe.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service


@Service
class CustomUserDetailService(
    val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(loginId : String): UserDetails? {
        val user = userRepository.findByUserId(loginId) ?: return null
        val userDetails = userToUserDetails(user)
        return userDetails
    }

    private fun userToUserDetails(user : User): CustomUserDetails {
        return CustomUserDetails(
            email = user.email,
            loginId = user.userId,
            id = user.id,
            nickname = user.displayName,
            authorities = setOf(SimpleGrantedAuthority(user.role.key)),
            password = "",
        )
    }
}