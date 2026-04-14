package com.codeit.otboo.domain.directmessage.controller.docs;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "DirectMessage", description = "DirectMessage API")
public interface DirectMessageControllerDocs {

    @Operation(
        summary = "DM 목록 조회",
        description = "DM 목록 조회 API",
        operationId = "getDms"
    )
    @Parameters({
        @Parameter(
            name = "userId",
            description = "조회할 유저 ID",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "cursor",
            description = "커서 기반 페이징 cursor",
            required = false,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "idAfter",
            description = "특정 ID 이후 데이터 조회",
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
    @ApiResponses(value = {

        @ApiResponse(
            responseCode = "200",
            description = "DM 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = CursorResponse.class)
            )
        ),

        @ApiResponse(
            responseCode = "400",
            description = "DM 목록 조회 실패",
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
    ResponseEntity<CursorResponse<DirectMessageResponse>> getDirectMessages(
        @AuthenticationPrincipal OtbooUserDetails userDetails,
        @RequestParam UUID userId,
        @ParameterObject @ModelAttribute @Valid CursorRequest cursorRequest
    );
}