package com.example.weatherapi.finedust.model.monitoringstation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Body {
    @SerializedName("items")
    private List<MonitoringStation> monitoringStations;
    @SerializedName("numOfRows")
    private Integer numOfRows;
    @SerializedName("pageNo")
    private Integer pageNo;
    @SerializedName("totalCount")
    private Integer totalCount;

    public Body(List<MonitoringStation> monitoringStations, Integer numOfRows, Integer pageNo, Integer totalCount) {
        this.monitoringStations = monitoringStations;
        this.numOfRows = numOfRows;
        this.pageNo = pageNo;
        this.totalCount = totalCount;
    }

    public List<MonitoringStation> getMonitoringStations() {
        return monitoringStations;
    }

    public Integer getNumOfRows() {
        return numOfRows;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setMonitoringStations(List<MonitoringStation> monitoringStations) {
        this.monitoringStations = monitoringStations;
    }

    public void setNumOfRows(Integer numOfRows) {
        this.numOfRows = numOfRows;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
