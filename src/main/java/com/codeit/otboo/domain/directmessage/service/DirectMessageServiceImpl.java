package com.codeit.otboo.domain.directmessage.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageServiceImpl implements DirectMessageService {
    private final DirectMessageRepository directMessageRepository;

    private LocalDateTime decodeCursor(String cursor) {
        if (cursor == null) return null;
        return LocalDateTime.parse(cursor);
    }

    public CursorResponse<DirectMessageResponse> getDirectMessages(UUID userId, CursorRequest cursorRequest){
        LocalDateTime cursor = decodeCursor(cursorRequest.cursor());

        Pageable pageable = PageRequest.of(0, cursorRequest.limit() + 1);

        List<DirectMessageResponse> list = directMessageRepository.findDirectMessageDtos(
                userId,
                cursor,
                cursorRequest.idAfter(),
                pageable
            )
            .stream()
            .map(DirectMessageResponse::from)
            .toList();

        boolean hasNext = list.size() > cursorRequest.limit();

        if (hasNext) {
            list = list.subList(0, cursorRequest.limit());
        }

        LocalDateTime nextCursor = null;
        UUID nextIdAfter = null;

        if (!list.isEmpty()) {
            DirectMessageResponse last = list.get(list.size() - 1);
            nextCursor = last.createdAt();
            nextIdAfter = last.id();
        }

        return CursorResponse.fromList(
            list,
            nextCursor != null ? nextCursor.toString() : null,
            nextIdAfter,
            hasNext,
            "createdAt",
            SortDirection.DESCENDING
        );
    }
}

