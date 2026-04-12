package com.codeit.otboo.domain.clothes.management.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesUrlResponse;
import com.codeit.otboo.global.slice.dto.CursorResponse;

import java.util.UUID;

public interface ClothesService {
    ClothesResponse createClothes(
            BinaryContentCreateRequest imageRequest,
            ClothesCreateRequest clothesCreateRequest
    );

    ClothesResponse updateClothes(
            UUID clothesId,
            BinaryContentCreateRequest imageRequest,
            ClothesUpdateRequest request);

    void deleteClothes(UUID clothesId);

    CursorResponse<ClothesResponse> getClothesListByOwnerId(ClothesCursorPageRequest request);

    ClothesUrlResponse getClothesInfoByUrl(String url);
}
