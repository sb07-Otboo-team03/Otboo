package com.codeit.otboo.domain.clothes.attribute.attributedef.controller.docs;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "의상 속성 정의", description = "의상 속성 정의 관련 API")
public interface ClothesAttributeDefControllerDocs {

    @Operation(
            summary = "의상 속성 정의 목록 조회",
            description = "의상 속성 정의 목록 조회 API"
    )
    @Parameters({
            @Parameter(
                    name = "sortBy",
                    in = ParameterIn.QUERY,
                    required = true,
                    schema = @Schema(
                            allowableValues = {"createdAt", "name"}
                    )
            ),
            @Parameter(
                    name = "sortDirection",
                    in = ParameterIn.QUERY,
                    required = true,
                    schema = @Schema(
                            allowableValues = {"ASCENDING", "DESCENDING"}
                    )
            ),
            @Parameter(
                    name = "keywordLike",
                    in = ParameterIn.QUERY,
                    required = false,
                    schema = @Schema(
                            type = "string"
                    )
            )
    })
    @ApiResponse(
            responseCode = "200",
            description = "의상 속성 정의 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = ClothesAttributeDefResponse.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "의상 속성 정의 목록 조회 실패",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<List<ClothesAttributeDefResponse>> getAllClothesAttributeDefs(
            @Parameter(hidden = true)
            @ModelAttribute ClothesAttributeSearchRequest searchRequest
    );


    @Operation(
            summary = "의상 속성 정의 등록",
            description = "의상 속성 정의 등록 API"
    )
    @ApiResponse(
            responseCode = "201",
            description = "의상 속성 정의 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = ClothesAttributeDefResponse.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "의상 속성 정의 등록 실패",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                    "definition_id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                    "code" : "CLOTHES_ATTRIBUTE_ALREADY_EXISTS",
                                    "message" : "이미 존재하는 의상 속성입니다."
                                    }
                                    """
                    )
            )
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ClothesAttributeDefResponse> postAttributeDef(
            @Valid @RequestBody ClothesAttributeDefCreateRequest clothesAttributeDefCreateRequest
    );

    @Operation(
            summary = "의상 속성 정의 수정",
            description = "의상 속성 정의 수정 API"
    )
    @Parameter(
            name = "definitionId",
            required = true,
            schema = @Schema(
                    implementation = String.class
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "의상 속성 정의 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = ClothesAttributeDefResponse.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "의상 속성 정의 수정 실패",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "ATTRIBUTE_NOT_FOUND",
                                    summary = "속성 정의를 찾을 수 없음",
                                    value = """
                                            {
                                            "definition_id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                            "code" : "CLOTHES_ATTRIBUTE_DEFINITION_NOT_FOUND",
                                            "message" : "속성 정의를 찾을 수 없습니다."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "ATTRIBUTE_ALREADY_EXISTS",
                                    summary = "이미 존재하는 속성 정의임",
                                    value = """
                                            {
                                            "definition_id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                            "code" : "CLOTHES_ATTRIBUTE_ALREADY_EXISTS",
                                            "message" : "이미 존재하는 속성 정의입니다."
                                            }
                                            """
                            )
                    }
            )
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ClothesAttributeDefResponse> patchAttributeDef(
            @PathVariable UUID definitionId,
            @Valid @RequestBody ClothesAttributeDefUpdateRequest clothesAttributeDefUpdateRequest
    );

    @Operation(
            summary = "의상 속성 정의 삭제",
            description = "의상 속성 정의 삭제 API"
    )
    @Parameter(
            name = "definitionId",
            required = true,
            schema = @Schema(
                    implementation = String.class
            )
    )
    @ApiResponse(
            responseCode = "204",
            description = "의상 속성 정의 삭제 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = ClothesAttributeDefResponse.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "의상 속성 정의 삭제 실패",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                    "definition_id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                    "code" : "CLOTHES_ATTRIBUTE_DEFINITION_NOT_FOUND",
                                    "message" : "속성 정의를 찾을 수 없습니다."
                                    }
                                    """
                    )
            )
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Void> deleteAttributeDef(@PathVariable UUID definitionId);

}