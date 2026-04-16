package com.codeit.otboo.domain.comment.controller.docs;

import com.codeit.otboo.domain.comment.dto.CommentCreateRequest;
import com.codeit.otboo.domain.comment.dto.CommentResponse;
import com.codeit.otboo.domain.comment.dto.CommentSearchRequest;
import com.codeit.otboo.global.exception.ErrorResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
public interface CommentControllerDocs {

    @Operation(
            summary = "피드 댓글 등록",
            description = "피드 댓글 등록 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "피드 댓글 등록 성공",
                            content = @Content(mediaType = "application.json",
                                    schema = @Schema(implementation = CommentResponse.class))
                    ),
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
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<CommentResponse> createComment(@PathVariable("feedId") UUID feedId,
                                                  @AuthenticationPrincipal OtbooUserDetails details,
                                                  @Valid @RequestBody CommentCreateRequest request);


    @Operation(
            summary = "피드 댓글 조회",
            description = "피드 댓글 조회 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "피드 댓글 조회 성공",
                            content = @Content(mediaType = "application.json",
                                    schema = @Schema(implementation = CursorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "data": [
                                                        {
                                                          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                          "createdAt": "2026-04-13T04:53:58.163Z",
                                                          "feedId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                          "author": {
                                                            "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                            "name": "string",
                                                            "profileImageUrl": "string"
                                                          },
                                                          "content": "string"
                                                        }
                                                      ],
                                                      "nextCursor": "string",
                                                      "nextIdAfter": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                      "hasNext": true,
                                                      "totalCount": 9007199254740991,
                                                      "sortBy": "string",
                                                      "sortDirection": "ASCENDING"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<CursorResponse<CommentResponse>> getAllComments(@ParameterObject @ModelAttribute
                                                                   CommentSearchRequest request);
}
