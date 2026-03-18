package com.codeit.otboo.domain.binarycontent.dto.response;

import java.util.UUID;

public record BinaryContentInfoRes(
        UUID id,
        String name,
        String type,
        long size
) {

}
