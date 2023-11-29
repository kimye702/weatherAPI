package com.example.weatherapi.weather.model;

import java.time.LocalDate;
import java.util.Calendar;

// 중기 예보 정보
public class MedWeatherModel {
    private LocalDate date;
    private String low_temp; // 최저 기온
    private String high_temp; // 최고 기온
    private String am_rain; // 오전 강수량
    private String pm_rain; // 오후 강수량
    private String rain; // 1시간 강수량
    private String am_sky; // 오전 하늘 상태
    private String pm_sky; // 오후 하늘 상태
    private String sky; // 하늘 상태

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLow_temp() {
        return low_temp;
    }

    public void setLow_temp(String low_temp) {
        this.low_temp = low_temp;
    }

    public String getHigh_temp() {
        return high_temp;
    }

    public void setHigh_temp(String high_temp) {
        this.high_temp = high_temp;
    }

    public String getAm_rain() {
        return am_rain;
    }

    public void setAm_rain(String am_rain) {
        this.am_rain = am_rain;
    }

    public String getPm_rain() {
        return pm_rain;
    }

    public void setPm_rain(String pm_rain) {
        this.pm_rain = pm_rain;
    }

    public String getRain() {
        return rain;
    }

    public void setRain(String rain) {
        this.rain = rain;
    }

    public String getAm_sky() {
        return am_sky;
    }

    public void setAm_sky(String am_sky) {
        this.am_sky = am_sky;
    }

    public String getPm_sky() {
        return pm_sky;
    }

    public void setPm_sky(String pm_sky) {
        this.pm_sky = pm_sky;
    }

    public String getSky() {
        return sky;
    }

    public void setSky(String sky) {
        this.sky = sky;
    }
}
