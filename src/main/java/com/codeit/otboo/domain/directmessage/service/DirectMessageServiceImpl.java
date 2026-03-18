package com.codeit.otboo.domain.directmessage.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.otboo.global.slice.dto.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageServiceImpl implements DirectMessageService {
    private final DirectMessageRepository directMessageRepository;

    @Override
    public PageResponse<DirectMessageResponse> getDirectMessages(UUID userId, CursorRequest cursorRequest) {

        throw new UnsupportedOperationException("🚨for Test");
    }
}
