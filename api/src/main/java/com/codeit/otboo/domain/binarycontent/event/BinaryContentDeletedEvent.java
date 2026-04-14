package com.codeit.otboo.domain.binarycontent.event;

import java.util.UUID;

public record BinaryContentDeletedEvent(
        UUID binaryContentId
) {
}
