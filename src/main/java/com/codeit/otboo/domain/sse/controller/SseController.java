package com.codeit.otboo.domain.sse.controller;

import com.codeit.otboo.domain.sse.service.SseService;
import com.codeit.otboo.domain.sse.service.SseServiceImpl;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;

    @GetMapping("/api/sse")
    public SseEmitter connect(@RequestParam(required = false) UUID lastEventId) {

        SseEmitter sseEmitter = sseService.connect(lastEventId);
        return sseEmitter;
    }
}