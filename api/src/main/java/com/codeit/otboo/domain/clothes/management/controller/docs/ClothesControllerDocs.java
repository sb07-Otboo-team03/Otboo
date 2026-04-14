package com.codeit.otboo.domain.clothes.management.controller.docs;

import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUrlRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesUrlResponse;
import com.codeit.otboo.global.exception.ErrorResponse;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "옷 관리", description = "옷 관련 API")
public interface ClothesControllerDocs {
    @Operation(
            summary = "옷 저장",
            description = "옷의 정보를 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "옷 저장 성공",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClothesResponse.class),
                                examples = {
                                    @ExampleObject(
                                            value = """
                                                        {
                                                            "id": "b47d3b11-ce17-4a67-888f-67b99c1e76da",
                                                            "ownerId": "6de3b7bc-b14c-4608-a343-2b16a27a7dd0",
                                                            "name": "새 옷",
                                                            "imageUrl": null,
                                                            "type": "ETC",
                                                            "attributes": []
                                                        }
                                                    """
                                    )
                                }
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "ownerId 가 NULL",
                                                    value = """
                                                    {
                                                         "exceptionName": "VALIDATION_ERROR",
                                                         "message": "유효성 검사에 실패하였습니다.",
                                                         "details": {
                                                             "ownerId": "널이어서는 안됩니다"
                                                         }
                                                    }
                                                """
                                            ),
                                            @ExampleObject(
                                                    name = "옷 이름이 공백 또는 NULL",
                                                    value = """
                                                    {
                                                         "exceptionName": "VALIDATION_ERROR",
                                                         "message": "유효성 검사에 실패하였습니다.",
                                                         "details": {
                                                             "name": "공백일 수 없습니다"
                                                         }
                                                    }
                                                """
                                            ),
                                            @ExampleObject(
                                                    name = "옷 타입이 NULL",
                                                    value = """
                                                        {
                                                            "exceptionName": "VALIDATION_ERROR",
                                                            "message": "유효성 검사에 실패하였습니다.",
                                                            "details": {
                                                                "type": "널이어서는 안됩니다"
                                                            }
                                                        }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "서버 에러",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                    {
                                                         "exceptionName": "INTERNAL_SERVER_ERROR",
                                                         "message": "서버 에러가 발생했습니다.",
                                                         "details": {}
                                                    }
                                                """
                                            )
                                    }
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<ClothesResponse> saveClothes(@Valid @RequestBody ClothesCreateRequest request);

    @Operation(
            summary = "옷 삭제",
            description = "옷을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "옷 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "옷 주인이 아닌 다른 계정이 삭제 시도",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class),
                                examples = {
                                        @ExampleObject(
                                                value = """
                                                    {
                                                         "exceptionName": "BadCredentialsException",
                                                         "message": "자격 증명에 실패하였습니다.",
                                                         "details": null
                                                    }
                                                """
                                        )
                            }
                        )
                    ),
                    @ApiResponse(responseCode = "500", description = "서버 에러",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "서버 내부 오류",
                                                    value = """
                                                    {
                                                         "exceptionName": "INTERNAL_SERVER_ERROR",
                                                         "message": "서버 에러가 발생했습니다.",
                                                         "details": {}
                                                    }
                                                """
                                            )
                                    }
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<Void> deleteClothes(@PathVariable UUID clothesId);

    @Operation(
            summary = "옷 수정",
            description = "옷을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "옷 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClothesResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                    {
                                                         "id": "8da831ef-3e9b-410d-a81b-7a55970775d5",
                                                         "ownerId": "6de3b7bc-b14c-4608-a343-2b16a27a7dd0",
                                                         "name": "수정된 옷",
                                                         "imageUrl": null,
                                                         "type": "ETC",
                                                         "attributes": []
                                                     }
                                                """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "옷 주인이 아닌 다른 계정이 수정 시도",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                    {
                                                         "exceptionName": "BadCredentialsException",
                                                         "message": "자격 증명에 실패하였습니다.",
                                                         "details": null
                                                    }
                                                """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "옷 이름이 공백 또는 NULL",
                                                    value = """
                                                    {
                                                         "exceptionName": "VALIDATION_ERROR",
                                                         "message": "유효성 검사에 실패하였습니다.",
                                                         "details": {
                                                             "name": "공백일 수 없습니다"
                                                         }
                                                    }
                                                """
                                            ),
                                            @ExampleObject(
                                                    name = "옷 타입이 NULL",
                                                    value = """
                                                        {
                                                            "exceptionName": "VALIDATION_ERROR",
                                                            "message": "유효성 검사에 실패하였습니다.",
                                                            "details": {
                                                                "type": "널이어서는 안됩니다"
                                                            }
                                                        }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "서버 에러",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                    {
                                                         "exceptionName": "INTERNAL_SERVER_ERROR",
                                                         "message": "서버 에러가 발생했습니다.",
                                                         "details": {}
                                                    }
                                                """
                                            )
                                    }
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<ClothesResponse> updateClothes(
            @PathVariable UUID clothesId,
            @Valid @RequestBody ClothesUpdateRequest request);

    @Operation(
            summary = "옷 조회",
            description = "옷 목록을 조회 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "옷 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CursorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name ="limit 가 5일 때의 조회",
                                                    value = """
                                                        {
                                                             "data": [
                                                                 {
                                                                     "id": "8da831ef-3e9b-410d-a81b-7a55970775d5",
                                                                     "ownerId": "6de3b7bc-b14c-4608-a343-2b16a27a7dd0",
                                                                     "name": "수정된 옷",
                                                                     "imageUrl": null,
                                                                     "type": "ETC",
                                                                     "attributes": []
                                                                 },
                                                                 {
                                                                     "id": "87c46d2a-47eb-4fb3-87bc-3e7d4d18d341",
                                                                     "ownerId": "6de3b7bc-b14c-4608-a343-2b16a27a7dd0",
                                                                     "name": "새 옷",
                                                                     "imageUrl": null,
                                                                     "type": "ETC",
                                                                     "attributes": []
                                                                 },
                                                                 {
                                                                     "id": "b47d3b11-ce17-4a67-888f-67b99c1e76da",
                                                                     "ownerId": "6de3b7bc-b14c-4608-a343-2b16a27a7dd0",
                                                                     "name": "새 옷",
                                                                     "imageUrl": null,
                                                                     "type": "ETC",
                                                                     "attributes": []
                                                                 },
                                                                 {
                                                                     "id": "fe5e862c-22d6-4b93-99f7-8405facabcdc",
                                                                     "ownerId": "6de3b7bc-b14c-4608-a343-2b16a27a7dd0",
                                                                     "name": "위트 넘치는 드로잉 얹은 위캔더스 × 럭키림",
                                                                     "imageUrl": "https://otboo-binary-content-storage555.s3.ap-northeast-2.amazonaws.com/binary/69385ab8-c6d4-4880-a5f4-62888737a54b?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260414T185619Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIAXNGUU5I47ENLVV4I%2F20260414%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=600&X-Amz-Signature=658f0dea5a9e40d604361814417beca3df7910797bfd4c946462ea7a254a0301",
                                                                     "type": "UNDERWEAR",
                                                                     "attributes": []
                                                                 },
                                                                 {
                                                                     "id": "07b05df3-87ae-470e-ae0c-41408ed58777",
                                                                     "ownerId": "6de3b7bc-b14c-4608-a343-2b16a27a7dd0",
                                                                     "name": "위트 넘치는 드로잉 얹은 위캔더스 × 럭키림",
                                                                     "imageUrl": "https://otboo-binary-content-storage555.s3.ap-northeast-2.amazonaws.com/binary/adc6e542-7d73-4fad-99a9-683f53cde2b2?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260414T185619Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIAXNGUU5I47ENLVV4I%2F20260414%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=600&X-Amz-Signature=6947ccffff782e89cd4bbf773b068007717d8c93778af248f631c8db850812fa",
                                                                     "type": "UNDERWEAR",
                                                                     "attributes": []
                                                                 }
                                                             ],
                                                             "nextCursor": "2026-04-14T23:28:18.142419",
                                                             "nextIdAfter": "07b05df3-87ae-470e-ae0c-41408ed58777",
                                                             "hasNext": true,
                                                             "totalCount": 34,
                                                             "sortBy": "createdAt",
                                                             "sortDirection": "DESCENDING"
                                                         }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name ="ownerId 가 NULL",
                                                    value = """
                                                        {
                                                            "exceptionName": "VALIDATION_ERROR",
                                                            "message": "유효성 검사에 실패하였습니다.",
                                                            "details": {
                                                                "ownerId": "널이어서는 안됩니다"
                                                            }
                                                        }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name ="limit 가 1보다 작음",
                                                    value = """
                                                        {
                                                             "exceptionName": "VALIDATION_ERROR",
                                                             "message": "유효성 검사에 실패하였습니다.",
                                                             "details": {
                                                                 "limit": "1 이상이어야 합니다"
                                                             }
                                                        }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저의 옷 목록 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name ="",
                                                    value = """
                                                        {
                                                            "exceptionName": "USER_NOT_FOUND",
                                                            "message": "사용자를 찾을 수 없습니다.",
                                                            "details": null
                                                        }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "서버 에러",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                    {
                                                         "exceptionName": "INTERNAL_SERVER_ERROR",
                                                         "message": "서버 에러가 발생했습니다.",
                                                         "details": {}
                                                    }
                                                """
                                            )
                                    }
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<CursorResponse<ClothesResponse>> getAllClothes(@Valid ClothesCursorPageRequest request);

    @Operation(
            summary = "옷 링크로 정보 불러오기",
            description = "옷 쇼핑몰 상세 페이지 링크로 옷의 정보를 불러온다",
            responses = {
                @ApiResponse(responseCode = "200", description = "옷 정보 불러오기 성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ClothesUrlResponse.class),
                            examples = {
                                @ExampleObject(
                                        value = """
                                            {
                                                "name": "디키즈의 워크웨어와 ASSC 스트릿의 충돌",
                                                "imageUrl": "https://image.msscdn.net/cms/v2/content/file_1774598983166_728239900_0689ui.jpg"
                                            }
                                        """
                                )
                            }
                    )
                ),
                @ApiResponse(responseCode = "400", description = "Validation 오류",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class),
                                examples = {
                                        @ExampleObject(
                                                name ="url 이 공백",
                                                value = """
                                                    {
                                                         "exceptionName": "VALIDATION_ERROR",
                                                         "message": "유효성 검사에 실패하였습니다.",
                                                         "details": {
                                                             "url": "공백일 수 없습니다"
                                                         }
                                                     }
                                                """
                                        )
                                }
                        )
                ),
                @ApiResponse(responseCode = "500", description = "서버 에러",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class),
                                examples = {
                                        @ExampleObject(
                                                value = """
                                                {
                                                     "exceptionName": "INTERNAL_SERVER_ERROR",
                                                     "message": "서버 에러가 발생했습니다.",
                                                     "details": {}
                                                }
                                            """
                                        )
                                }
                        )
                )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<ClothesUrlResponse> getExtractions(@Valid ClothesUrlRequest request);
}
