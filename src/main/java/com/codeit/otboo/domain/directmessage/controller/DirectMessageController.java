package com.codeit.otboo.domain.directmessage.controller;

import com.codeit.otboo.domain.directmessage.controller.docs.DirectMessageControllerDocs;
import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.service.DirectMessageService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageController implements DirectMessageControllerDocs {

    private final DirectMessageService directMessageService;

    // DirectMessage 목록 조회
    @GetMapping
    public ResponseEntity<CursorResponse<DirectMessageResponse>> getDirectMessages(
        @AuthenticationPrincipal OtbooUserDetails userDetails,
        @RequestParam UUID userId,
        @ParameterObject @ModelAttribute @Valid CursorRequest cursorRequest
    ) {
        UUID myId = userDetails.getUserResponse().id();
        CursorResponse<DirectMessageResponse> response =
                directMessageService.getDirectMessages(myId, userId, cursorRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
