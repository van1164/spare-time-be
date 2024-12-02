package com.van1164.resttimebe.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuthSuccessHandler: OAuthSuccessHandler,
    private val jwtRequestFilter: JwtRequestFilter,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf {
                it.disable()
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/**").permitAll()
                    .requestMatchers(
                        "/swagger",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                    )
//                    .permitAll()
                    .hasAnyRole("ADMIN")
                    //.requestMatchers("/admin/**").hasRole(Role.ADMIN.name)
                    .requestMatchers("/login-admin", "/css/**", "/js/**").permitAll()
                    .requestMatchers("/login/**", "/auth/**", "/oauth2/**")
                    .permitAll()
                    .requestMatchers(
                        "/",
                        "/error",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js"
                    )
                    .permitAll()
                    //모바일 사용자 로그인
                    .requestMatchers("/api/v1/login/**").permitAll()
                    .requestMatchers("/test/**").permitAll()
                    .requestMatchers("/health")
                    .permitAll()
                    .requestMatchers("/**").authenticated()
            }
            .formLogin{test ->
                test.loginPage("/admin/login")
            }
            .logout {
                it.logoutSuccessUrl("/")
            }
            .oauth2Login {
                it.successHandler(oAuthSuccessHandler)
            }
            .oauth2Login(
                Customizer.withDefaults()
            )
            .exceptionHandling {
                it.authenticationEntryPoint(customAuthenticationEntryPoint)
            }
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .withUser("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}