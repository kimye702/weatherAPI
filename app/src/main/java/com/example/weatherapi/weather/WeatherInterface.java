package com.example.weatherapi.weather;

import com.example.weatherapi.BuildConfig;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherInterface {

    // 초단기 예보
    @GET("VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey="+ BuildConfig.AIR_KOREA_SERVICE_KEY)
    Call<WEATHER> GetVeryShortTermWeather(@Query("numOfRows") int num_of_rows,
                            @Query("pageNo") int page_no,
                            @Query("dataType") String data_type,
                            @Query("base_date") String base_date,
                            @Query("base_time") String base_time,
                            @Query("nx") String nx,
                            @Query("ny") String ny);

    // 단기 예보
    @GET("VilageFcstInfoService_2.0/getVilageFcst?serviceKey="+ BuildConfig.AIR_KOREA_SERVICE_KEY)
    Call<WEATHER> GetShortTermWeather(@Query("numOfRows") int num_of_rows,
                            @Query("pageNo") int page_no,
                            @Query("dataType") String data_type,
                            @Query("base_date") String base_date,
                            @Query("base_time") String base_time,
                            @Query("nx") String nx,
                            @Query("ny") String ny);

    // 중기 예보 - 기온
    @GET("MidFcstInfoService/getMidTa?serviceKey="+ BuildConfig.AIR_KOREA_SERVICE_KEY)
    Call<med_TEMP> GetMediumTermTemp(@Query("numOfRows") int num_of_rows,
                            @Query("pageNo") int page_no,
                            @Query("dataType") String data_type,
                            @Query("regId") String base_date, // 예보구역코드
                            @Query("tmFc") String base_time); // 발표 시각

    // 중기 예보 - 육상 예보
    @GET("MidFcstInfoService/getMidLandFcst?serviceKey="+ BuildConfig.AIR_KOREA_SERVICE_KEY)
    Call<med_WEATHER> GetMediumTermWeather(@Query("numOfRows") int num_of_rows,
                            @Query("pageNo") int page_no,
                            @Query("dataType") String data_type,
                            @Query("regId") String base_date,
                            @Query("tmFc") String base_time);
}

class WEATHER {
    public RESPONSE response;
}

class RESPONSE {
    public HEADER header;
    public BODY body;
}

class HEADER {
    public int resultCode;
    public String resultMsg;
}

class BODY {
    public String dataType;
    public ITEMS items;
    public int totalCount;
}

class ITEMS {
    public List<ITEM> item;
}

class ITEM {
    String baseDate;
    String baseTime;
    String category;
    String fcstDate;
    String fcstTime;
    String fcstValue;
}

class med_WEATHER {
    public med_weather_RESPONSE response;
}

class med_weather_RESPONSE {
    public HEADER header;
    public med_weather_BODY body;
}


class med_weather_BODY {
    public String dataType;
    public med_weather_ITEMS items;
    public int totalCount;
}

class med_weather_ITEMS {
    public List<med_weather_ITEM> item;
}

class med_weather_ITEM {
    String rnSt3Am; // 오전 강수 확률
    String rnSt3Pm; // 오후 강수 확률
    String rnSt4Am;
    String rnSt4Pm;
    String rnSt5Am;
    String rnSt5Pm;
    String rnSt6Am;
    String rnSt6Pm;
    String rnSt7Am;
    String rnSt7Pm;
    String rnSt8; // 강수 확률
    String rnSt9;
    String rnSt10;
    String wf3Am; // 오전 하늘
    String wf3Pm; // 오후 하늘
    String wf4Am;
    String wf4Pm;
    String wf5Am;
    String wf5Pm;
    String wf6Am;
    String wf6Pm;
    String wf7Am;
    String wf7Pm;
    String wf8;
    String wf9;
    String wf10;

}

class med_TEMP {
    public med_temp_RESPONSE response;
}

class med_temp_RESPONSE {
    public HEADER header;
    public med_temp_BODY body;
}


class med_temp_BODY {
    public String dataType;
    public med_temp_ITEMS items;
    public int totalCount;
}

class med_temp_ITEMS {
    public List<med_temp_ITEM> item;
}

class med_temp_ITEM {
    String taMin3; // 최저 기온
    String taMax3; // 최고 기온
    String taMin4;
    String taMax4;
    String taMin5;
    String taMax5;
    String taMin6;
    String taMax6;
    String taMin7;
    String taMax7;
    String taMin8;
    String taMax8;
    String taMin9;
    String taMax9;
    String taMin10;
    String taMax10;
}

class ApiObject {
    public static WeatherInterface retrofitService = new Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1360000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherInterface.class);
}
