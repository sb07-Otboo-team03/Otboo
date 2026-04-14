package com.codeit.otboo.domain.feed.controller.docs;

import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
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

@Tag(name = "피드 관리", description = "피드 관련 API")
public interface FeedControllerDocs {

    @Operation(
            summary = "피드 등록",
            description = """
                    피드 등록 API
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "피드 등록 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = FeedResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "AuthorizationDeniedException",
                                                        "message": "요청하신 작업에 대한 권한이 없습니다.",
                                                        "details": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "날씨를 찾을 수 없음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "WEATHER_NOT_FOUND",
                                                        "message": "날씨 정보를 찾을 수 없습니다.",
                                                        "details": {
                                                            "weatherId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
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
    ResponseEntity<FeedResponse> createFeed(@Valid @RequestBody FeedCreateRequest request);

    @Operation(
            summary = "피드 목록 조회",
            description = "피드 목록 조회 API",
            responses = {

                    @ApiResponse(responseCode = "200", description = "피드 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CursorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "data": [
                                                        {
                                                          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                          "createdAt": "2026-04-13T04:18:21.311Z",
                                                          "updatedAt": "2026-04-13T04:18:21.311Z",
                                                          "author": {
                                                            "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                            "name": "string",
                                                            "profileImageUrl": "string"
                                                          },
                                                          "weather": {
                                                            "weatherId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                            "skyStatus": "CLEAR",
                                                            "precipitation": {
                                                              "type": "NONE",
                                                              "amount": 0.1,
                                                              "probability": 0.1
                                                            },
                                                            "temperature": {
                                                              "current": 0.1,
                                                              "comparedToDayBefore": 0.1,
                                                              "min": 0.1,
                                                              "max": 0.1
                                                            }
                                                          },
                                                          "ootds": [
                                                            {
                                                              "clothesId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                              "name": "string",
                                                              "imageUrl": "string",
                                                              "type": "TOP",
                                                              "attributes": [
                                                                {
                                                                  "definitionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                                  "definitionName": "string",
                                                                  "selectableValues": [
                                                                    "string"
                                                                  ],
                                                                  "value": "string"
                                                                }
                                                              ]
                                                            }
                                                          ],
                                                          "content": "string",
                                                          "likeCount": 9007199254740991,
                                                          "commentCount": 1073741824,
                                                          "likedByMe": true
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
    ResponseEntity<CursorResponse<FeedResponse>> getFeedList(@ParameterObject @ModelAttribute FeedSearchRequest request,
                                                             @AuthenticationPrincipal OtbooUserDetails details);

    @Operation(
            summary = "피드 수정",
            description = "피드 수정 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "피드 수정 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = FeedResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "AccessDeniedException",
                                                        "message": "요청하신 작업에 대한 권한이 없습니다.",
                                                        "details": null
                                                    }
                                                    """
                                    )
                            )
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
                    )

            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<FeedResponse> updateFeed(@Valid @RequestBody FeedUpdateRequest request,
                                            @PathVariable UUID feedId,
                                            @AuthenticationPrincipal OtbooUserDetails details);

    @Operation(
            summary = "피드 삭제",
            description = "피드 삭제 API",
            responses = {
                    @ApiResponse(responseCode = "204", description = "피드 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "exceptionName": "AccessDeniedException",
                                                        "message": "요청하신 작업에 대한 권한이 없습니다.",
                                                        "details": null
                                                    }
                                                    """
                                    )
                            )
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
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId,
                                    @AuthenticationPrincipal OtbooUserDetails details);
}
