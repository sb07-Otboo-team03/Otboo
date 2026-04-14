package com.codeit.otboo.domain.feed.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class FeedNotFoundException extends FeedException {

    public FeedNotFoundException(UUID feedId) {
        super(ErrorCode.FEED_NOT_FOUND,
                Map.of("feedId", feedId.toString()),
                HttpStatus.NOT_FOUND);
    }

    public FeedNotFoundException() {
        super(ErrorCode.FEED_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
