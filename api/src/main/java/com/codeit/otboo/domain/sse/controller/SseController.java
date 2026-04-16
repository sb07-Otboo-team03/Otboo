package com.codeit.otboo.domain.sse.controller;

import com.codeit.otboo.domain.sse.service.SseService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
        @AuthenticationPrincipal OtbooUserDetails userDetails,
        @RequestParam(value = "LastEventId", required = false) UUID lastEventId
    ) {
        UUID userId = userDetails.getUserResponse().id();

        return sseService.connect(userId, lastEventId);
    }
}
