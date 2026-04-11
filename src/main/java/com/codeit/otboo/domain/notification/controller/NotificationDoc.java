package com.codeit.otboo.domain.notification.controller;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.global.exception.ErrorResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "알림", description = "알림 관련 API")
public interface NotificationDoc {

    @Operation(
        summary = "알림 목록 조회",
        description = "알림 목록 조회 API",
        operationId = "getNotifications"
    )
    @Parameters({
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
            description = "알림 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = CursorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "알림 목록 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<CursorResponse<NotificationResponse>> getNotifications(
        @Parameter(hidden = true)
        @AuthenticationPrincipal OtbooUserDetails authPrincipal,

        @ParameterObject @ModelAttribute @Valid CursorRequest cursorRequest
    );

    @Operation(
        summary = "알림 읽음 처리",
        description = "알림 읽음 처리 API",
        operationId = "readNotifications"
    )
    @Parameters({
        @Parameter(
            name = "notificationId",
            description = "알림 ID",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "알림 읽음 처리 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "알림 읽음 처리 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<Void> deleteNotification(
        @Parameter(hidden = true)
        @AuthenticationPrincipal OtbooUserDetails authPrincipal,

        @PathVariable UUID notificationId
    );
}
