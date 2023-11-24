package com.example.weatherapi.finedust;

import android.os.AsyncTask;


import com.example.weatherapi.finedust.model.airquality.MeasuredValue;

import java.io.IOException;

public class FetchAirQualityDataTask extends AsyncTask<Void, Void, MeasuredValue> {
    private final FetchAirQualityDataCallback callback;

    public FetchAirQualityDataTask(FetchAirQualityDataCallback callback) {
        this.callback = callback;
    }

    @Override
    protected MeasuredValue doInBackground(Void... voids) {
        // 백그라운드 스레드에서 미세먼지 데이터 가져오기
        try {
            return Repository.getLatestAirQualityData("your_station_name_here");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(MeasuredValue measuredValue) {
        // 백그라운드 작업이 완료되면 UI 업데이트를 수행
        if (measuredValue != null) {
            try {
                callback.onFetchSuccess(measuredValue);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            callback.onFetchFailure();
        }
    }

    public interface FetchAirQualityDataCallback {
        void onFetchSuccess(MeasuredValue measuredValue) throws IOException;
        void onFetchFailure();
    }
}
