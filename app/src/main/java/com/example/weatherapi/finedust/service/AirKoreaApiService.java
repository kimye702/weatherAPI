package com.example.weatherapi.finedust.service;


import com.example.weatherapi.BuildConfig;
import com.example.weatherapi.finedust.model.airquality.AirQualityResponse;
import com.example.weatherapi.finedust.model.monitoringstation.MonitoringStationsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirKoreaApiService {

    @GET("B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList" +
            "?serviceKey="+BuildConfig.AIR_KOREA_SERVICE_KEY+
            "&returnType=json")
    Call<MonitoringStationsResponse> getNearbyMonitoringStation(
            @Query("tmX") double tmX,
            @Query("tmY") double tmY
    );

    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty"  +
            "?serviceKey="+BuildConfig.AIR_KOREA_SERVICE_KEY+
            "&returnType=json"+
            "&dataTerm=DAILY"+
            "&ver=1.3")
    Call<AirQualityResponse> getRealtimeAirQualities(
            @Query("stationName") String stationName
    );
}
