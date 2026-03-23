package com.codeit.otboo.domain.like.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class LikeNotFoundException extends LikeException{
    public LikeNotFoundException(UUID feedId, UUID userId) {
        super(ErrorCode.LIKE_NOT_FOUND,
                Map.of("feedId", feedId.toString(), "userId", userId.toString()),
                HttpStatus.NOT_FOUND);
    }

    public LikeNotFoundException() {
        super(ErrorCode.LIKE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
