package com.codeit.otboo.domain.weather.controller.openapi;

import com.codeit.otboo.domain.weather.dto.response.WeatherAPILocationResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherResponse;
import com.codeit.otboo.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "날씨 관리(WeatherController", description = "날씨 조회를 관리하는 API 입니다.")
public interface WeatherControllerDocs {

    @Operation(
            summary = "날씨 정보 조회",
            description = """
                    날씨 정보를 조회합니다.
                    
                    ## 요청 데이터(Request Param)
                    - longitude: 경도
                    - latitude: 위도
                    
                    ## 응답 데이터
                    - 성공 시 조회된 날씨 정보 리스트를 반환합니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode= "200",
                    description = "날씨 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = WeatherResponse.class)
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            [
                                                {
                                                    "id": "4344dae1-3ed9-4955-a8c4-9575a4376d1d",
                                                    "forecastedAt": "2026-04-09T00:00:00",
                                                    "forecastAt": "2026-04-09T00:00:00",
                                                    "location": {
                                                        "latitude": 37.5742138888888,
                                                        "longitude": 127.006397222222,
                                                        "x": 60,
                                                        "y": 127,
                                                        "locationNames": [
                                                            "서울특별시",
                                                            "종로구",
                                                            "종로5.6가동",
                                                            ""
                                                        ]
                                                    },
                                                    "skyStatus": "CLOUDY",
                                                    "precipitation": {
                                                        "type": "NONE",
                                                        "amount": 0.0,
                                                        "probability": 30.0
                                                    },
                                                    "humidity": {
                                                        "current": 65.0,
                                                        "comparedToDayBefore": 0.0
                                                    },
                                                    "temperature": {
                                                        "current": 7.0,
                                                        "comparedToDayBefore": 2.0,
                                                        "min": 6.0,
                                                        "max": 12.0
                                                    },
                                                    "windSpeed": {
                                                        "speed": 0.5,
                                                        "asWord": "WEAK"
                                                    }
                                                },
                                                {
                                                    "id": "ff3b66f2-942f-45bf-b1f8-74377111bae1",
                                                    "forecastedAt": "2026-04-09T00:00:00",
                                                    "forecastAt": "2026-04-10T00:00:00",
                                                    "location": {
                                                        "latitude": 37.5742138888888,
                                                        "longitude": 127.006397222222,
                                                        "x": 60,
                                                        "y": 127,
                                                        "locationNames": [
                                                            "서울특별시",
                                                            "종로구",
                                                            "종로5.6가동",
                                                            ""
                                                        ]
                                                    },
                                                    "skyStatus": "CLOUDY",
                                                    "precipitation": {
                                                        "type": "RAIN",
                                                        "amount": 2.0,
                                                        "probability": 60.0
                                                    },
                                                    "humidity": {
                                                        "current": 95.0,
                                                        "comparedToDayBefore": 30.0
                                                    },
                                                    "temperature": {
                                                        "current": 12.0,
                                                        "comparedToDayBefore": 5.0,
                                                        "min": 10.0,
                                                        "max": 14.0
                                                    },
                                                    "windSpeed": {
                                                        "speed": 1.0,
                                                        "asWord": "WEAK"
                                                    }
                                                }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "KMA API 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "KMA API Error",
                                            value = """
                                                    {
                                                      "exceptionName": "KMA_API_ERROR",
                                                      "message": "기상청 API 호출 중 오류가 발생했습니다.",
                                                      "details": {
                                                        "resultCode": "03",
                                                        "resultMsg": "NO_DATA"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Response",
                                            value = """
                                                    {
                                                      "exceptionName": "KMA_API_INVALID_RESPONSE",
                                                      "message": "기상청 API 응답 형식이 올바르지 않습니다.",
                                                      "details": {
                                                        "reason": "body is null"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<List<WeatherResponse>> getWeather(double longitude, double latitude);

    @Operation(
            summary = "위도, 경도에 맞는 지역 정보 조회",
            description = """
                    지역 정보를 조회합니다.
                    
                    ## 요청 데이터(Request Param)
                    - longitude: 경도
                    - latitude: 위도
                    
                    ## 응답 데이터
                    - 성공 시 지역 정보가 반환됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "지역 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WeatherAPILocationResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "latitude": 37.5742,
                                                "longitude": 127.0064,
                                                "x": 60,
                                                "y": 127,
                                                "locationNames": [
                                                    "서울특별시",
                                                    "종로구",
                                                    "종로5,6가동",
                                                    ""
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Kakao API 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Kakao API Error",
                                            value = """
                                                    {
                                                      "exceptionName": "KAKAO_API_ERROR",
                                                      "message": "외부 API 호출 중 오류가 발생했습니다.",
                                                      "details": {
                                                        "reason": "Kakao 지역 API 호출 실패"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Response",
                                            value = """
                                                    {
                                                      "exceptionName": "KAKAO_API_INVALID_RESPONSE",
                                                      "message": "외부 API 응답 형식이 올바르지 않습니다.",
                                                      "details": {
                                                        "reason": "Invalid JSON structure"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WeatherAPILocationResponse> getWeatherLocation(double longitude, double latitude);
}
