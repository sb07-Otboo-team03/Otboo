package com.codeit.otboo.domain.follow.exception.follow;

import com.codeit.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class FollowNotFoundException extends FollowException {

    public FollowNotFoundException() {
        super(ErrorCode.FOLLOW_NOT_FOUND,
            HttpStatus.NOT_FOUND);
    }


    public FollowNotFoundException(UUID followId) {
        super(ErrorCode.FOLLOW_NOT_FOUND,
            Map.of("followId : ", followId.toString()),
            HttpStatus.NOT_FOUND);
    }

    public FollowNotFoundException(UUID followerId, UUID followeeId) {
        super(ErrorCode.FOLLOW_NOT_FOUND,
            Map.of("followerId : ", followerId.toString(),
                "followeeId : ", followeeId.toString()),
            HttpStatus.NOT_FOUND);
    }
}
