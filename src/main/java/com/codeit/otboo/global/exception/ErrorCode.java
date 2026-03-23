package com.codeit.otboo.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),

    // Clothes

    // Comment

    // Feed
    FEED_NOT_FOUND("해당 피드를 찾을 수 없습니다."),

    // Follow
    DUPLICATE_FOLLOW("이미 존재하는 팔로우입니다."),

    // Like
    LIKE_NOT_FOUND("좋아요를 찾을 수 없습니다."),
    LIKE_ALREADY_EXISTS("좋아요가 이미 존재합니다."),

    // Notification
    DUPLICATE_NOTIFICATION("이미 존재하는 알림입니다."),

    // Weather

    // Profile

    // BinaryContent
    BINARY_CONTENT_NOT_FOUNT("해당 UUID를 가진 바이너리 컨텐츠가 존재하지 않습니다."),
    FILE_CONVERSION_FAILED("파일 변환 중 오류가 발생했습니다."),
    INVALID_FILE_REQUEST("잘못된 파일 요청입니다."),

    // JWT

    // Validation
    VALIDATION_ERROR("유효성 검사에 실패하였습니다."),

    // Internal
    INTERNAL_SERVER_ERROR("서버 에러가 발생했습니다.");



    private final String message;
}
