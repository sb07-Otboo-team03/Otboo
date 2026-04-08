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

/**
 * 요청 시작 시 세팅
 * 요청 종료 시 정리
 */
@Component
public class RequestMdcFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    public static final String METHOD = "method";
    public static final String URI = "uri";
    public static final String USER_ID = "userId";

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
