package com.codeit.otboo.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


@Component
public class RequestMdcFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId"; // 요청 식별 고유 ID
    public static final String METHOD = "method"; // HTTP METHOD
    public static final String URI = "uri"; // 엔드포인트
    public static final String USER_ID = "userId"; // 식별자 (로그인 없으면 없음)

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        clearMdc();

        try {
            MDC.put(TRACE_ID, UUID.randomUUID().toString());
            MDC.put(METHOD, request.getMethod());
            MDC.put(URI, request.getRequestURI());

            filterChain.doFilter(request, response);
        } finally {
            clearMdc();
        }
    }

    private void clearMdc() {
        MDC.remove(TRACE_ID);
        MDC.remove(METHOD);
        MDC.remove(URI);
        MDC.remove(USER_ID);
    }
}
