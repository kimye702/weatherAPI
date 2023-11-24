package com.example.weatherapi.finedust;

import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.weatherapi.BuildConfig;

import java.io.IOException;
import java.util.Comparator;

import com.example.weatherapi.finedust.model.airquality.AirQualityResponse;
import com.example.weatherapi.finedust.model.airquality.MeasuredValue;
import com.example.weatherapi.finedust.model.monitoringstation.MonitoringStation;
import com.example.weatherapi.finedust.model.monitoringstation.MonitoringStationsResponse;
import com.example.weatherapi.finedust.model.tmcoordinates.Document;
import com.example.weatherapi.finedust.model.tmcoordinates.TmCoordinatesResponse;
import com.example.weatherapi.finedust.service.AirKoreaApiService;
import com.example.weatherapi.finedust.service.KakaoLocalApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {

    private static KakaoLocalApiService kakaoLocalApiService =
            new Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create(KakaoLocalApiService.class);

    private static AirKoreaApiService airKoreaApiService =
            new Retrofit.Builder()
            .baseUrl(Url.AIR_KOREA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create(AirKoreaApiService.class);

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getTMLocation(double longitude, double latitude)
    {
        TmCoordinatesResponse tmCoordinatesResponse = kakaoLocalApiService.createTmCoordinatesCall(longitude, latitude).body();

        if (tmCoordinatesResponse == null)
            return "tmCoordinatesResponse is null";

        if (tmCoordinatesResponse != null && tmCoordinatesResponse.getDocuments() != null && !tmCoordinatesResponse.getDocuments().isEmpty()) {
            Document tmCoordinates = tmCoordinatesResponse.getDocuments().get(0);
            Double tmX = tmCoordinates.getX();
            Double tmY = tmCoordinates.getY();

            if (tmX != null && tmY != null)
                return "tmX : " + tmX+" tmY : "+tmY;
        }
        return "getTMLocation() error!!!";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static MonitoringStation getNearbyMonitoringStation(double latitude, double longitude) throws IOException {
        TmCoordinatesResponse tmCoordinatesResponse = kakaoLocalApiService.getTmCoordinates(longitude, latitude).execute().body();
        if (tmCoordinatesResponse != null && tmCoordinatesResponse.getDocuments() != null && !tmCoordinatesResponse.getDocuments().isEmpty()) {
            Document tmCoordinates = tmCoordinatesResponse.getDocuments().get(0);
            Double tmX = tmCoordinates.getX();
            Double tmY = tmCoordinates.getY();

            if (tmX != null && tmY != null) {
                MonitoringStationsResponse monitoringStationsResponse = airKoreaApiService.getNearbyMonitoringStation(tmX, tmY).execute().body();
                if (monitoringStationsResponse != null && monitoringStationsResponse.getResponse() != null
                        && monitoringStationsResponse.getResponse().getBody() != null && monitoringStationsResponse.getResponse().getBody().getMonitoringStations() != null) {
                    return monitoringStationsResponse.getResponse().getBody().getMonitoringStations()
                            .stream()
                            .min(Comparator.comparingDouble(station -> station.getTm() != null ? station.getTm() : Double.MAX_VALUE))
                            .orElse(null);
                }
            }
        }
        return null;
    }

    public interface NearbyMonitoringStationCallback {
        void onMonitoringStationResult(MonitoringStation station);
    }

    public static void getNearbyMonitoringStation(double latitude, double longitude, NearbyMonitoringStationCallback callback) {
        new NearbyMonitoringStationTask(callback).execute(latitude, longitude);
    }

    private static class NearbyMonitoringStationTask extends AsyncTask<Double, Void, MonitoringStation> {
        private NearbyMonitoringStationCallback callback;

        NearbyMonitoringStationTask(NearbyMonitoringStationCallback callback) {
            this.callback = callback;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected MonitoringStation doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];

            try {
                TmCoordinatesResponse tmCoordinatesResponse = kakaoLocalApiService.getTmCoordinates(longitude, latitude).execute().body();
                if (tmCoordinatesResponse != null && tmCoordinatesResponse.getDocuments() != null && !tmCoordinatesResponse.getDocuments().isEmpty()) {
                    Document tmCoordinates = tmCoordinatesResponse.getDocuments().get(0);
                    Double tmX = tmCoordinates.getX();
                    Double tmY = tmCoordinates.getY();

                    if (tmX != null && tmY != null) {
                        MonitoringStationsResponse monitoringStationsResponse = airKoreaApiService.getNearbyMonitoringStation(tmX, tmY).execute().body();
                        if (monitoringStationsResponse != null && monitoringStationsResponse.getResponse() != null
                                && monitoringStationsResponse.getResponse().getBody() != null && monitoringStationsResponse.getResponse().getBody().getMonitoringStations() != null) {
                            return monitoringStationsResponse.getResponse().getBody().getMonitoringStations()
                                    .stream()
                                    .min(Comparator.comparingDouble(station -> station.getTm() != null ? station.getTm() : Double.MAX_VALUE))
                                    .orElse(null);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MonitoringStation result) {
            callback.onMonitoringStationResult(result);
        }
    }

    public static MeasuredValue getLatestAirQualityData(String stationName) throws IOException {
        AirQualityResponse airQualityResponse = airKoreaApiService.getRealtimeAirQualities(stationName).execute().body();
        if (airQualityResponse != null && airQualityResponse.getResponse() != null
                && airQualityResponse.getResponse().getBody() != null
                && airQualityResponse.getResponse().getBody().getMeasuredValues() != null
                && !airQualityResponse.getResponse().getBody().getMeasuredValues().isEmpty()) {
            return airQualityResponse.getResponse().getBody().getMeasuredValues().get(0);
        }
        return null;
    }

    public static String getLatestAirQualityDataError(String stationName) throws IOException {
        AirQualityResponse airQualityResponse = airKoreaApiService.getRealtimeAirQualities(stationName).execute().body();
        if (airQualityResponse != null && airQualityResponse.getResponse() != null
                && airQualityResponse.getResponse().getBody() != null
                && airQualityResponse.getResponse().getBody().getMeasuredValues() != null
                && !airQualityResponse.getResponse().getBody().getMeasuredValues().isEmpty()) {
            return airQualityResponse.getResponse().getHeader().getResultCode();
        }
        return null;
    }

    private static KakaoLocalApiService getKakaoLocalApiService() {
        return new Retrofit.Builder()
                .baseUrl(Url.KAKAO_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(buildHttpClient())
                .build()
                .create(KakaoLocalApiService.class);
    }

    private static AirKoreaApiService getAirKoreaApiService() {
        return new Retrofit.Builder()
                .baseUrl(Url.AIR_KOREA_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(buildHttpClient())
                .build()
                .create(AirKoreaApiService.class);
    }

    private static OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(
                        BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE))
                .build();
    }
}
