package com.codeit.otboo.domain.binarycontent.mapper;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentInfoResponse;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.exception.FileConversionFail;
import com.codeit.otboo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class BinaryContentMapper {

    public static BinaryContentCreateRequest toRequestDto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return new BinaryContentCreateRequest(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new FileConversionFail(ErrorCode.FILE_CONVERSION_FAILED);
        }
    }

    public static BinaryContentInfoResponse toResponseDto(BinaryContent binaryContent) {
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
