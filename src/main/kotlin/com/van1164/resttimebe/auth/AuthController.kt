package com.van1164.resttimebe.auth

import com.van1164.resttimebe.common.dto.AppleLoginDto
import com.van1164.resttimebe.common.dto.AppleSignupDto
import com.van1164.resttimebe.common.dto.MobileUserLoginDto
import com.van1164.resttimebe.common.response.JwtTokenResponse
import com.van1164.resttimebe.common.response.MobileLoginResponse
import com.van1164.resttimebe.security.CustomOAuth2User
import com.van1164.resttimebe.security.CustomOAuth2UserService
import com.van1164.resttimebe.security.JwtUtil
import com.van1164.resttimebe.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/login")
class AuthController(
    private val jwtUtil: JwtUtil,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val authService: AuthService,
    private val userService: UserService
) {

    @GetMapping("/oauth2/success")
    fun oauth2LoginSuccess(authentication: Authentication): String {
        val oauth2User = authentication.principal as CustomOAuth2User
        val userId = oauth2User.userId

        // JWT 생성
        val token = jwtUtil.generateJwtToken(userId)

        // JWT를 프론트엔드에 전송 (여기서는 문자열로 반환)
        return token
    }


    @PostMapping("/apple")
    fun appleLogin(@RequestBody appleLoginDto : AppleLoginDto) : MobileLoginResponse {
        userService.getById("apple:${appleLoginDto.userId}")

        return createMobileLoginResponse("apple:${appleLoginDto.userId}")
    }

    @PostMapping("/apple/signup")
    fun appleSignUp(@RequestBody mobileUserLoginDto: AppleSignupDto) : MobileLoginResponse {
        val oAuthUser = customOAuth2UserService.customOAuth2UserForApple(
            name = mobileUserLoginDto.name,
            email = mobileUserLoginDto.email,
            userNameAttributeNameValue = mobileUserLoginDto.userNameAttributeNameValue,
            appleId = mobileUserLoginDto.userId
        )
        return createMobileLoginResponse(oAuthUser.userId)
    }

    @PostMapping("")
    @Transactional
    fun mobileLogin(@RequestBody mobileUserLoginDto: MobileUserLoginDto): MobileLoginResponse {
         val oAuthUser = customOAuth2UserService.customOAuth2User(
             name = mobileUserLoginDto.name,
             email = mobileUserLoginDto.email,
             registrationId = mobileUserLoginDto.registrationId,
             userNameAttributeNameValue = mobileUserLoginDto.userNameAttributeNameValue
         )
        return createMobileLoginResponse(oAuthUser.userId)
    }

    private fun createMobileLoginResponse(userId : String): MobileLoginResponse {
        return MobileLoginResponse(
            jwt =  jwtUtil.generateJwtToken(userId),
            refreshToken = jwtUtil.generateRefreshToken(userId)
        )
    }

    @PostMapping("/refresh")
    fun refresh(@RequestParam("refreshToken") refreshToken : String): JwtTokenResponse {
        return JwtTokenResponse(
            authService.createNewToken(refreshToken)
        )

    }

    @PostMapping("/verify")
    fun verify(@RequestParam("jwt") jwt : String): ResponseEntity<Any> {
        if(!jwtUtil.validateToken(jwt,jwtUtil.extractUsername(jwt))) return ResponseEntity.badRequest().build<Any>()
       return ResponseEntity.ok().build()
    }


}