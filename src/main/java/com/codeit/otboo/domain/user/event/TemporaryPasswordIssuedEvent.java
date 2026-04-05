package com.codeit.otboo.domain.user.event;

import java.time.LocalDateTime;

public record TemporaryPasswordIssuedEvent(
        String email,
        String temporaryPassword,
        LocalDateTime expiresAt)  {
}
