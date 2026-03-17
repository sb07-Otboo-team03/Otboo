package com.codeit.otboo.domain.directmessage.controller;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageCursorResponse;
import com.codeit.otboo.domain.directmessage.service.DirectMessageService;
import com.codeit.otboo.global.slice.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    // DirectMessage 목록 조회
    @GetMapping
    public ResponseEntity<PageResponse<DirectMessageCursorResponse>> getDirectMessages(
        @RequestParam UUID userId,
        @Valid CursorRequest cursorRequest
    ) {

        PageResponse<DirectMessageCursorResponse> response =
                directMessageService.getDirectMessages(userId, cursorRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
