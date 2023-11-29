package com.example.weatherapi.weather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.R;
import com.example.weatherapi.weather.adapter.VeryShortWeatherDetailAdapter;
import com.example.weatherapi.weather.model.VeryShortWeatherModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class VeryShortWeatherActivity extends AppCompatActivity {


    private TextView tv_date;
    private TextView tv_weather;
    private TextView tv_temprature;
    private RecyclerView rv_very_short;
    private RecyclerView rv_hour_simple;
    private TextView locationText;
    private Point curPoint;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_very_short_weather);

        rv_very_short = findViewById(R.id.rv_very_short);
        rv_very_short.setLayoutManager(new LinearLayoutManager(this));

        // 현재 시간을 가져옵니다.
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        // 요일을 포함한 날짜를 포맷팅합니다.
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 EEEE", Locale.getDefault());
        String formattedDate = sdf.format(currentTime.getTime());

        // 시간을 오전 또는 오후로 변환합니다.
        String amPm;
        if (hour >= 12) {
            amPm = "오후";
            hour -= 12;
        } else {
            amPm = "오전";
        }

//        tv_date.setText(formattedDate);

        Intent intent = getIntent();

        System.out.println("위치 정보 x : "+intent.getStringExtra("x")+" y : "+intent.getStringExtra("y"));
        setWeather(intent.getStringExtra("x"),intent.getStringExtra("y"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWeather(String nx, String ny) {
        // 현재 날짜와 시간
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDate currentDate = currentDateTime.toLocalDate();

        // 날짜 형식 지정
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault());
        String base_date = currentDate.format(dateFormatter);

        // 시간 형식 지정
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH", Locale.getDefault());
        String timeH = currentDateTime.format(timeFormatter);
        String timeM = currentDateTime.format(timeFormatter);
        String base_time = getBaseTime(timeH, timeM);

        if ("00".equals(timeH) && "2330".equals(base_time)) {
            currentDate = currentDate.minusDays(1);
            base_date = currentDate.format(dateFormatter);
        }

        Call<WEATHER> very_short_call = ApiObject.retrofitService.GetVeryShortTermWeather(60, 1, "JSON", base_date, base_time, nx, ny);

        // 초단기 예보 정보 받아오기
        very_short_call.enqueue(new retrofit2.Callback<WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    List<ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;

                    VeryShortWeatherModel[] weatherArr = new VeryShortWeatherModel[6];

                    for (int i = 0; i < 6; i++) {
                        weatherArr[i] = new VeryShortWeatherModel();
                    }

                    int index = 0;
                    int totalCount = response.body().response.body.totalCount - 1;
//                    System.out.println("초단기예보 totalCount : "+totalCount);
                    for (int i = 0; i <= totalCount; i++) {
                        index %= 6;
                        switch (items.get(i).category) {
                            case "PTY": // 강수형태
                                weatherArr[index].setRainType(items.get(i).fcstValue);
                                break;
                            case "REH": // 습도
                                weatherArr[index].setHumidity(items.get(i).fcstValue);
                                break;
                            case "SKY": // 하늘상태
                                weatherArr[index].setSky(items.get(i).fcstValue);
                                break;
                            case "T1H": // 기온
                                weatherArr[index].setTemp(items.get(i).fcstValue);
                                break;
                            case "RN1": // 1시간 강수량
                                weatherArr[index].setHourRain(items.get(i).fcstValue);
                                break;
                            case "WSD": // 풍속
                                weatherArr[index].setWindSpeed(items.get(i).fcstValue);
                                break;
                            default:
                                break;
                        }
                        index++;
                    }

                    for (int i = 0; i <= 5; i++) {
                        String fcstTime = items.get(i).fcstTime;
                        String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
                        weatherArr[i].setFcstTime(formattedTime);
                    }

                    String sky = weatherArr[0].getSky();
                    String weather;
                    switch (sky) {
                        case "1":
                            weather = "맑음";
                            break;
                        case "3":
                            weather = "구름 많음";
                            break;
                        case "4":
                            weather = "흐림";
                            break;
                        default:
                            weather = "오류 rainType : " + sky;
                            break;
                    }

//                    tv_weather.setText(weather);
//                    tv_temprature.setText(weatherArr[0].getTemp());


                    rv_very_short.setAdapter(new VeryShortWeatherDetailAdapter(weatherArr));

                    Toast.makeText(getApplicationContext(), "초단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private String getBaseTime(String h, String m) {
        String result;

        if (Integer.parseInt(m) < 45) {
            if (h.equals("00")) {
                result = "2330";
            } else {
                int resultH = Integer.parseInt(h) - 1;
                if (resultH < 10) {
                    result = "0" + resultH + "30";
                } else {
                    result = resultH + "30";
                }
            }
        } else {
            result = h + "30";
        }

        return result;
    }
}
