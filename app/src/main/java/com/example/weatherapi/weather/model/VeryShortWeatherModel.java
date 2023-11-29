package com.example.weatherapi.weather.model;

// 초단기 예보 정보
public class VeryShortWeatherModel {
    private String fcstDate;
    private String fcstTime; // 예보 시각
    private String temp;     // 기온
    private String hourRain; // 1시간 강수량
    private String sky;      // 하늘 상태
    private String humidity; // 습도
    private String rainType; // 강수 형태
    private String windSpeed; // 풍속

    public String getFcstDate() {
        return fcstDate;
    }

    public void setFcstDate(String fcstDate) {
        this.fcstDate = fcstDate;
    }

    public String getHourRain() {
        return hourRain;
    }

    public void setHourRain(String hourRain) {
        this.hourRain = hourRain;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getRainType() {
        return rainType;
    }

    public void setRainType(String rainType) {
        this.rainType = rainType;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getSky() {
        return sky;
    }

    public void setSky(String sky) {
        this.sky = sky;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getFcstTime() {
        return fcstTime;
    }

    public void setFcstTime(String fcstTime) {
        this.fcstTime = fcstTime;
    }
}
