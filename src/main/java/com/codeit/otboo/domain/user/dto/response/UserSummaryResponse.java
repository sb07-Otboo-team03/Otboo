package com.codeit.otboo.domain.user.dto.response;

import com.codeit.otboo.domain.user.entity.User;
import java.util.UUID;

public record UserSummaryResponse(
   UUID userId,
   String name,
   String profileImageUrl
) {
    public static UserSummaryResponse from(User user) {

        String senderProfileImageUrl = "yml 명시 경로" + "/" + user.getProfile().getBinaryContent().getId().toString(); //??
        UserSummaryResponse senderSummary = new UserSummaryResponse(user.getId(), user.getProfile().getName(), senderProfileImageUrl);

        return new UserSummaryResponse(user.getId(), user.getProfile().getName(), senderProfileImageUrl);
    }
}
