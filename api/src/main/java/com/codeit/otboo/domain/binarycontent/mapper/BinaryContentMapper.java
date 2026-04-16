package com.codeit.otboo.domain.binarycontent.mapper;

import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentInfoResponse;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import org.springframework.stereotype.Component;

@Component
public class BinaryContentMapper {
    public BinaryContentInfoResponse toResponseDto(BinaryContent binaryContent) {
        if (binaryContent == null) {
            return null;
        } else {
            return new BinaryContentInfoResponse(
                    binaryContent.getId(),
                    binaryContent.getName(),
                    binaryContent.getType(),
                    binaryContent.getSize()
            );
        }
    }
}
