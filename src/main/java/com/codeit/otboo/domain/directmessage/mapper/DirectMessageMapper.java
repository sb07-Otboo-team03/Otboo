package com.codeit.otboo.domain.directmessage.mapper;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {
    private final UserMapper userMapper;

    public DirectMessageResponse toDto(DirectMessage directMessage) {
        UUID senderBinaryContentId = null;
        if(directMessage.getSender().getProfile().getBinaryContent() != null) {
            senderBinaryContentId = directMessage.getSender().getProfile().getBinaryContent().getId();
        }
        UUID receiverBinaryContentId = null;
        if(directMessage.getSender().getProfile().getBinaryContent() != null) {
            receiverBinaryContentId = directMessage.getSender().getProfile().getBinaryContent().getId();
        }

        return new DirectMessageResponse(
            directMessage.getId(),
            directMessage.getCreatedAt(),
            userMapper.toSummaryDto(
                directMessage.getSender().getId(),
                directMessage.getSender().getProfile().getName(),
                    senderBinaryContentId),
            userMapper.toSummaryDto(
                directMessage.getReceiver().getId(),
                directMessage.getReceiver().getProfile().getName(),
                    receiverBinaryContentId),
            directMessage.getContent());
    }

    public DirectMessageResponse toDto(DirectMessageDto directMessageDto) {
        return new DirectMessageResponse(
            directMessageDto.id(),
            directMessageDto.createdAt(),
            userMapper.toSummaryDto(
                directMessageDto.senderId(),
                directMessageDto.senderName(),
                directMessageDto.senderProfileImageId()),
            userMapper.toSummaryDto(
                directMessageDto.receiverId(),
                directMessageDto.receiverName(),
                directMessageDto.receiverProfileImageId()),
            directMessageDto.content());
    }
}
