package com.pricehawk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig
{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                .csrf(csrf -> csrf.disable()) // REST APIs ke liye CSRF off
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // /api/** publicly available
                        .anyRequest().permitAll()
                )
                .formLogin(login -> login.disable()) // Spring default login page disable
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
