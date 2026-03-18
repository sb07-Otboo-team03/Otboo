package com.codeit.otboo.domain.binarycontent.dto.request;

public record BinaryContentCreateReq(
        byte[] data,
        String name,
        String type,
        long size
) {

}
