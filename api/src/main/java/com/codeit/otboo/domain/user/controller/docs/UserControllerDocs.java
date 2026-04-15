package com.codeit.otboo.domain.user.controller.docs;

import com.codeit.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.user.dto.request.*;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "프로필 관리", description = "프로필 관련 API")
public interface UserControllerDocs {

    @Operation(
            summary = "사용자 등록 (회원가입)",
            description = "name, email, password를 입력하여, 회원가입합니다. email은 중복될 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "id": "35eb4f38-9c7d-419b-9745-92f9b2b2582d",
                                                              "createdAt": "2026-04-14T06:11:10.4934591",
                                                              "email": "test@email.com",
                                                              "name": "홍길동",
                                                              "role": "USER",
                                                              "locked": false
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "exceptionName": "VALIDATION_ERROR",
                                                              "message": "유효성 검사에 실패하였습니다.",
                                                              "details": {
                                                                "email": "올바른 형식의 이메일 주소여야 합니다"
                                                              }
                                                            }
                                                            """
                                            )
                                    })),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }

    )
    @SecurityRequirement(name = "CsrfToken")
    ResponseEntity<UserResponse> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest);

    @Operation(
            summary = "프로필 조회",
            description = "userId에 해당하는 사용자의 Profile을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                              "name": "test@email.com",
                                                              "gender": "MALE",
                                                              "birthDate": "2026-01-01",
                                                              "location": {
                                                                "latitude": 37.55083474214788,
                                                                "longitude": 127.55083474214788,
                                                                "x": 37,
                                                                "y": 117,
                                                                "locationNames": [
                                                                              "ㅇㅇ시",
                                                                              "ㅇ구",
                                                                              "ㅇㅇ동",
                                                                              ""
                                                                ]
                                                              },
                                                              "temperatureSensitivity": 3,
                                                              "profileImageUrl": null
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(responseCode = "404", description = "유저 조회 실패",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                             {
                                                              "exceptionName": "USER_NOT_FOUND",
                                                              "message": "사용자를 찾을 수 없습니다.",
                                                              "details": {
                                                                "email": "test@111.com"
                                                              }
                                                            }
                                                            """
                                            )
                                    })),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }

    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<ProfileResponse> getProfile(@PathVariable("userId") UUID userId);

    @Operation(
            summary = "비밀번호 변경",
            description = "사용자의 비밀번호를 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "비밀번호 변경 성공"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
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
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "exceptionName": "VALIDATION_ERROR",
                                                              "message": "유효성 검사에 실패하였습니다.",
                                                              "details": {
                                                                "password": "크기가 6에서 2147483647 사이여야 합니다"
                                                              }
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
    ResponseEntity<Void> updatePassword(@PathVariable("userId") UUID userId, @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest);


    @Operation(
            summary = "유저 목록 조회",
            description = "유저의 계정 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "계정 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CursorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                        {
                                                          "data": [
                                                            {
                                                              "id": "11111111-1111-1111-1111-111111111111",
                                                              "createdAt": "2026-04-14T10:00:00",
                                                              "email": "admin@example.com",
                                                              "name": "관리자",
                                                              "role": "ADMIN",
                                                              "locked": false
                                                            },
                                                            {
                                                              "id": "22222222-2222-2222-2222-222222222222",
                                                              "createdAt": "2026-04-14T09:00:00",
                                                              "email": "user1@example.com",
                                                              "name": "홍길동",
                                                              "role": "USER",
                                                              "locked": false
                                                            },
                                                            {
                                                              "id": "33333333-3333-3333-3333-333333333333",
                                                              "createdAt": "2026-04-14T08:00:00",
                                                              "email": "user2@example.com",
                                                              "name": "김철수",
                                                              "role": "USER",
                                                              "locked": true
                                                            }
                                                          ],
                                                          "nextCursor": "2026-04-14T08:00:00",
                                                          "nextIdAfter": "33333333-3333-3333-3333-333333333333",
                                                          "hasNext": true,
                                                          "totalCount": 100,
                                                          "sortBy": "createdAt",
                                                          "sortDirection": "DESCENDING"
                                                        }
                                                        """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<CursorResponse<UserResponse>> getAllUser(@Valid @ParameterObject @ModelAttribute UserSearchRequest request);

    @Operation(
            summary = "프로필 수정",
            description = "userId에 해당하는 사용자의 프로필 정보를 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "프로필 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                              "name": "홍길동",
                                                              "gender": "MALE",
                                                              "birthDate": "2026-01-01",
                                                              "location": {
                                                                "latitude": 37.55083474214788,
                                                                "longitude": 127.55083474214788,
                                                                "x": 37,
                                                                "y": 117,
                                                                "locationNames": [
                                                                  "서울특별시",
                                                                  "강남구",
                                                                  "역삼동",
                                                                  ""
                                                                ]
                                                              },
                                                              "temperatureSensitivity": 3,
                                                              "profileImageUrl": null
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "exceptionName": "VALIDATION_ERROR",
                                                              "message": "유효성 검사에 실패하였습니다.",
                                                              "details": {
                                                                "name": "이름은 비어 있을 수 없습니다."
                                                              }
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "exceptionName": "USER_NOT_FOUND",
                                                              "message": "사용자를 찾을 수 없습니다.",
                                                              "details": {
                                                                "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                                                              }
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody ProfileUpdateRequest request
    );

    @Operation(
            summary = "사용자 잠금 상태 변경",
            description = "userId에 해당하는 사용자의 계정 잠금 상태를 변경합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "잠금 상태 변경 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "id": "35eb4f38-9c7d-419b-9745-92f9b2b2582d",
                                                              "createdAt": "2026-04-14T06:11:10.4934591",
                                                              "email": "test@email.com",
                                                              "name": "홍길동",
                                                              "role": "USER",
                                                              "locked": true
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                                {
                                                                  "exceptionName": "VALIDATION_ERROR",
                                                                  "message": "유효성 검사에 실패하였습니다.",
                                                                  "details": {
                                                                    "locked": "널이어서는 안됩니다"
                                                                  }
                                                                }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "exceptionName": "USER_NOT_FOUND",
                                                              "message": "사용자를 찾을 수 없습니다.",
                                                              "details": {
                                                                "userId": "35eb4f38-9c7d-419b-9745-92f9b2b2582d"
                                                              }
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<UserResponse> updateUserLock(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UserLockUpdateRequest userLockUpdateRequest
    );

    @Operation(
            summary = "사용자 권한 변경",
            description = "userId에 해당하는 사용자의 권한을 변경합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "권한 변경 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "id": "35eb4f38-9c7d-419b-9745-92f9b2b2582d",
                                                              "createdAt": "2026-04-14T06:11:10.4934591",
                                                              "email": "test@email.com",
                                                              "name": "홍길동",
                                                              "role": "ADMIN",
                                                              "locked": false
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "exceptionName": "USER_NOT_FOUND",
                                                              "message": "사용자를 찾을 수 없습니다.",
                                                              "details": {
                                                                "userId": "35eb4f38-9c7d-419b-9745-92f9b2b2582d"
                                                              }
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @SecurityRequirement(name = "CsrfToken")
    @SecurityRequirement(name = "BearerAuth")
    ResponseEntity<UserResponse> updateUserRole(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UserRoleUpdateRequest userRoleUpdateRequest
    );

}