package com.codeit.otboo.domain.profile.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ProfileNotFoundException extends ProfileException {
    public ProfileNotFoundException(UUID profileId) {
        super(ErrorCode.PROFILE_NOT_FOUND,
                Map.of("profileId", profileId.toString()),
                HttpStatus.NOT_FOUND);
    }

    public ProfileNotFoundException() {
        super(ErrorCode.PROFILE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
