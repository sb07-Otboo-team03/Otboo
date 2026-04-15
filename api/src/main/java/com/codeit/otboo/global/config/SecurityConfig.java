package com.codeit.otboo.global.config;

import com.codeit.otboo.global.filter.RequestMdcFilter;
import com.codeit.otboo.global.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.codeit.otboo.global.oauth.OidcUserServiceImpl;
import com.codeit.otboo.global.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.codeit.otboo.global.oauth.handler.OAuth2AuthenticationSuccessHandler;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.ForwardedHeaderFilter;

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
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ObjectMapper objectMapper,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           RequestMdcFilter mdcFilter,
                                           Http401AuthenticationEntryPoint authenticationEntryPoint,
                                           Http403ForbiddenAccessDeniedHandler accessDeniedHandler,
                                           OidcUserServiceImpl oidcUserServiceImpl,
                                           OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                                           OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                                           HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository
    ) throws Exception {
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
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization") // 사용자가 로그인을 시작하는 진입점
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(redirection -> redirection // 인증을 마친 후 리다이렉트될 API
                                .baseUri("/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserServiceImpl)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        .ignoringRequestMatchers(
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**"
                        )
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
