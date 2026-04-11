package com.codeit.otboo.domain.notification.controller;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.notification.doc.NotificationApi;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<CursorResponse<NotificationResponse>> getNotifications(
        @AuthenticationPrincipal OtbooUserDetails authPrincipal,
        @ParameterObject @ModelAttribute @Valid CursorRequest cursorRequest
    ) {
        UUID id = authPrincipal.getUserResponse().id();

        CursorResponse<NotificationResponse> response =
            notificationService.getNotifications(id, cursorRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    // 알림 읽음 처리
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
        @AuthenticationPrincipal OtbooUserDetails authPrincipal,
        @PathVariable UUID notificationId
    ) {

        UUID id = authPrincipal.getUserResponse().id();
        notificationService.deleteNotification(id, notificationId);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
