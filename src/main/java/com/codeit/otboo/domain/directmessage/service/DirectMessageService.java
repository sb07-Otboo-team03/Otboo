package com.codeit.otboo.domain.directmessage.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageCursorResponse;
import com.codeit.otboo.global.slice.dto.PageResponse;
import java.util.UUID;

public interface DirectMessageService {

    PageResponse<DirectMessageCursorResponse> getDirectMessages(UUID userId, CursorRequest cursorRequest);
}
