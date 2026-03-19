package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
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
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    private LocalDateTime decodeCursor(String cursor) {
        if (cursor == null) return null;
        return LocalDateTime.parse(cursor);
    }

    @Override
    public CursorResponse<NotificationResponse> getNotifications(CursorRequest cursorRequest) {
        LocalDateTime cursor = decodeCursor(cursorRequest.cursor());

        Pageable pageable = PageRequest.of(0, cursorRequest.limit() + 1);

        List<NotificationResponse> notificationList = notificationRepository.findAll(
                cursor,
                cursorRequest.idAfter(),
                pageable
            )
            .stream()
            .map(notificationMapper::toDto)
            .toList();

        boolean hasNext = notificationList.size() > cursorRequest.limit();

        if (hasNext) {
            notificationList = notificationList.subList(0, cursorRequest.limit());
        }

        LocalDateTime nextCursor = null;
        UUID nextIDAfter = null;

        if (!notificationList.isEmpty()) {
            NotificationResponse last = notificationList.get(notificationList.size() - 1);
            nextCursor = last.createdAt();
            nextIDAfter = last.id();
        }

        return CursorResponse.fromList(
            notificationList,
            nextCursor != null ? nextCursor.toString() : null,
            nextIDAfter,
            hasNext,
            "createdAt",
            SortDirection.DESCENDING
        );
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("🚨"));

        notificationRepository.delete(notification);
    }
}
