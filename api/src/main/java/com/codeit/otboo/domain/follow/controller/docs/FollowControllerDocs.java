package com.codeit.otboo.domain.follow.controller.docs;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.global.exception.ErrorResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "팔로우 관리", description = "팔로우 관련 API")
public interface FollowControllerDocs {

    @Operation(
        summary = "팔로우 생성",
        description = "팔로우 생성 API",
        operationId = "createFollow"
    )
    @ApiResponses(value = {

        @ApiResponse(
            responseCode = "201",
            description = "팔로우 생성 성공",
            content = @Content(
                schema = @Schema(implementation = FollowResponse.class)
            )
        ),

        @ApiResponse(
            responseCode = "400",
            description = "팔로우 생성 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),

        @ApiResponse(
            responseCode = "500",
            description = "Unhandled exception",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<FollowResponse> createFollow(
        @Valid FollowCreateRequest request
    );

    @Operation(
        summary = "팔로우 요약 정보 조회",
        description = "팔로우 요약 정보 조회 API",
        operationId = "getFollowSummary"
    )
    @Parameters({
        @Parameter(
            name = "userId",
            description = "유저 ID",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "팔로우 요약 정보 조회 성공",
            content = @Content(
                schema = @Schema(implementation = FollowSummaryResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "팔로우 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Unhandled exception",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<FollowSummaryResponse> getFollowSummary(
        @RequestParam UUID userId,
        @AuthenticationPrincipal OtbooUserDetails userDetails
    );

    @Operation(
        summary = "팔로잉 목록 조회",
        description = "팔로잉 목록 조회 API",
        operationId = "getFollowings"
    )
    @Parameters({
        @Parameter(
            name = "followerId",
            description = "팔로우 하는 유저 ID",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "nameLike",
            description = "이름 검색 필터",
            required = false,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "cursor",
            description = "커서",
            required = false,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "idAfter",
            description = "이후 ID",
            required = false,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "limit",
            description = "조회 개수",
            required = true,
            schema = @Schema(type = "integer", format = "int32")
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "팔로잉 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = CursorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "팔로잉 목록 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),

        @ApiResponse(
            responseCode = "500",
            description = "Unhandled exception",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<CursorResponse<FollowResponse>> getFollowings(
        @RequestParam UUID followerId,
        @RequestParam(required = false) String nameLike,
        @ParameterObject @ModelAttribute @Valid CursorRequest cursorRequest
    );

    @Operation(
        summary = "팔로워 목록 조회",
        description = "팔로워 목록 조회 API",
        operationId = "getFollowers"
    )
    @Parameters({
        @Parameter(
            name = "followeeId",
            description = "팔로우 받는 유저 ID",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "nameLike",
            description = "이름 검색 필터",
            required = false,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "cursor",
            description = "커서",
            required = false,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "idAfter",
            description = "이후 ID",
            required = false,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "limit",
            description = "조회 개수",
            required = true,
            schema = @Schema(type = "integer", format = "int32")
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "팔로워 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = CursorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "팔로워 목록 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),

        @ApiResponse(
            responseCode = "500",
            description = "Unhandled exception",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<CursorResponse<FollowResponse>> getFollowers(
        @RequestParam UUID followeeId,
        @RequestParam(required = false) String nameLike,
        @ParameterObject @ModelAttribute @Valid CursorRequest cursorRequest
    );

    @Operation(
        summary = "팔로우 취소",
        description = "팔로우 취소 API",
        operationId = "cancelFollow"
    )
    @Parameters({
        @Parameter(
            name = "followId",
            description = "팔로우 ID",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "팔로우 취소 성공"
        ),

        @ApiResponse(
            responseCode = "400",
            description = "팔로우 취소 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),

        @ApiResponse(
            responseCode = "500",
            description = "Unhandled exception",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<Void> cancelFollow(
        @PathVariable UUID followId
    );
}
