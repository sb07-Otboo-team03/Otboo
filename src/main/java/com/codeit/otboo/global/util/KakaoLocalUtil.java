package com.codeit.otboo.global.util;

import com.codeit.otboo.global.exception.KakaoApiException;
import com.codeit.otboo.global.exception.KakaoApiInvalidResponseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalUtil {

    private final RestClient restClient;

    @Value( "${api.kakao-rest-api-key}")
    private String kakaoApiKey;

    /**
     * 위경도 → 행정동/법정동 주소 4단계 리스트 반환
     * 순서: [시/도, 구/군, 동/읍면, 리/통]
     *
     * @param longitude 경도 (x)
     * @param latitude  위도 (y)
     * @param regionType 행정동(H) 또는 법정동(B) 우선순위
     * @return List<String> - 4개 요소 (없으면 빈 문자열 "")
     */
    public List<String> getAddressLevels(double longitude, double latitude, KakaoRegionType regionType) {
        Optional<JsonObject> regionOpt = getRegionInfo(longitude, latitude, regionType);

        if (regionOpt.isEmpty()) {
            return List.of("", "", "", "");
        }

        JsonObject doc = regionOpt.get();

        List<String> levels = new ArrayList<>(4);
        levels.add(doc.get("region_1depth_name").getAsString());
        levels.add(doc.get("region_2depth_name").getAsString());
        levels.add(doc.get("region_3depth_name").getAsString());
        levels.add(doc.get("region_4depth_name").getAsString());

        return Collections.unmodifiableList(levels);
    }

    /**
     * 내부 헬퍼 - 실제 API 호출 및 파싱
     */
    private Optional<JsonObject> getRegionInfo(double x, double y, KakaoRegionType regionType) {
        String response;

        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("dapi.kakao.com")
                            .path("/v2/local/geo/coord2regioncode.json")
                            .queryParam("x", x)
                            .queryParam("y", y)
                            .build())
                    .header("Authorization", "KakaoAK " + kakaoApiKey)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new KakaoApiException("Kakao 지역 API 호출 실패");
        }

        JsonObject root;
        JsonArray docs;

        try {
            root = JsonParser.parseString(response).getAsJsonObject();
            docs = root.getAsJsonArray("documents");

        } catch (Exception e) {
            throw new KakaoApiInvalidResponseException("Invalid JSON structure");
        }

        if (docs == null || docs.isEmpty()) {
            return Optional.empty();
        }

        String targetCode = regionType != null ? regionType.getCode() : null;

        if (targetCode != null) {
            for (int i = 0; i < docs.size(); i++) {
                JsonObject doc = docs.get(i).getAsJsonObject();
                if (targetCode.equals(doc.get("region_type").getAsString())) {
                    return Optional.of(doc);
                }
            }
        }

        return Optional.of(docs.get(0).getAsJsonObject());
    }

    /**
     * 카카오 로컬 API의 region_type 값
     * - H: 행정동 (행정구역 기준, 일상적으로 쓰는 동네 이름)
     * - B: 법정동 (법률·공식 문서 기준)
     */
    public enum KakaoRegionType {
        H("H", "행정동"),
        B("B", "법정동");

        private final String code;
        private final String description;

        KakaoRegionType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
