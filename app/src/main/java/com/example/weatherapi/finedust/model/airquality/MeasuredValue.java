package com.example.weatherapi.finedust.model.airquality;

import com.google.gson.annotations.SerializedName;

public class MeasuredValue {
    private Object coFlag;
    private int coGrade;
    private String coValue;
    private String dataTime;
    private int khaiGrade;
    private String khaiValue;
    private String mangName;
    private Object no2Flag;
    private int no2Grade;
    private String no2Value;
   private Object o3Flag;
    private int o3Grade;
    private String o3Value;
    private Object pm10Flag;
    private int pm10Grade;
    private int pm10Grade1h;
    private String pm10Value;
    private String pm10Value24;
    private Object pm25Flag;
    private int pm25Grade;
    private int pm25Grade1h;
    private String pm25Value;
    private String pm25Value24;
    private Object so2Flag;
    private int so2Grade;
    private String so2Value;

    public MeasuredValue(Object coFlag, int coGrade, String coValue, String dataTime, int khaiGrade, String khaiValue, String mangName, Object no2Flag, int no2Grade, String no2Value, Object o3Flag, int o3Grade, String o3Value, Object pm10Flag, int pm10Grade, int pm10Grade1h, String pm10Value, String pm10Value24, Object pm25Flag, int pm25Grade, int pm25Grade1h, String pm25Value, String pm25Value24, Object so2Flag, int so2Grade, String so2Value) {
        this.coFlag = coFlag;
        this.coGrade = coGrade;
        this.coValue = coValue;
        this.dataTime = dataTime;
        this.khaiGrade = khaiGrade;
        this.khaiValue = khaiValue;
        this.mangName = mangName;
        this.no2Flag = no2Flag;
        this.no2Grade = no2Grade;
        this.no2Value = no2Value;
        this.o3Flag = o3Flag;
        this.o3Grade = o3Grade;
        this.o3Value = o3Value;
        this.pm10Flag = pm10Flag;
        this.pm10Grade = pm10Grade;
        this.pm10Grade1h = pm10Grade1h;
        this.pm10Value = pm10Value;
        this.pm10Value24 = pm10Value24;
        this.pm25Flag = pm25Flag;
        this.pm25Grade = pm25Grade;
        this.pm25Grade1h = pm25Grade1h;
        this.pm25Value = pm25Value;
        this.pm25Value24 = pm25Value24;
        this.so2Flag = so2Flag;
        this.so2Grade = so2Grade;
        this.so2Value = so2Value;
    }

    public Object getCoFlag() {
        return coFlag;
    }

    public int getCoGrade() {
        return coGrade;
    }

    public String getCoValue() {
        return coValue;
    }

    public String getDataTime() {
        return dataTime;
    }

    public int getKhaiGrade() {
        return khaiGrade;
    }

    public String getKhaiValue() {
        return khaiValue;
    }

    public String getMangName() {
        return mangName;
    }

    public Object getNo2Flag() {
        return no2Flag;
    }

    public int getNo2Grade() {
        return no2Grade;
    }

    public String getNo2Value() {
        return no2Value;
    }

    public Object getO3Flag() {
        return o3Flag;
    }

    public int getO3Grade() {
        return o3Grade;
    }

    public String getO3Value() {
        return o3Value;
    }

    public Object getPm10Flag() {
        return pm10Flag;
    }

    public int getPm10Grade() {
        return pm10Grade;
    }

    public int getPm10Grade1h() {
        return pm10Grade1h;
    }

    public String getPm10Value() {
        return pm10Value;
    }

    public String getPm10Value24() {
        return pm10Value24;
    }

    public Object getPm25Flag() {
        return pm25Flag;
    }

    public int getPm25Grade() {
        return pm25Grade;
    }

    public int getPm25Grade1h() {
        return pm25Grade1h;
    }

    public String getPm25Value() {
        return pm25Value;
    }

    public String getPm25Value24() {
        return pm25Value24;
    }

    public Object getSo2Flag() {
        return so2Flag;
    }

    public int getSo2Grade() {
        return so2Grade;
    }

    public String getSo2Value() {
        return so2Value;
    }

    public Grade getGrade(int value) {
        if (value == 1) {
            return Grade.GOOD;
        } else if (value == 2) {
            return Grade.NORMAL;
        } else if (value == 3) {
            return Grade.BAD;
        } else if (value == 4) {
            return Grade.AWFUL;
        } else {
            return Grade.UNKNOWN;
        }
    }
}