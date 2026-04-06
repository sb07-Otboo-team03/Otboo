package com.codeit.otboo.domain.binarycontent.event;

import java.util.UUID;

public record BinaryContentCreatedEvent (
        UUID binaryContentId,
        byte[] bytes,
        String contentType
) {}