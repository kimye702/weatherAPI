package com.example.weatherapi.weather.model;

// 단기 예보 정보를 담는 데이터 클래스
public class ShortWeatherModel {
    private String fcstTime; // 예보 시각
    private String rainPercent; // 강수확률
    private String rainType; // 강수 형태
    private String hourRain; // 1시간 강수량
    private String humidity; // 습도
    private String sky;      // 하늘 상태
    private String hourTemp; // 1시간 기온
    private String lowTemp; // 일 최저기온
    private String highTemp; // 일 최고기온
    private String windSpeed; // 풍속

    public String getFcstTime() {
        return fcstTime;
    }

    public void setFcstTime(String fcstTime) {
        this.fcstTime = fcstTime;
    }

    public String getRainPercent() {
        return rainPercent;
    }

    public void setRainPercent(String rainPercent) {
        this.rainPercent = rainPercent;
    }

    public String getRainType() {
        return rainType;
    }

    public void setRainType(String rainType) {
        this.rainType = rainType;
    }

    public String getHourRain() {
        return hourRain;
    }

    public void setHourRain(String hourRain) {
        this.hourRain = hourRain;
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

    public String getHourTemp() {
        return hourTemp;
    }

    public void setHourTemp(String hourTemp) {
        this.hourTemp = hourTemp;
    }

    public String getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }
}
