package com.codeit.otboo.global.security.jwt.registry.event;

import java.util.UUID;

public record SessionDeletedRequestEvent(
        UUID userId,
        SessionInvalidationReason reason
) {
}
