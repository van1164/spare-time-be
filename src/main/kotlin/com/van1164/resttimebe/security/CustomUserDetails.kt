package com.van1164.resttimebe.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


data class CustomUserDetails(
    val id: String?, // DB에서 PK 값
    val loginId: String, // 로그인용 ID 값
    private val password: String?, // 비밀번호
    val email: String, //이메일
    val emailVerified : Boolean = false, //이메일 인증 여부
    val locked : Boolean = false, //계정 잠김 여부
    private val nickname: String?, //닉네임
    private val authorities: Collection<GrantedAuthority> ? = null, //권한 목록
) : UserDetails{
    override fun getAuthorities(): Collection<GrantedAuthority>? = authorities

    override fun getPassword(): String? = password

    override fun getUsername(): String = loginId
}