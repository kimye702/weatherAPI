package com.example.weatherapi.weather.model;

import java.time.LocalDate;
import java.util.Calendar;

// 리사이클러뷰에 들어갈 내용
public class MedWeatherModel {
    private LocalDate date;
    private String low_temp;
    private String high_temp;
    private String am_rain;
    private String pm_rain;
    private String rain;
    private String am_sky;
    private String pm_sky;
    private String sky;

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
