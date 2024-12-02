package com.van1164.resttimebe.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class JwtUtil(
    @Value("\${secretKey}")
    private var secretKey: String
) {

    @PostConstruct
    fun init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }
    


    fun generateJwtToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + 1000 * 60 * 60 * 10) // 10시간 유효
        return generateToken(username, now, validity)
    }

    fun generateRefreshToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + Int.MAX_VALUE) // 10시간 유효
        return generateToken(username, now, validity)
    }

    private fun generateToken(username: String, now: Date, validity: Date): String {
        val claims: Claims = Jwts.claims().setSubject(username)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun validateToken(token: String, username: String): Boolean {
        val claims = extractAllClaims(token)
        val tokenUsername = claims.subject
        println(tokenUsername == username )
        println(!isTokenExpired(claims))
        return (tokenUsername == username && !isTokenExpired(claims))
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
    }

    fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }

    fun generateAuthToken(username: String): String {
        val date = LocalDateTime.now().plusYears(3)
        val validity = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
        return generateToken(username, Date(), validity)
    }
}