package com.codeit.otboo.domain.user.controller.docs;

import com.codeit.otboo.domain.user.dto.request.PasswordResetRequest;
import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.dto.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "인증 관리")
public interface AuthControllerDocs {
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 입력하여 로그인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = JwtResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "userDto": {
                                                                "id": "111c88f3-c4f1-4817-a08d-a385daf60000",
                                                                "createdAt": "2026-01-01T00:00:27.123456",
                                                                "email": "email@email.com",
                                                                "name": "name",
                                                                "role": "USER",
                                                                "locked": false
                                                              },
                                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0NzZlMjBkNC0xYjYwLTRjNGEtOGJhNC05OTc0N2NlODA4NmQiLCJpc3MiOiJjb2RlaXQiLCJzZXNzaW9uSWQiOiJmMjMzOGJmMS05NDBkLTRlY2EtYWIyZi0zNTcwMTA5MmI2NmUiLCJleHAiOjE3NzYwMjIyMTAsInRva2VuX3R5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NzYwMjEzMTAsImVtYWlsIjoidGVzdDNAY29kZWl0LmNvbSJ9.k6pMpFKJ1vFzvO_JGcaGa8ChEPhmRVE2b0eAQPZjgA4"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인 실패",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "인증 실패",
                                                    value = """
                                                            {
                                                              "exceptionName": "BadCredentialsException",
                                                              "message": "자격 증명에 실패하였습니다.",
                                                              "details": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "계정 잠금",
                                                    value = """
                                                            {
                                                              "exceptionName": "LockedException",
                                                              "message": "사용자 계정이 잠겨 있습니다.",
                                                              "details": null
                                                            }
                                                            """
                                            )
                                    }


                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    ResponseEntity<JwtResponse> signIn(@Valid @ModelAttribute SignInRequest signInRequest,
                                       HttpServletResponse response);


    @Operation(
            summary = "토큰 재발급",
            description = "쿠키(REFRESH_TOKEN)에 저장된 리프레시 토큰으로 리프레시 토큰과 엑세스 토큰을 재발급합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 재발급 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = JwtResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "userDto": {
                                                                "id": "111c88f3-c4f1-4817-a08d-a385daf60000",
                                                                "createdAt": "2026-01-01T00:00:27.123456",
                                                                "email": "email@email.com",
                                                                "name": "name",
                                                                "role": "USER",
                                                                "locked": false
                                                              },
                                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0NzZlMjBkNC0xYjYwLTRjNGEtOGJhNC05OTc0N2NlODA4NmQiLCJpc3MiOiJjb2RlaXQiLCJzZXNzaW9uSWQiOiJmMjMzOGJmMS05NDBkLTRlY2EtYWIyZi0zNTcwMTA5MmI2NmUiLCJleHAiOjE3NzYwMjIyMTAsInRva2VuX3R5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NzYwMjEzMTAsImVtYWlsIjoidGVzdDNAY29kZWl0LmNvbSJ9.k6pMpFKJ1vFzvO_JGcaGa8ChEPhmRVE2b0eAQPZjgA4"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            responseCode = "403",
                            description = "CsrfToken 에러",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "자격 증명 실패 (CsrfToken 에러)",
                                                    value = """
                                                            {
                                                              "exceptionName": "InvalidCsrfTokenException",
                                                              "message": "자격 증명에 실패하셨습니다.",
                                                              "details": {}
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "JWT 서명 검증 실패",
                                                    value = """
                                                            {
                                                              "exceptionName": "INVALID_SIGNATURE",
                                                              "message": "JWT 서명 검증 실패",
                                                              "details": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "인증 실패",
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
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )

    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<JwtResponse> refresh(
            @Parameter(hidden = true)
            @CookieValue("REFRESH_TOKEN") String refreshToken,
            HttpServletResponse response);

    @Operation(
            summary = "로그아웃",
            description = "로그아웃합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그아웃 실패 - 인증 실패",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "인증 실패",
                                                    value = """
                                                            {
                                                              "exceptionName": "BadCredentialsException",
                                                              "message": "자격 증명에 실패하였습니다.",
                                                              "details": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<Void> signOut(@AuthenticationPrincipal OtbooUserDetails userDetails, HttpServletResponse response);


    @Operation(
            summary = "CSRF 토큰 발급",
            description = """
                    CSRF 토큰을 발급합니다.
                    - 쿠키: XSRF-TOKEN
                    - 요청 시 헤더에 X-XSRF-TOKEN으로 전달.
                    """
    )
    @ApiResponse(
            responseCode = "204",
            description = "CSRF 토큰 발급 성공"
    )
    ResponseEntity<Void> getCsrfToken(
            @Parameter(hidden = true)
            CsrfToken csrfToken);


    @Operation(
            summary = "비밀번호 초기화",
            description = """
                    임시 비밀번호로 초기화 이후 이메일로 전송합니다.
                    """
    )
    @ApiResponse(
            responseCode = "204",
            description = "비밀번호 초기화 성공"
    )
    @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "존재하지 않는 사용자",
                                    value = """
                                            {
                                              "exceptionName": "USER_NOT_FOUND",
                                              "message": "사용자를 찾을 수 없습니다.",
                                              "details": {
                                                "email": "test@111.com"
                                              }
                                            }
                                            """
                            ),
                    }
            )

    )
    @SecurityRequirement(name = "CsrfToken")
    ResponseEntity<Void> passwordReset(@Valid @RequestBody PasswordResetRequest passwordResetRequest);
}
