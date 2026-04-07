package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.user.entity.User;

public class DirectMessageSseEvent extends BaseSseEvent {
    public DirectMessageSseEvent(String title, String content, User user) {
        super(title, content, user);
    }
}
