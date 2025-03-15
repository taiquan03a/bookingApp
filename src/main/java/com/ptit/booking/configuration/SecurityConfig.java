package com.ptit.booking.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    //private final AuthenticationProvider authenticationProvider;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                //.authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                new AntPathRequestMatcher("/api/v1/auth/register/**"),
//                                new AntPathRequestMatcher("/api/v1/auth/register"),
//                                new AntPathRequestMatcher("/api/v1/user/otp"),
//                                new AntPathRequestMatcher("/api/v1/user/check_email"),
//                                new AntPathRequestMatcher("/api/v1/auth/login"),
//                                new AntPathRequestMatcher("/api/v1/auth/refresh"),
//                                new AntPathRequestMatcher("/api/v1/room/getByRank/**"),
//                                new AntPathRequestMatcher("/api/v1/rank","GET"),
//                                new AntPathRequestMatcher("/api/v1/service_hotel/category/get-service"),
//                                new AntPathRequestMatcher("/api/v1/zalopay/**")
//                        )
//                        .permitAll()
                        .anyRequest().permitAll()
                )
                .build();
    }
}
