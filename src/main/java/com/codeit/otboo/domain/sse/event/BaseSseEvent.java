package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseSseEvent {
    String title;
    String content;
    User user;
}

