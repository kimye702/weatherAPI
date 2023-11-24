package com.example.weatherapi.finedust.model.tmcoordinates;

import com.google.gson.annotations.SerializedName;

public class Meta {
    @SerializedName("total_count")
    private Integer totalCount;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
