package com.codeit.otboo.domain.directmessage.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.directmessage.mapper.DirectMessageMapper;
import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.websocket.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.websocket.event.DirectMessageCreatedEvent;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageServiceImpl implements DirectMessageService {
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageMapper directMessageMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    private LocalDateTime toLocalDateTime(String cursor) {
        return (cursor == null) ? null :LocalDateTime.parse(cursor);
    }

    @Override
    @Transactional
    public DirectMessageResponse create(DirectMessageCreateRequest request) {

        User receiver = userRepository.findById(request.receiverId())
            .orElseThrow(() -> new UserNotFoundException(request.receiverId()));

        User sender = userRepository.findById(request.senderId())
            .orElseThrow(() -> new UserNotFoundException(request.senderId()));

        DirectMessage directMessage = new DirectMessage(sender, receiver, request.content());
        DirectMessage saveDirectMessage = directMessageRepository.save(directMessage);
        DirectMessageResponse response = directMessageMapper.toDto(saveDirectMessage);

        eventPublisher.publishEvent(
            new DirectMessageCreatedEvent(
                response, response.createdAt()
            )
        );

        return response;
    }

    @Override
    public CursorResponse<DirectMessageResponse> getDirectMessages(
        UUID userId,
        CursorRequest cursorRequest
    ) {
        LocalDateTime cursor = toLocalDateTime(cursorRequest.cursor());
        Pageable pageable = PageRequest.of(0, cursorRequest.limit() + 1);

        List<DirectMessageDto> results = directMessageRepository.findDirectMessageDtos(
            userId,
            cursor,
            cursorRequest.idAfter(),
            pageable
        );

        boolean hasNext = results.size() > cursorRequest.limit();

        List<DirectMessageDto> page = hasNext
            ? results.subList(0, cursorRequest.limit())
            : results;

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (!page.isEmpty()) {
            DirectMessageDto last = page.get(page.size() - 1);
            nextCursor = last.createdAt().toString();
            nextIdAfter = last.id();
        }

        List<DirectMessageResponse> content = page.stream()
            .map(directMessageMapper::toDto)
            .toList();

        return CursorResponse.fromList(
            content,
            nextCursor,
            nextIdAfter,
            hasNext,
            "createdAt",
            SortDirection.DESCENDING
        );
    }
}

