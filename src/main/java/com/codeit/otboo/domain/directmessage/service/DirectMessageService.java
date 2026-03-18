package com.codeit.otboo.domain.directmessage.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.util.UUID;

public interface DirectMessageService {

    CursorResponse<DirectMessageResponse> getDirectMessages(UUID userId, CursorRequest cursorRequest);
}
