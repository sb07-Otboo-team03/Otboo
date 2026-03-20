package com.codeit.otboo.global.security.jwt.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JwtErrorCode {
    INVALID_SIGNATURE("JWT 서명 검증 실패"),
    EXPIRED_TOKEN("JWT 토큰 만료"),
    INVALID_ISSUER("JWT issuer 불일치"),
    INVALID_TOKEN_TYPE("JWT 타입 불일치"),
    PARSE_ERROR("JWT 파싱 실패");

    private final String message;
}
