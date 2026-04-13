package com.codeit.otboo.domain.binarycontent.dto.request;

public record BinaryContentCreateRequest(
        byte[] data,
        String name,
        String type,
        Long size
) {

}
