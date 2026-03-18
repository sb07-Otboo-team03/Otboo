package com.codeit.otboo.domain.directmessage.dto;

import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DirectMessageResponse(
        UUID id,
        LocalDateTime createdAt,
        UserSummaryResponse sender,
        UserSummaryResponse receiver,
        String content
) {
    public static DirectMessageResponse toDto(DirectMessage directMessage, UserSummaryResponse sender, UserSummaryResponse receiver) {

        return new DirectMessageResponse(
            directMessage.getId(),
            directMessage.getCreatedAt(),
            sender,
            receiver,
            directMessage.getContent());
    }

    public static DirectMessageResponse from(DirectMessageDto directMessageDto) {
        String senderProfileImageUrl = "yml 명시 경로" + "/" + directMessageDto.senderProfileImageId(); //??
        UserSummaryResponse senderSummary = new UserSummaryResponse(directMessageDto.senderId(), directMessageDto.senderName(), senderProfileImageUrl);

        String receiverProfileImageUrl = "yml 명시 경로" + "/" + directMessageDto.receiverProfileImageId(); //??
        UserSummaryResponse receiverSummary = new UserSummaryResponse(directMessageDto.receiverId(), directMessageDto.receiverName(), receiverProfileImageUrl);

        return new DirectMessageResponse(
            directMessageDto.id(),
            directMessageDto.createdAt(),
            senderSummary,
            receiverSummary,
            directMessageDto.content());
    }
}
