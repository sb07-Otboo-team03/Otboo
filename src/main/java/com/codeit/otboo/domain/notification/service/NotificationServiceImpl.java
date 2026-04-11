package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.notification.dto.NotificationCreateCommand;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.exception.notification.NotificationNotFoundException;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    private LocalDateTime toLocalDateTime(String cursor) {
        return (cursor == null) ? null :LocalDateTime.parse(cursor);
    }

    @Override
    public CursorResponse<NotificationResponse> getNotifications(
        UUID authPrincipalId, CursorRequest cursorRequest) {

        LocalDateTime cursor = toLocalDateTime(cursorRequest.cursor());
        Pageable pageable = PageRequest.of(0, cursorRequest.limit() + 1);

        List<NotificationDto> results = notificationRepository.findAllByReceiverId(
            authPrincipalId,
            cursor,
            cursorRequest.idAfter(),
            pageable
        );

        boolean hasNext = results.size() > cursorRequest.limit();

        List<NotificationDto> page = hasNext
            ? results.subList(0, cursorRequest.limit())
            : results;

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (!page.isEmpty()) {
            NotificationDto last = page.get(page.size() - 1);
            nextCursor = last.createdAt().toString();
            nextIdAfter = last.id();
        }

        List<NotificationResponse> content = page.stream()
            .map(notificationMapper::toDto)
            .toList();

        return CursorResponse.fromList(
            content,
            nextCursor,
            nextIdAfter,
            hasNext,
            content.size(),
            "createdAt",
            SortDirection.DESCENDING
        );
    }

    @Override
    @Transactional
    public void deleteNotification(UUID authPrincipalId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, authPrincipalId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public Notification create(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification create(NotificationCreateCommand command) {
        User receiver = userRepository.findById(command.receiverId())
                .orElseThrow(() -> new UserNotFoundException(command.receiverId()));

        Notification notification = new Notification(
                command.title(),
                command.content(),
                command.level(),
                receiver
        );

        return notificationRepository.save(notification);
    }
}
