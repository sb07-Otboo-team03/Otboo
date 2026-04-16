package com.codeit.otboo.domain.like.controller.docs;

import com.codeit.otboo.global.exception.ErrorResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "좋아요 관리", description = "좋아요 관련 API")
public interface LikeControllerDocs {

    @Operation(
            summary = "피드 좋아요",
            description = """
                    피드 좋아요 API
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "피드 좋아요 성공"),
                    @ApiResponse(responseCode = "404", description = "피드를 찾을 수 없음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "FEED_NOT_FOUND",
                                                        "message": "해당 피드를 찾을 수 없습니다.",
                                                        "details": {
                                                            "feedId": "1298802c-3d43-49b6-819e-9ebc9fcfaa11"
                                                        }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "USER_NOT_FOUND",
                                                        "message": "해당 유저를 찾을 수 없습니다.",
                                                        "details": {
                                                            "feedId": "eb069fde-ab23-458e-a8a9-f29c5c1c6f8a"
                                                        }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "409", description = "좋아요가 이미 존재함",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "LIKE_ALREADY_EXISTS",
                                                        "message": "좋아요가 이미 존재합니다.",
                                                        "details": {
                                                            "feedId": "f6f7459f-22f6-449f-98f4-cbd4a891ecf9",
                                                            "userId": "06d485ce-bb26-4a04-bb52-69ab9c0b3661"
                                                        }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<Void> like(@PathVariable("feedId") UUID feedId,
                              @AuthenticationPrincipal OtbooUserDetails details);

    @Operation(
            summary = "피드 좋아요 취소",
            description = """
                    피드 좋아요 취소 API
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "피드 좋아요 취소 성공"),
                    @ApiResponse(responseCode = "404", description = "좋아요를 찾을 수 없음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "LIKE_NOT_FOUND",
                                                        "message": "좋아요를 찾을 수 없습니다.",
                                                        "details": {
                                                            "feedId": "f6f7459f-22f6-449f-98f4-cbd4a891ecf9",
                                                            "userId": "06d485ce-bb26-4a04-bb52-69ab9c0b3661"
                                                        }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<Void> unlike(@PathVariable("feedId") UUID feedId,
                                @AuthenticationPrincipal OtbooUserDetails details);
}
