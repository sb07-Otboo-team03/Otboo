package com.codeit.otboo.global.security.jwt;

import com.codeit.otboo.global.security.jwt.exception.JwtErrorCode;
import com.codeit.otboo.global.security.jwt.exception.JwtException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtProvider {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    private static final String TOKEN_TYPE = "token_type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";
    private final JwtProperties jwtProperties;

    private final MACSigner signer;
    private final MACVerifier verifier;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        SecretKey secretKey = getSecretKey();
        try {
            this.signer = new MACSigner(secretKey);
            this.verifier = new MACVerifier(secretKey);
        } catch (JOSEException e) {
            log.error("JWT 컴포넌트 초기화 실패 - Secret길이 또는 알고리즘 설정 문제", e);
            throw new IllegalStateException("JWT 컴포넌트 초기화 실패 - Secret길이 또는 알고리즘 설정 문제");
        }
    }

    private SecretKey getSecretKey() {
        byte[] secretBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            log.error("JWT secret은 최소 32자 이상이어야 합니다.");
            throw new IllegalArgumentException("JWT secret은 최소 32자 이상이어야 합니다.");
        }

        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    public String generateAccessToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(jwtProperties.issuer())
                .subject(userId.toString())
                .issueTime(now)
                .expirationTime(expiryDate)
                .claim(TOKEN_TYPE, ACCESS)
                .build();

        return sign(claimsSet);
    }

    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

        // PayLoad clams Set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(jwtProperties.issuer())
                .subject(userId.toString())
                .issueTime(now)
                .expirationTime(expiryDate)
                .claim(TOKEN_TYPE, REFRESH)
                .build();

        return sign(claimsSet);
    }

    private String sign(JWTClaimsSet claims) {
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );
        try {
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("JWT 서명 연산 실패", e);
        }
    }

    private JWTClaimsSet verifyAndGetClaims(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(verifier)) {
                throw new JwtException(JwtErrorCode.INVALID_SIGNATURE);
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            if (claims.getExpirationTime() == null
                    || new Date().after(claims.getExpirationTime())) {
                throw new JwtException(JwtErrorCode.EXPIRED_TOKEN);
            }

            if (!jwtProperties.issuer().equals(claims.getIssuer())) {
                throw new JwtException(JwtErrorCode.INVALID_ISSUER);
            }

            return claims;

        } catch (ParseException e) {
            throw new JwtException(JwtErrorCode.PARSE_ERROR, e);
        } catch (JOSEException e) {
            log.error("JWT 서명 검증 내부 라이브러리 오류", e);
            throw new JwtException(JwtErrorCode.INVALID_SIGNATURE);
        }
    }

    public JWTClaimsSet validateAccessToken(String token) {
        JWTClaimsSet claims = verifyAndGetClaims(token);

        if (!ACCESS.equals(claims.getClaim(TOKEN_TYPE))) {
            throw new JwtException(JwtErrorCode.INVALID_TOKEN_TYPE);
        }

        return claims;
    }

    public JWTClaimsSet validateRefreshToken(String token) {
        JWTClaimsSet claims = verifyAndGetClaims(token);

        if (!REFRESH.equals(claims.getClaim(TOKEN_TYPE))) {
            throw new JwtException(JwtErrorCode.INVALID_TOKEN_TYPE);
        }

        return claims;
    }

    public boolean isValidAccessToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            JWTClaimsSet claims = verifyAndGetClaims(token);
            return ACCESS.equals(claims.getClaim(TOKEN_TYPE));

        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isValidRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            JWTClaimsSet claims = verifyAndGetClaims(token);

            return REFRESH.equals(claims.getClaim(TOKEN_TYPE));

        } catch (JwtException e) {
            return false;
        }
    }

}
