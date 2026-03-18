package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateReq;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import org.springframework.core.io.Resource;

import java.util.UUID;

public interface BinaryContentService {
    BinaryContent upload(BinaryContentCreateReq req);

    Resource download(UUID binaryContentId);

    BinaryContent getInfo(UUID binaryContentId);

    void delete(UUID binaryContentId);
}
