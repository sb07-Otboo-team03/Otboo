package com.codeit.otboo.global.security;

import com.codeit.otboo.global.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class Http401AuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 세팅
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse error = ErrorResponse.builder()
                .exceptionName(authException.getClass().getSimpleName())
                .message("자격 증명에 실패하였습니다.")
                .details(null)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
