package com.codeit.otboo.domain.directmessage.mapper;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {
    private final UserMapper userMapper;

    public DirectMessageResponse toDto(DirectMessage directMessage) {

        return new DirectMessageResponse(
            directMessage.getId(),
            directMessage.getCreatedAt(),
            userMapper.toSummaryDto(
                directMessage.getSender().getId(),
                directMessage.getSender().getProfile().getName(),
                directMessage.getSender().getProfile().getId()),
            userMapper.toSummaryDto(
                directMessage.getReceiver().getId(),
                directMessage.getReceiver().getProfile().getName(),
                directMessage.getReceiver().getProfile().getId()),
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
