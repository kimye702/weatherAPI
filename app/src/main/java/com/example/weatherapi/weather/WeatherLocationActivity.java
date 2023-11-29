package com.example.weatherapi.weather;

import static java.lang.Math.pow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.R;
import com.example.weatherapi.finedust.FineDustActivity;
import com.example.weatherapi.weather.adapter.DailyWeatherSimpleAdapter;
import com.example.weatherapi.weather.adapter.HourWeatherSimpleAdapter;
import com.example.weatherapi.weather.adapter.VeryShortWeatherDetailAdapter;
import com.example.weatherapi.weather.model.MedWeatherModel;
import com.example.weatherapi.weather.model.ShortWeatherModel;
import com.example.weatherapi.weather.model.VeryShortWeatherModel;
import com.example.weatherapi.weather.util.Conversion;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import retrofit2.Call;
import retrofit2.Response;

public class WeatherLocationActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1981;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2981;
    private static final String[] PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private TextView tv_date;
    private TextView tv_today_weather;
    private TextView tv_now_temp;
    private TextView tv_perceived_temp;
    private TextView tv_highest_temp;
    private TextView tv_lowest_temp;
    private Button btn_finedust;
    private RecyclerView rv_very_short;
    private RecyclerView rv_hour_simple;
    private RecyclerView rv_daily_simple;
    private TextView locationText;
    private Point curPoint;

    // 중기 예보에 필요한 쿼리 변수
    private String weather_region_code;
    private String temp_region_code;

    // 네비게이션 드로어
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView nav_icon;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // 툴바와 드로어를 연동
        actionBarDrawerToggle = new ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // 툴바에 네비게이션 버튼 표시
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // 네비게이션 메뉴 아이템 클릭 리스너 설정
        navigationView.setNavigationItemSelectedListener(item -> {
            // 클릭된 아이템에 대한 동작 처리
            switch (item.getItemId()) {
                case R.id.nav_item1:
                    // 네비게이션 메뉴 1 선택 시 동작
                    break;
                case R.id.nav_item2:
                    // 네비게이션 메뉴 2 선택 시 동작
                    break;
                case R.id.nav_item3:
                    break;
                // 다른 메뉴 아이템들에 대한 처리 추가
            }

            // 네비게이션 드로어를 닫음
            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });

        nav_icon = findViewById(R.id.nav_icon);

        nav_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNavigationDrawer();
            }
        });


        init();
        checkLocation();
        requestLocation();

        tv_date = findViewById(R.id.tv_date);
        tv_today_weather = findViewById(R.id.tv_today_weather);
        tv_now_temp = findViewById(R.id.tv_now_temp);
        tv_perceived_temp = findViewById(R.id.tv_perceived_temp);
        tv_highest_temp = findViewById(R.id.tv_highest_temp);
        tv_lowest_temp = findViewById(R.id.tv_lowest_temp);
        locationText = findViewById(R.id.tv_location);
        btn_finedust = findViewById(R.id.btn_finedust);
        rv_very_short = findViewById(R.id.rv_very_short);
        rv_very_short.setLayoutManager(new LinearLayoutManager(this));
        rv_hour_simple = findViewById(R.id.rv_hour_simple);
        rv_hour_simple.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_daily_simple = findViewById(R.id.rv_daily_simple);
        rv_daily_simple.setLayoutManager(new LinearLayoutManager(this));

        // 현재 시간을 가져옵니다.
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        int hour = currentTime.getHour();
        int minute = currentTime.getMinute();

        // 요일을 포함한 날짜를 포맷팅합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 EEEE");
        String formattedDate = currentDate.format(formatter);

        tv_date.setText(formattedDate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_finedust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FineDustActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openNavigationDrawer() {
        // DrawerLayout을 열도록 하는 코드 추가
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // ActionBarDrawerToggle의 상태를 동기화
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 화면 방향 전환 등의 변경 시 ActionBarDrawerToggle에 알려줌
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    // ActionBar의 Home 버튼 클릭 이벤트를 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        String base_date2 = currentDate.format(dateFormatter);
        String base_time2 = getBaseTime2();

        if (("00".equals(timeH) || "01".equals(timeH) || "02".equals(timeH)) && "2300".equals(base_time2)) {
            currentDate = currentDate.minusDays(1);
            base_date2 = currentDate.format(dateFormatter);
        }

        Call<WEATHER> very_short_call = ApiObject.retrofitService.GetVeryShortTermWeather(60, 1, "JSON", base_date, base_time, nx, ny);
        Call<WEATHER> short_call = ApiObject.retrofitService.GetShortTermWeather(1000, 1, "JSON", base_date2, base_time2, nx, ny);

        System.out.println("very short base date : " + base_date + "base time : " + base_time);
        System.out.println("short base date : " + base_date2 + "base time2 : " + base_time2);

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

                    // 초단기 예보 정보를 받은 후에 UI 업데이트
                    runOnUiThread(() -> {
                        tv_today_weather.setText(weather);
                        tv_now_temp.setText(weatherArr[0].getTemp());

                        double T = Double.parseDouble(weatherArr[0].getTemp());
                        double V = Double.parseDouble(weatherArr[0].getWindSpeed());
                        double rst = 13.12 + 0.6251 * T - 11.37 * pow(V, 0.16) + 0.3965 * pow(V, 0.16) * T;

                        // 체감온도를 소수점 첫째 자리까지 형식화
                        String perceived_temp = String.format("%.1f", rst);
                        tv_perceived_temp.setText(perceived_temp + "°");

                        rv_very_short.setAdapter(new VeryShortWeatherDetailAdapter(weatherArr));
                    });

                    Toast.makeText(getApplicationContext(), "초단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });

        // 단기예보 api 정보 받아오기
        short_call.enqueue(new retrofit2.Callback<WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    WEATHER weather = response.body();
//                    System.out.println("response : " + response);
                    if (weather != null && weather.response != null && weather.response.body != null && weather.response.body.items != null) {
                        List<ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;

                        int now_hour = Integer.parseInt(timeH);
                        int base_hour = Integer.parseInt(base_time2.substring(0, 2));
                        int diff = now_hour - base_hour;

                        if (diff < 0)
                            diff = 0;

                        System.out.println("now_hour : " + now_hour + " base_hour : " + base_hour + " diff : " + diff);

                        int cnt = 0;
                        int index = 0;
                        int totalCount = response.body().response.body.totalCount - 1;

                        int num = totalCount / 12;

                        ShortWeatherModel[] weatherArr = new ShortWeatherModel[num];

                        for (int i = 0; i < num; i++) {
                            weatherArr[i] = new ShortWeatherModel();
                        }

//                    System.out.println("단기예보 totalCount : " + totalCount);
                        for (int i = 0; i <= totalCount; i++) {
                            switch (items.get(i).category) {
                                case "POP": // 강수확률
                                    weatherArr[index].setRainPercent(items.get(i).fcstValue);
                                    break;
                                case "PTY": // 강수형태
                                    weatherArr[index].setRainType(items.get(i).fcstValue);
                                    break;
                                case "PCP": // 1시간 강수량
                                    weatherArr[index].setHourRain(items.get(i).fcstValue);
                                    break;
                                case "REH": // 습도
                                    weatherArr[index].setHumidity(items.get(i).fcstValue);
                                    break;
                                case "SKY": // 하늘상태
                                    weatherArr[index].setSky(items.get(i).fcstValue);
                                    break;
                                case "TMP": // 1시간 기온
                                    weatherArr[index].setHourTemp(items.get(i).fcstValue);
                                    break;
                                case "TMN": // 일 최저기온
                                    weatherArr[index].setLowTemp(items.get(i).fcstValue);
                                    cnt--;
                                    break;
                                case "TMX": // 일 최고기온
                                    weatherArr[index].setHighTemp(items.get(i).fcstValue);
                                    cnt--;
                                    break;
                                case "WSD": // 풍속
                                    weatherArr[index].setWindSpeed(items.get(i).fcstValue);
                                    break;
                                default:
                                    break;
                            }
                            cnt++;

                            if (cnt % 12 == 0 && !items.get(i).category.equals("TMX") && !items.get(i).category.equals("TMN")) {
//                            System.out.println("fcstTime : "+items.get(i).fcstTime+"category : "+ items.get(i).category);
                                String fcstTime = items.get(i).fcstTime;
                                String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
                                weatherArr[index].setFcstTime(formattedTime);
                                index++;
                            }
                        }

                        String low_temp = null, high_temp = null;

                        for (int i = 0; i < num; i++) {
                            if (weatherArr[i].getLowTemp() == null)
                                continue;
                            else {
                                low_temp = weatherArr[i].getLowTemp();
                            }
                        }

                        for (int i = 0; i < num; i++) {
                            if (weatherArr[i].getHighTemp() == null)
                                continue;
                            else {
                                high_temp = weatherArr[i].getHighTemp();
                            }
                        }

                        String finalLow_temp = low_temp;
                        String finalHigh_temp = high_temp;
                        runOnUiThread(() -> {
                            if (finalLow_temp != null)
                                tv_lowest_temp.setText(finalLow_temp + "°");
                            else
                                tv_lowest_temp.setText("error");

                            if (finalHigh_temp != null)
                                tv_highest_temp.setText(finalHigh_temp + "°");
                            else
                                tv_highest_temp.setText("error");

                            rv_hour_simple.setAdapter(new HourWeatherSimpleAdapter(weatherArr));

                        });

                        Toast.makeText(getApplicationContext(), "단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private String getAmRain(med_weather_ITEM item, int index) {
        switch (index) {
            case 3:
                return item.rnSt3Am;
            case 4:
                return item.rnSt4Am;
            case 5:
                return item.rnSt5Am;
            case 6:
                return item.rnSt6Am;
            case 7:
                return item.rnSt7Am;
        }
        return null;
    }

    private String getPmRain(med_weather_ITEM item, int index) {
        switch (index) {
            case 3:
                return item.rnSt3Pm;
            case 4:
                return item.rnSt4Pm;
            case 5:
                return item.rnSt5Pm;
            case 6:
                return item.rnSt6Pm;
            case 7:
                return item.rnSt7Pm;
        }
        return null;
    }

    private String getAmSky(med_weather_ITEM item, int index) {
        String sky = null;

        switch (index) {
            case 3:
                sky = item.wf3Am;
                break;
            case 4:
                sky = item.wf4Am;
                break;
            case 5:
                sky = item.wf5Am;
                break;
            case 6:
                sky = item.wf6Am;
                break;
            case 7:
                sky = item.wf7Am;
                break;
        }
        return sky;
    }

    private String getPmSky(med_weather_ITEM item, int index) {
        switch (index) {
            case 3:
                return item.wf3Pm;
            case 4:
                return item.wf4Pm;
            case 5:
                return item.wf5Pm;
            case 6:
                return item.wf6Pm;
            case 7:
                return item.wf7Pm;
        }
        return null;
    }

    private String getLowTemp(med_temp_ITEM item, int index) {
        switch (index) {
            case 3:
                return item.taMin3;
            case 4:
                return item.taMin4;
            case 5:
                return item.taMin5;
            case 6:
                return item.taMin6;
            case 7:
                return item.taMin7;
            case 8:
                return item.taMin8;
            case 9:
                return item.taMin9;
            case 10:
                return item.taMin10;
        }
        return null;
    }

    private String getHighTemp(med_temp_ITEM item, int index) {
        switch (index) {
            case 3:
                return item.taMax3;
            case 4:
                return item.taMax4;
            case 5:
                return item.taMax5;
            case 6:
                return item.taMax6;
            case 7:
                return item.taMax7;
            case 8:
                return item.taMax8;
            case 9:
                return item.taMax9;
            case 10:
                return item.taMax10;
        }
        return null;
    }

    // 중기 정보 받아오기
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setMedWeather(String nx, String ny) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault());

        String base_date3 = currentDate.format(dateFormatter);
        String base_time3 = getBaseTime3();

        if (base_time3.equals("-1800")) {
            currentDate = currentDate.minusDays(1);
            base_date3 = currentDate.format(dateFormatter);
            base_time3 = "1800";
        }

//        System.out.println("weather_region_code : " + weather_region_code + " temp_region_code : " + temp_region_code + " base_time : " + base_date3 + base_time3);

        Call<med_WEATHER> med_weather_call = ApiObject.retrofitService.GetMediumTermWeather(1000, 1, "JSON", weather_region_code, base_date3 + base_time3);
        Call<med_TEMP> med_temp_call = ApiObject.retrofitService.GetMediumTermTemp(1000, 1, "JSON", temp_region_code, base_date3 + base_time3);

        int num = 7;
        MedWeatherModel[] medWeatherArr = new MedWeatherModel[num];

        for (int i = 0; i < num; i++) {
            medWeatherArr[i] = new MedWeatherModel();
            medWeatherArr[i].setDate(currentDate.plusDays(i + 4));
        }

        // 중기 욱상예보 정보(오전 오후 강수 및 날씨) 받아오기
        med_weather_call.enqueue(new retrofit2.Callback<med_WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<med_WEATHER> call, @NonNull Response<med_WEATHER> response) {
                if (response.isSuccessful()) {
                    List<med_weather_ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;
                    med_weather_ITEM item = items.get(0);

//                    System.out.println("3일 오전 : " + item.rnSt3Am);
//                    System.out.println("3일 오후 : " + item.rnSt3Pm);
//                    System.out.println("4일 오전 : " + item.rnSt4Am);
//                    System.out.println("4일 오후 : " + item.rnSt4Pm);

                    // UI 업데이트를 별도의 스레드에서 수행
                    runOnUiThread(() -> {
                        updateMedWeatherUI(item, medWeatherArr);
                    });


                    Toast.makeText(getApplicationContext(), "중기 육상 예보 로드 성공", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<med_WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });

        // 중기예보 기온 (3~10일) 가져오기
        med_temp_call.enqueue(new retrofit2.Callback<med_TEMP>() {
            @Override
            public void onResponse(@NonNull Call<med_TEMP> call, @NonNull Response<med_TEMP> response) {
                if (response.isSuccessful()) {
                    List<med_temp_ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;
                    med_temp_ITEM item = items.get(0);

                    // UI 업데이트를 별도의 스레드에서 수행
                    runOnUiThread(() -> {
                        updateMedTempUI(item, medWeatherArr);
                    });

                    Toast.makeText(getApplicationContext(), "중기 예보 기온 로드 성공", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<med_TEMP> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    // UI 업데이트를 수행하는 메서드
    private void updateMedWeatherUI(med_weather_ITEM item, MedWeatherModel[] medWeatherArr) {
        // 1일, 2일후 오전오후 강수랑 날씨는 어디서 받아올건데??? -> 일단 보류


        // 3~7일 후
        for (int i = 0; i < 4; i++) {
            medWeatherArr[i].setAm_rain(getAmRain(item, i + 4));
            medWeatherArr[i].setPm_rain(getPmRain(item, i + 4));
            medWeatherArr[i].setAm_sky(getAmSky(item, i + 4));
            medWeatherArr[i].setPm_sky(getPmSky(item, i + 4));

//                        System.out.println(i + 3 + "번째 am_sky : " + getAmSky(item, i + 3));
//                        System.out.println(i + 3 + "번째 pm_sky : " + getPmSky(item, i + 3));
        }

        // 8~10일 후
        medWeatherArr[4].setRain(item.rnSt8);
        medWeatherArr[4].setSky(item.wf8);
        medWeatherArr[5].setRain(item.rnSt9);
        medWeatherArr[5].setSky(item.wf9);
        medWeatherArr[6].setRain(item.rnSt10);
        medWeatherArr[6].setSky(item.wf10);

//                    System.out.println("8번째 sky : " + medWeatherArr[4].getSky());
//                    System.out.println("9번째 sky : " + medWeatherArr[5].getSky());
//                    System.out.println("10번째 sky : " + medWeatherArr[6].getSky());

        rv_daily_simple.setAdapter(new DailyWeatherSimpleAdapter(medWeatherArr));

    }

    private void updateMedTempUI(med_temp_ITEM item, MedWeatherModel[] medWeatherArr) {
        // 1일, 2일후 최저,최고 기온은 어디서 받아올건데??? -> 일단 보류

        // 3~10일 후
        for (int i = 0; i < 7; i++) {
            medWeatherArr[i].setLow_temp(getLowTemp(item, i + 4));
            medWeatherArr[i].setHigh_temp(getHighTemp(item, i + 4));
//                        System.out.println(i+4+"번째 최저 기온 : "+medWeatherArr[i].getLow_temp());
//                        System.out.println((i+4+"번째 최고 기온  : "+medWeatherArr[i].getHigh_temp()));
        }

        rv_daily_simple.setAdapter(new DailyWeatherSimpleAdapter(medWeatherArr));

    }

    // 지역 코드 가져오기
    private String getWeatherRegionCode(String region, double longitude) {
        Map<String, String> regionCodeMap = new HashMap<>();
        regionCodeMap.put("서울", "11B00000");
        regionCodeMap.put("인천", "11B00000");
        regionCodeMap.put("경기도", "11B00000");
        regionCodeMap.put("강원도 영서", "11D10000");
        regionCodeMap.put("강원도 영동", "11D20000");
        regionCodeMap.put("대전", "11C20000");
        regionCodeMap.put("세종", "11C20000");
        regionCodeMap.put("충청남도", "11C20000");
        regionCodeMap.put("충청북도", "11C10000");
        regionCodeMap.put("광주", "11F20000");
        regionCodeMap.put("전라남도", "11F20000");
        regionCodeMap.put("전라북도", "11F10000");
        regionCodeMap.put("대구", "11H10000");
        regionCodeMap.put("경상북도", "11H10000");
        regionCodeMap.put("부산", "11H20000");
        regionCodeMap.put("울산", "11H20000");
        regionCodeMap.put("경상남도", "11H20000");
        regionCodeMap.put("제주도", "11G00000");

        if (region.equals("강원도")) {
            if (longitude < 128.18) // 영서
                return "11D10000";
            else
                return "11D20000"; // 영동
        }

        // 키 값 차례로 가져오기
        for (String regionName : regionCodeMap.keySet()) {
            if (region.contains(regionName))
                return regionCodeMap.get(regionName);
        }

        return null;
    }

    // 지역 코드 가져오기
    private String getTempRegionCode(String region) {
//        System.out.println("현재 위치 : " + region);

        try {
            InputStream is = getBaseContext().getResources().getAssets().open("region_code.xls");

            Workbook wb = Workbook.getWorkbook(is); //엑셀파일
            //엑셀 파일이 있다면
            Sheet sheet = wb.getSheet(0);//시트 블러오기

            if (sheet != null) {
//                int colTotal = sheet.getColumns(); //전체 컬럼
                int rowTotal = sheet.getColumn(0).length;

                for (int row = 0; row < rowTotal; row++) {

                    //col: 컬럼순서, contents: 데이터값
//                        for (int col = 0; col < colTotal; col++) {
//                            String contents = sheet.getCell(col, row).getContents();
//                            Log.d("region_code", col + "번째: " + contents);
//                        }

                    String name = sheet.getCell(0, row).getContents();
                    String code = sheet.getCell(1, row).getContents();

                    // 일치하는 지역 있으면
                    if (region.contains(name)) {
//                            System.out.println("검색한 region_code : " + code);
                        return code;
                    }
                }
            }
            return null;
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void requestLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        curPoint = Conversion.dfs_xy_conv(location.getLatitude(), location.getLongitude());
                        setWeather(String.valueOf(curPoint.x), String.valueOf(curPoint.y));
                        List<Address> addressList = getAddress(location.getLatitude(), location.getLongitude());

//                        // 경산
//                        List<Address> addressList1 = getAddress(35.828032,128.739770);
//
//                        if (!addressList1.isEmpty()) {
//                            Address address = addressList1.get(0);
//                            System.out.println("현재 지역 : "+address.getAddressLine(0)); // 대한민국 경상북도 경산시 중방동 834-27
//                            System.out.println("현재 지역 : "+address.getThoroughfare()); // 중방동
//                            System.out.println("현재 지역 : "+address.getLocality()); // 경산시
//                            System.out.println("현재 지역 : "+address.getSubThoroughfare()); //834-27
//                            System.out.println("현재 지역 : "+address.getSubAdminArea()); // null
//                            System.out.println("현재 지역 : "+address.getSubLocality()); // null
//                            region_code = getRegionCode(address.getAdminArea());
//                        } else {
//                            locationText.setText(location.getLatitude() + ", " + location.getLongitude());
//                        }

//                        // 강원도 인제
//                        List<Address> addressList1 = getAddress(38.065028,128.279208);
//
//                        if (!addressList1.isEmpty()) {
//                            Address address = addressList1.get(0);
//                            System.out.println("현재 지역 : "+address.getAddressLine(0)); // 대한민국 경상북도 경산시 중방동 834-27
//                            System.out.println("현재 지역 : "+address.getThoroughfare()); // 중방동
//                            System.out.println("현재 지역 : "+address.getLocality()); // 경산시
//                            System.out.println("현재 지역 : "+address.getSubThoroughfare()); //834-27
//                            System.out.println("현재 지역 : "+address.getSubAdminArea()); // null
//                            System.out.println("현재 지역 : "+address.getSubLocality()); // null
//                            temp_region_code = getTempRegionCode(address.getAdminArea());
//                        } else {
//                            locationText.setText(location.getLatitude() + ", " + location.getLongitude());
//                        }

                        if (!addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            locationText.setText(address.getThoroughfare());
//                        region = address.getAdminArea();
//                            System.out.println("현재 지역 : "+address.getAddressLine(0)); // 대한민국 대구광역시 북구 대현동 19-54
//                            System.out.println("현재 지역 : "+address.getThoroughfare()); // 대현동
//                            System.out.println("현재 지역 : "+address.getLocality()); // null
//                            System.out.println("현재 지역 : "+address.getSubThoroughfare()); // 19-54
//                            System.out.println("현재 지역 : "+address.getSubAdminArea()); // null
//                            System.out.println("현재 지역 : "+address.getSubLocality()); // 북구
                            weather_region_code = getWeatherRegionCode(address.getAdminArea(), location.getLongitude());
                            temp_region_code = getTempRegionCode(address.getAdminArea());
                            setMedWeather(String.valueOf(curPoint.x), String.valueOf(curPoint.y));
                        } else {
                            locationText.setText(location.getLatitude() + ", " + location.getLongitude());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("requestLocation", "fail"));
    }

    private List<Address> getAddress(double lat, double lng) {
        List<Address> address = null;

        try {
            Geocoder geocoder = new Geocoder(WeatherLocationActivity.this, Locale.KOREA);
            address = geocoder.getFromLocation(lat, lng, 10);
        } catch (IOException e) {
            Toast.makeText(this, "주소를 가져 올 수 없습니다", Toast.LENGTH_SHORT).show();
        }

        return address;
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

//    private String getBaseTime2() {
//        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis())));
//        int minute = Integer.parseInt(new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis())));
//
//        int baseHour = (hour / 3) * 3 + 2;
//
//        if (hour % 3 == 2 && minute >= 10) {
//            baseHour = (baseHour + 3) % 24;
//        }
//
//        return String.format("%02d00", baseHour);
//    }

    private String getBaseTime2() {
        @SuppressLint("SimpleDateFormat") String now = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis()));
        @SuppressLint("SimpleDateFormat") String hour = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis() - (1000 * 7200)));
        @SuppressLint("SimpleDateFormat") String minute = new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis()));
        int check = ((Integer.parseInt(hour) / 3) * 3) + 2;

        // 10분 미만
        if ((Integer.parseInt(now)) % 3 == 2 && Integer.parseInt(minute) < 10) {
            check -= 3;
            if (check == -1)
                check = 23;
        }

        if (check < 10)
            hour = "0" + check + "00";
        else
            hour = check + "00";
        return hour;
    }


    private String getBaseTime3() {
        @SuppressLint("SimpleDateFormat") String hour = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis()));
        @SuppressLint("SimpleDateFormat") String minute = new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis()));
        int check = Integer.parseInt(hour);

        if (check < 6) { // 전날 18시
            hour = "-1800";
        } else if (check < 18) { // 6시
            hour = "0600";
        } else if (check <= 23) { // 18시
            hour = "1800";
        }

        return hour;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Toast.makeText(WeatherLocationActivity.this, "Result OK", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(WeatherLocationActivity.this, "Result Cancel", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void init() {
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20 * 1000);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocation() {
        if (isPermissionGranted()) {
            startLocationUpdates();
        } else {
            requestPermissions();
        }
    }

    private boolean isPermissionGranted() {
        for (String permission : PERMISSIONS) {
            if (permission.equals(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                continue;
            }
            final int result = ContextCompat.checkSelfPermission(this, permission);

            if (PackageManager.PERMISSION_GRANTED != result) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    resolveLocationSettings(e);
                } else {
                }
            }
        });
    }

    public void resolveLocationSettings(Exception exception) {
        ResolvableApiException resolvable = (ResolvableApiException) exception;
        try {
            resolvable.startResolutionForResult(this, REQUEST_CODE_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                for (String permission : permissions) {
                    if ("android.permission.ACCESS_FINE_LOCATION".equals(permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("알림");
                        builder.setMessage("위치 정보 권한이 필요합니다.\n\n[설정]->[권한]에서 '위치' 항목을 사용으로 설정해 주세요.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(WeatherLocationActivity.this, "Cancel Click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
            }
        }
    }
}
