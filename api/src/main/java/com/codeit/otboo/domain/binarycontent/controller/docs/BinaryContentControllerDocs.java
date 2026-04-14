package com.codeit.otboo.domain.binarycontent.controller.docs;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentPresignedUrlRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentPresignedUrlResponse;
import com.codeit.otboo.global.exception.ErrorResponse;
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

@Tag(name = "파일 관리", description = "파일 관련 API")
public interface BinaryContentControllerDocs {
    @Operation(
            summary = "presigned url 발급",
            description = "S3에 이미지를 업로드 하기 위하여 URL 을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이미지 url 발급 성공",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = BinaryContentPresignedUrlResponse.class),
                                examples = {
                                        @ExampleObject(
                                                value = """
                                                            {
                                                                "binaryContentId": "02c746bf-1fdf-4309-b18d-3c850dcb21da",
                                                                "uploadUrl": "https://otboo-binary-content-storage555.s3.ap-northeast-2.amazonaws.com/binary/02c746bf-1fdf-4309-b18d-3c850dcb21da?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260413T213425Z&X-Amz-SignedHeaders=content-type%3Bhost&X-Amz-Credential=AKIAXNGUU5I47ENLVV4I%2F20260413%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=600&X-Amz-Signature=176434cb5df19fa55f147fb03855f18bfd8c9c429c5f0f1f2e7a5caefcf7cffd"
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
                                            name = "파일 이름 또는 파일 타입 공백",
                                                value = """
                                                    {
                                                        "exceptionName": "VALIDATION_ERROR",
                                                        "message": "유효성 검사에 실패하였습니다.",
                                                        "details": {
                                                            "fileName": "공백일 수 없습니다"
                                                        }
                                                    }
                                                """
                                        ),
                                        @ExampleObject(
                                                name = "파일 size가 음수",
                                                value = """
                                                    {
                                                        "exceptionName": "VALIDATION_ERROR",
                                                        "message": "유효성 검사에 실패하였습니다.",
                                                        "details": {
                                                            "size": "0 이상이어야 합니다"
                                                        }
                                                    }
                                                """
                                        )
                                }
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "파일 타입 에러",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "파일 타입이 정상적이지 않을 때 오류",
                                                    value = """
                                                    {
                                                        "exceptionName": "INVALID_FILE_TYPE",
                                                        "message": "파일 타입이 맞지 않습니다.",
                                                        "details": {
                                                            "supportedTypes": "이미지파일",
                                                            "requestedType": "png"
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
    ResponseEntity<BinaryContentPresignedUrlResponse> issueImagePresignedUrl(
            @Valid @RequestBody BinaryContentPresignedUrlRequest request
    );


    @Operation(
            summary = "이미지 업로드 성공 요청으로 업데이트",
            description = "이미지 업로드 상태로 업데이트 합니다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이미지 업로드 상태 업데이트 성공"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 UUID 를 가진 파일이 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "해당 UUID 를 가진 파일이 없음 예시",
                                            value = """
                                                    {
                                                        "exceptionName": "BINARY_CONTENT_NOT_FOUND",
                                                        "message": "해당 UUID를 가진 바이너리 컨텐츠가 존재하지 않습니다.",
                                                        "details": {
                                                            "binaryContentId": "cea06c4c-84fd-43de-ab05-d67393a3cc29"
                                                        }
                                                    }
                                                    """
                                    )
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
    ResponseEntity<Void> completeUpload(@PathVariable UUID binaryContentId);
}