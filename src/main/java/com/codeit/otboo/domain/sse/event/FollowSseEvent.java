package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.user.entity.User;
import lombok.Getter;

@Getter
public class FollowSseEvent extends BaseSseEvent {
    public FollowSseEvent(String title, String content, User user) {
        super(title, content, user);
    }
}
