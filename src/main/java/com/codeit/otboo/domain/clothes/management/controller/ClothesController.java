package com.codeit.otboo.domain.clothes.management.controller;

import com.codeit.otboo.domain.binarycontent.mapper.BinaryContentMapper;

import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.service.ClothesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

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
}