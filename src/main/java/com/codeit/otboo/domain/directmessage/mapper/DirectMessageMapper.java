package com.codeit.otboo.domain.directmessage.mapper;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class DirectMessageMapper {

    public DirectMessageResponse toDto(DirectMessage directMessage, UserSummaryResponse sender, UserSummaryResponse receiver) {

        return new DirectMessageResponse(
            directMessage.getId(),
            directMessage.getCreatedAt(),
            sender,
            receiver,
            directMessage.getContent());
    }

    public DirectMessageResponse from(DirectMessageDto directMessageDto) {

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
