package com.codeit.otboo.domain.follow.exception.follow;

import com.codeit.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class DuplicateFollowException extends FollowException {

    public DuplicateFollowException() {
        super(ErrorCode.DUPLICATE_FOLLOW,
            HttpStatus.CONFLICT);
    }

    public DuplicateFollowException(UUID followId) {
        super(ErrorCode.DUPLICATE_FOLLOW,
            Map.of("followerId : ", followId.toString()),
            HttpStatus.CONFLICT);
    }

    public DuplicateFollowException(UUID followerId, UUID followeeId) {
        super(ErrorCode.DUPLICATE_FOLLOW,
            Map.of("followerId : ", followerId.toString(),
                   "followeeId : ", followeeId.toString()),
            HttpStatus.CONFLICT);
    }
}
