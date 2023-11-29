package com.example.weatherapi.finedust.model.monitoringstation;

import com.google.gson.annotations.SerializedName;

public class MonitoringStationsResponse {
    @SerializedName("response")
    private Response response;

    public MonitoringStationsResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
