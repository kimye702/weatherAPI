package com.example.weatherapi.weather.util;

import android.graphics.Point;

public class Conversion {
    // baseTime 설정하기
    public String getBaseTime(String h, String m) {
        String result = "";

        // 45분 전이면
        if (Integer.parseInt(m) < 45) {
            // 0시면 2330
            if (h.equals("00")) {
                result = "2330";
            }
            // 아니면 1시간 전 날씨 정보 부르기
            else {
                int resultH = Integer.parseInt(h) - 1;
                // 1자리면 0 붙여서 2자리로 만들기
                if (resultH < 10) {
                    result = "0" + resultH + "30";
                }
                // 2자리면 그대로
                else {
                    result = resultH + "30";
                }
            }
        }
        // 45분 이후면 바로 정보 받아오기
        else {
            result = h + "30";
        }

        return result;
    }

    // 위경도를 기상청에서 사용하는 격자 좌표로 변환
    public static Point dfs_xy_conv(double v1, double v2) {
        double RE = 6371.00877;     // 지구 반경(km)
        double GRID = 5.0;          // 격자 간격(km)
        double SLAT1 = 30.0;        // 투영 위도1(degree)
        double SLAT2 = 60.0;        // 투영 위도2(degree)
        double OLON = 126.0;        // 기준점 경도(degree)
        double OLAT = 38.0;         // 기준점 위도(degree)
        int XO = 43;                // 기준점 X좌표(GRID)
        int YO = 136;               // 기준점 Y좌표(GRID)
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

        double ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = v2 * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int x = (int) (ra * Math.sin(theta) + XO + 0.5);
        int y = (int) (ro - ra * Math.cos(theta) + YO + 0.5);

        return new Point(x, y);
    }
}
