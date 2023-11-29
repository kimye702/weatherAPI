package com.example.weatherapi.finedust.model.airquality;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Body {
    @SerializedName("items")
    private List<MeasuredValue> measuredValues;

    @SerializedName("numOfRows")
    private Integer numOfRows;

    @SerializedName("pageNo")
    private Integer pageNo;

    @SerializedName("totalCount")
    private Integer totalCount;

    public List<MeasuredValue> getMeasuredValues() {
        return measuredValues;
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
}
