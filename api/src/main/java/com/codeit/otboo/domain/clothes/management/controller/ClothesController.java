package com.codeit.otboo.domain.clothes.management.controller;

import com.codeit.otboo.domain.clothes.management.controller.docs.ClothesControllerDocs;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUrlRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesUrlResponse;
import com.codeit.otboo.domain.clothes.management.service.ClothesService;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothesController implements ClothesControllerDocs {
    private final ClothesService clothesService;

    @PostMapping
    public ResponseEntity<ClothesResponse> saveClothes(
            @Valid @RequestBody ClothesCreateRequest request
    ){
        ClothesResponse response = clothesService.createClothes(request);
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
            @Valid @RequestBody ClothesUpdateRequest request){
        ClothesResponse response = clothesService.updateClothes(clothesId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CursorResponse<ClothesResponse>> getAllClothes(@Valid ClothesCursorPageRequest request){
        return ResponseEntity.ok(clothesService.getClothesListByOwnerId(request));
    }

    @GetMapping("/extractions")
    public ResponseEntity<ClothesUrlResponse> getExtractions(@Valid ClothesUrlRequest request){
        return ResponseEntity.ok(clothesService.getClothesInfoByUrl(request.url()));
    }
}