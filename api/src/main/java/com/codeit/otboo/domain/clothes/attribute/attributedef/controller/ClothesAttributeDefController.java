package com.codeit.otboo.domain.clothes.attribute.attributedef.controller;

import com.codeit.otboo.domain.clothes.attribute.attributedef.controller.docs.ClothesAttributeDefControllerDocs;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.service.ClothesAttributeDefService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class ClothesAttributeDefController implements ClothesAttributeDefControllerDocs {

    private final ClothesAttributeDefService clothesAttributeDefService;

    /**
     * GET : /api/clothes/attribute-defs
     * 의상 속성 정의 목록
     */
    @GetMapping
    public ResponseEntity<List<ClothesAttributeDefResponse>> getAllClothesAttributeDefs(
            @ModelAttribute ClothesAttributeSearchRequest searchRequest
    ) {
        List<ClothesAttributeDefResponse> allAttributeDef
                = clothesAttributeDefService.getAllAttributeDef(searchRequest);
        return ResponseEntity.ok(allAttributeDef);
    }

    /**
     * POST : /api/clothes/attribute-defs
     * 의상 속성 정의 등록
     */
    @PostMapping
    public ResponseEntity<ClothesAttributeDefResponse> postAttributeDef(
            @Valid @RequestBody ClothesAttributeDefCreateRequest clothesAttributeDefCreateRequest
            ) {
        ClothesAttributeDefResponse attributeDef
                = clothesAttributeDefService.createAttributeDef(clothesAttributeDefCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(attributeDef);
    }

    /**
     * PATCH : /api/clothes/attribute-defs/{definitionId}
     * 의상 속성 정의 수정
     */
    @PatchMapping("/{definitionId}")
    public ResponseEntity<ClothesAttributeDefResponse> patchAttributeDef(
            @PathVariable UUID definitionId,
            @Valid @RequestBody ClothesAttributeDefUpdateRequest clothesAttributeDefUpdateRequest
            ) {
        ClothesAttributeDefResponse attributeDef
                = clothesAttributeDefService.updateAttributeDef(definitionId, clothesAttributeDefUpdateRequest);
        return ResponseEntity.ok().body(attributeDef);
    }

    /**
     * DELETE : /api/clothes/attribute--defs/{definitionId}
     * 의상 속성 정의 삭제
     */
    @DeleteMapping("/{definitionId}")
    public ResponseEntity<Void> deleteAttributeDef(@PathVariable UUID definitionId){
        clothesAttributeDefService.deleteAttributeDef(definitionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
