package com.codeit.otboo.domain.like.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class LikeAlreadyExistsException extends LikeException{
    public LikeAlreadyExistsException(UUID feedId, UUID userId) {

        super(ErrorCode.LIKE_ALREADY_EXISTS,
                Map.of("feedId", feedId.toString(), "userId", userId.toString()),
                HttpStatus.CONFLICT);
    }

    public LikeAlreadyExistsException() {
        super(ErrorCode.LIKE_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }
}
