package com.codeit.otboo.domain.websocket.auth;

import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.security.jwt.exception.JwtException;
import com.codeit.otboo.global.security.jwt.exception.JwtInvalidTokenTypeException;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final RedisRegistry redisRegistry;
    private final UserDetailsService userDetailsService;
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message,
            StompHeaderAccessor.class
        );

        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                String accessToken = resolveToken(accessor).orElseThrow(JwtInvalidTokenTypeException::new);

                String email = jwtProvider.getEmail(accessToken);
                String sessionId = jwtProvider.getSessionId(accessToken);
                JWTClaimsSet claims = jwtProvider.validateAccessToken(accessToken);

                UUID userId = UUID.fromString(claims.getSubject());

                if (!redisRegistry.isValidSession(userId, sessionId)) {
                    throw new BadCredentialsException("유효하지 않은 세션입니다.");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                accessor.setUser(authentication);

                log.debug("✅ Set authentication. email = {}", email);
            }
        }
        catch (JwtException | BadCredentialsException e) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("유효하지 않은 access token입니다.", e);
        }
        catch (NullPointerException e) {
            SecurityContextHolder.clearContext();

            log.debug("✅ StompHeaderAccessor null point exception err");
            throw new IllegalArgumentException("✅ StompHeaderAccessor null point exception", e);
        }
        catch (Exception e) {
            SecurityContextHolder.clearContext();

            log.debug("✅ 그 밖의 errerr ", e);
            throw new IllegalArgumentException("✅ Exception ", e);
        }
        return message;
    }

    private Optional<String> resolveToken(StompHeaderAccessor accessor) {
        String prefix = "Bearer ";
        return Optional.ofNullable(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION))
            .map(value -> {
                if (value.startsWith(prefix)) {
                    return value.substring(prefix.length());
                } else {
                    return null;
                }
            });
    }
}
