package com.codeit.otboo.domain.clothes.management.controller;

import com.codeit.otboo.domain.binarycontent.mapper.BinaryContentMapper;

import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.service.ClothesService;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothesController {
    private final ClothesService clothesService;
    private final BinaryContentMapper binaryContentMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothesResponse> upload(
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart ClothesCreateRequest request
    ){
        ClothesResponse response = clothesService.createClothes(
            binaryContentMapper.toRequestDto(image),
            request
        );
        return ResponseEntity
                .created(URI.create("/api/clothes/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> deleteClothes(@PathVariable UUID clothesId){
        clothesService.deleteClothes(clothesId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{clothesId}")
    public ResponseEntity<ClothesResponse> updateClothes(
            @PathVariable UUID clothesId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart ClothesUpdateRequest request){
        ClothesResponse response = clothesService.updateClothes(
                clothesId,
                binaryContentMapper.toRequestDto(image),
                request
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CursorResponse<ClothesResponse>> getAllClothes(ClothesCursorPageRequest request){
        return ResponseEntity.ok(clothesService.getMyClotheList(request));
    }
}