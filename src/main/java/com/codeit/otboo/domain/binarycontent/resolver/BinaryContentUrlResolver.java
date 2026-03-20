package com.codeit.otboo.domain.binarycontent.resolver;

import java.util.UUID;

public interface BinaryContentUrlResolver {
    String resolve(UUID binaryContentId);
}
