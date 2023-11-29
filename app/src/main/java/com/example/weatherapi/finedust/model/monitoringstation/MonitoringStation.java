package com.example.weatherapi.finedust.model.monitoringstation;

import com.google.gson.annotations.SerializedName;

public class MonitoringStation {
    @SerializedName("addr")
    private String addr;
    @SerializedName("stationName")
    private String stationName;
    @SerializedName("tm")
    private Double tm;

    public MonitoringStation(String addr, String stationName, Double tm) {
        this.addr = addr;
        this.stationName = stationName;
        this.tm = tm;
    }

    public String getAddr() {
        return addr;
    }

    public String getStationName() {
        return stationName;
    }

    public Double getTm() {
        return tm;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public void setTm(Double tm) {
        this.tm = tm;
    }
}
