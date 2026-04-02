package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;

import java.util.UUID;

public interface BinaryContentService {
    BinaryContent upload(BinaryContentCreateRequest request);

    void delete(UUID binaryContentId);
}
