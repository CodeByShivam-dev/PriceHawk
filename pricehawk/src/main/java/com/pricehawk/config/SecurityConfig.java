package com.pricehawk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central Spring Security configuration for PriceHawk application.
 *
 * Design Rationale:
 * - Our current system exposes only REST APIs; no web-based login pages needed.
 * - CSRF protection is disabled because we serve stateless APIs (typical for REST + JS frontend or mobile clients).
 * - All /api/** endpoints are currently public. Authentication/authorization can be added later when needed.
 * - Disables default Spring login form and basic auth to reduce attack surface and prevent accidental exposures.
 *
 * Notes for future enhancements:
 * - Introduce JWT or OAuth2 for secured endpoints when user accounts are implemented.
 * - Add role-based access control for admin operations (tracking, analytics dashboards).
 */
@Configuration
public class SecurityConfig
{

    /**
     * Define the main security filter chain.
     *
     * Key points:
     * 1️⃣ CSRF is off for stateless REST APIs.
     * 2️⃣ All /api/** endpoints are publicly accessible for now.
     * 3️⃣ Default login page and HTTP Basic auth are disabled to avoid confusion/exposure.
     *
     * @param http Spring HttpSecurity builder
     * @return fully built SecurityFilterChain
     * @throws Exception on configuration error
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST APIs ke liye CSRF disable
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // /api/** currently public
                        .anyRequest().permitAll() // all other requests also open
                )
                .formLogin(login -> login.disable()) // Disable default login form
                .httpBasic(basic -> basic.disable()); // Disable HTTP Basic auth

        return http.build();
    }
}
