package com.codeit.otboo.domain.clothes.recommendation.controller.docs;

import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "추천 관리", description = "추천 관련 API")
public interface RecommendationControllerDocs {

    @Operation(
            summary = "추천 조회",
            description = "추천 조회 API"
    )
    @Parameter(
            name = "weatherId",
            in = ParameterIn.QUERY,
            required = true
    )
    @ApiResponse(
            responseCode = "200",
            description = "추천 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RecommendationResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "추천 조회 실패",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "WEATHER_NOT_FOUND",
                                    summary = "날씨 정보를 찾을 수 없음",
                                    value = """
                                            {
                                            "definition_id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                            "code" : "WEATHER_NOT_FOUND",
                                            "message" : "날씨 정보를 찾을 수 없습니다."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "PROFILE_NOT_FOUND",
                                    summary = "프로필 정보를 찾을 수 없음",
                                    value = """
                                            {
                                            "definition_id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                            "code" : "PROFILE_NOT_FOUND",
                                            "message" : "프로필 정보를 찾을 수 없습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<RecommendationResponse> recommend(
            @RequestParam UUID weatherId,
            Authentication authentication
    );
}
