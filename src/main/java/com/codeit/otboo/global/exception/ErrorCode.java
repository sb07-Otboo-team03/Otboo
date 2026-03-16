package com.codeit.otboo.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),

    // Clothes

    // Comment

    // DirectMessage

    // Feed

    // Follow

    // Like

    // Notification

    // Weather

    // Profile

    // JWT

    // Validation
    VALIDATION_ERROR("유효성 검사에 실패하였습니다."),

    // Internal
    INTERNAL_SERVER_ERROR("서버 에러가 발생했습니다.");



    private final String message;




}
