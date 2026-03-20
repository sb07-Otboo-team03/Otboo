package com.codeit.otboo.domain.clothes.management.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;

public interface ClothesService {
    ClothesResponse createClothes(
            BinaryContentCreateRequest imageRequest,
            ClothesCreateRequest clothesCreateRequest
    );
}
