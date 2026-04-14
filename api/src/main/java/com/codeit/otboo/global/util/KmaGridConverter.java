package com.codeit.otboo.global.util;

import org.springframework.stereotype.Component;

// 기상청 참고 자료 기반 작성
// https://apihub.kma.go.kr/getAttachFile.do?fileName=8.%EC%9C%84%EC%84%B1%EC%9E%90%EB%A3%8C%20%EA%B8%B0%EB%B3%B8%20%EA%B4%80%EC%B8%A1%EC%9E%90%EB%A3%8C%20%EA%B2%BD%EB%9F%89%ED%99%94%20%EC%A1%B0%ED%9A%8C%EC%84%9C%EB%B9%84%EC%8A%A4%20API%20%ED%99%9C%EC%9A%A9%EA%B0%80%EC%9D%B4%EB%93%9C.docx
@Component
public class KmaGridConverter {
    private static final double RE     = 6371.00877;
    private static final double GRID   = 5.0;
    private static final double SLAT1  = 30.0;
    private static final double SLAT2  = 60.0;
    private static final double OLON   = 126.0;
    private static final double OLAT   = 38.0;
    private static final double XO     = 210 / GRID;
    private static final double YO     = 675 / GRID;

    /**
     * 위경도 → 기상청 격자 좌표 (nx, ny) 변환
     * @param lat 위도 (예: 37.5344)
     * @param lon 경도 (예: 126.8216)
     * @return GridResult (nx, ny)
     */
    public GridResult convertToGrid(double lat, double lon) {
        double DEGRAD = Math.PI / 180.0;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        // 위도, 경도 -> 격자 좌표 변환
        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);

        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI)  theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        double x = ra * Math.sin(theta) + XO;
        double y = ro - ra * Math.cos(theta) + YO;

        int nx = (int) (x + 1.5);
        int ny = (int) (y + 1.5);

        return new GridResult(nx, ny);
    }

    // 결과 반환용 클래스
    public record GridResult (
            int nx,
            int ny
    ){}
}
