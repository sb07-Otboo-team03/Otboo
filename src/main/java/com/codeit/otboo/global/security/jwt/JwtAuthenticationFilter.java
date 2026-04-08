package com.codeit.otboo.global.security.jwt;

import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.exception.JwtException;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisRegistry redisRegistry;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorizationHeader.substring(7);

        try {
            JWTClaimsSet claims = jwtProvider.validateAccessToken(accessToken);

            UUID userId = UUID.fromString(claims.getSubject());
            String sessionId = jwtProvider.getSessionId(accessToken);
            String email = jwtProvider.getEmail(accessToken);

            if (!redisRegistry.isValidSession(userId, sessionId)) {
                throw new BadCredentialsException("유효하지 않은 세션입니다.");
            }
            OtbooUserDetails userDetails = (OtbooUserDetails) userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );


            SecurityContextHolder.getContext().setAuthentication(authentication);
            MDC.put("userId", String.valueOf(userDetails.getUserResponse().id()));

        } catch (JwtException | BadCredentialsException e) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("유효하지 않은 access token입니다.", e);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

}
