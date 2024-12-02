package com.van1164.resttimebe.security

import com.van1164.resttimebe.common.exception.GlobalExceptions
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtRequestFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")
        var username: String? = null
        var jwt: String? = null

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7)
                username = jwtUtil.extractUsername(jwt)
            }
            if (username != null && SecurityContextHolder.getContext().authentication == null && jwt != null) {
                val userDetails = userDetailsService.loadUserByUsername(username) as CustomUserDetails
                if (jwtUtil.validateToken(jwt, userDetails.loginId)) {
                    val authenticationToken =
                        UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    SecurityContextHolder.getContext().authentication = authenticationToken
                }
            }
            chain.doFilter(request, response)
        } catch (e: ExpiredJwtException){
            response.sendError(401,"JWTEXP")
        } catch (e: GlobalExceptions.NotFoundException){
            response.sendError(401,"NOT FOUND USER")
        }

    }
}