package com.example.weatherapi.finedust.service;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.weatherapi.BuildConfig;
import com.example.weatherapi.finedust.model.tmcoordinates.TmCoordinatesResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface KakaoLocalApiService {

    @Headers("Authorization: KakaoAK "+BuildConfig.KAKAO_API_KEY)
    @GET("v2/local/geo/transcoord.json?output_coord=TM")
    Call<TmCoordinatesResponse> getTmCoordinates(
        @Query("x") double longitude, // 경도
        @Query("y") double latitude // 위도
    );

    @RequiresApi(api = Build.VERSION_CODES.N)
    default CompletableFuture<Response<TmCoordinatesResponse>> getTmCoordinatesAsync(double longitude, double latitude) {
        CompletableFuture<Response<TmCoordinatesResponse>> future = new CompletableFuture<>();

        Call<TmCoordinatesResponse> call = getTmCoordinates(longitude, latitude);
        call.enqueue(new retrofit2.Callback<TmCoordinatesResponse>() {
            @Override
            public void onResponse(Call<TmCoordinatesResponse> call, Response<TmCoordinatesResponse> response) {
                future.complete(response);
            }

            @Override
            public void onFailure(Call<TmCoordinatesResponse> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Headers("Authorization: KakaoAK " + BuildConfig.KAKAO_API_KEY)
    @GET("v2/local/geo/transcoord.json?input_coord=WGS84&output_coord=TM")
    default Response<TmCoordinatesResponse> createTmCoordinatesCall(
        @Query("x") double longitude,
        @Query("y") double latitude
    ) {
        try {
            return getTmCoordinatesAsync(longitude, latitude).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}