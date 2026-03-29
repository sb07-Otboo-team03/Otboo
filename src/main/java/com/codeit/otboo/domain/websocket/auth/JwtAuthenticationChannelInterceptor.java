package com.codeit.otboo.domain.websocket.auth;

import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.security.jwt.exception.JwtInvalidTokenTypeException;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final UserDetailsService userDetailsService;
    private final RedisRegistry jwtRegistry;
    private final JwtProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message,
            StompHeaderAccessor.class
        );

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String jwtToken = resolveToken(accessor).orElseThrow(JwtInvalidTokenTypeException::new);

            JWTClaimsSet accessTokenClaimsSet = tokenProvider.validateAccessToken(jwtToken);
            String username = accessTokenClaimsSet.getSubject();
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                accessor.setUser(authentication);

                log.debug("✅ Set authentication for user: {}", username);
            }
            else {
                log.debug("⚠️Invalid JWT token. username error");
                throw new JwtInvalidTokenTypeException();
            }
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
