package com.codeit.otboo.domain.directmessage.controller;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.service.DirectMessageService;
import com.codeit.otboo.global.slice.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    public ResponseEntity<PageResponse<DirectMessageResponse>> getDirectMessages(
        @RequestParam UUID userId,
        @ParameterObject @ModelAttribute @Valid CursorRequest cursorRequest
    ) {

        PageResponse<DirectMessageResponse> response =
                directMessageService.getDirectMessages(userId, cursorRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
