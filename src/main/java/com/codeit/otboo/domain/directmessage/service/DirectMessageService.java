package com.codeit.otboo.domain.directmessage.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.global.slice.dto.PageResponse;
import java.util.UUID;

public interface DirectMessageService {

    PageResponse<DirectMessageResponse> getDirectMessages(UUID userId, CursorRequest cursorRequest);
}
