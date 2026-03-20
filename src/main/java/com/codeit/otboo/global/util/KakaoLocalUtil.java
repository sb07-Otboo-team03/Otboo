package com.codeit.otboo.global.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class KakaoLocalUtil {

    private static final String KAKAO_REST_API_KEY = System.getenv("KAKAO_REST_API_KEY");
    private static final String COORD2REGION_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

    /**
     * 위경도 → 행정동/법정동 주소 4단계 리스트 반환
     * 순서: [시/도, 구/군, 동/읍면, 리/통]
     *
     * @param longitude 경도 (x)
     * @param latitude  위도 (y)
     * @param regionType 행정동(H) 또는 법정동(B) 우선순위
     * @return List<String> - 4개 요소 (없으면 빈 문자열 "")
     */
    public static List<String> getAddressLevels(double longitude, double latitude, KakaoRegionType regionType) {
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
    private static Optional<JsonObject> getRegionInfo(double x, double y, KakaoRegionType regionType) {
        try {
            StringBuilder urlBuilder = new StringBuilder(COORD2REGION_URL);
            urlBuilder.append("?x=").append(URLEncoder.encode(String.valueOf(x), StandardCharsets.UTF_8));
            urlBuilder.append("&y=").append(URLEncoder.encode(String.valueOf(y), StandardCharsets.UTF_8));

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "KakaoAK " + KAKAO_REST_API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("카카오 API 응답 코드: " + responseCode);
                return Optional.empty();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            conn.disconnect();

            // JSON 파싱
            JsonObject root = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray documents = root.getAsJsonArray("documents");

            if (documents == null || documents.isEmpty()) {
                return Optional.empty();
            }

            String targetCode = regionType != null ? regionType.getCode() : null;

            if (targetCode != null) {
                for (int i = 0; i < documents.size(); i++) {
                    JsonObject doc = documents.get(i).getAsJsonObject();
                    String docType = doc.get("region_type").getAsString();
                    if (targetCode.equals(docType)) {
                        return Optional.of(doc);
                    }
                }
            }

            // 기본: 행정동 결과 값 반환
            return Optional.of(documents.get(1).getAsJsonObject());

        } catch (Exception e) {
            System.err.println("카카오 지역 조회 실패: " + e.getMessage());
            return Optional.empty();
        }
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
