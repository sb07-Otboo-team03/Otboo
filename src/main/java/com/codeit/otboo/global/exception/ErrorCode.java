package com.codeit.otboo.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.");

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
    private final String message;


}
