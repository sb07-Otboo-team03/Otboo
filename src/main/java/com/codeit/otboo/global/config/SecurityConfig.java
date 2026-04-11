package com.codeit.otboo.global.config;

import com.codeit.otboo.global.filter.RequestMdcFilter;
import com.codeit.otboo.global.security.Http401AuthenticationEntryPoint;
import com.codeit.otboo.global.security.Http403ForbiddenAccessDeniedHandler;
import com.codeit.otboo.global.security.SpaCsrfTokenRequestHandler;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public Http401AuthenticationEntryPoint http401AuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new Http401AuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public Http403ForbiddenAccessDeniedHandler http403ForbiddenAccessDeniedHandler(ObjectMapper objectMapper) {
        return new Http403ForbiddenAccessDeniedHandler(objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ObjectMapper objectMapper,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           RequestMdcFilter mdcFilter,
                                           Http401AuthenticationEntryPoint authenticationEntryPoint,
                                           Http403ForbiddenAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/auth/sign-in", "/api/auth/sign-out",
                                "/api/auth/reset-password", "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/auth/csrf-token").permitAll()
                        .requestMatchers("/actuator/**", "/actuator/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/", "/assets/**", "index.html", "/css/**", "/js/**", "/favicon.ico", "/*.svg").permitAll()
                        .requestMatchers("/error").permitAll()

                        // ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/lock").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/clothes/attribute-defs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/clothes/attribute-defs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/clothes/attribute-defs/**").hasRole("ADMIN")

                        // AUTHENTICATED
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(mdcFilter, SecurityContextHolderFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
