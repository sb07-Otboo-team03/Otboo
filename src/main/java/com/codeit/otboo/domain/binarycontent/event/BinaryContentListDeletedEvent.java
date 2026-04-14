package com.codeit.otboo.domain.binarycontent.event;

import java.util.List;
import java.util.UUID;

public record BinaryContentListDeletedEvent(
        List<UUID> binaryContentIdList
) {
}
