package com.example.weatherapi.weather;

import static java.lang.Math.pow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.R;
import com.example.weatherapi.classInfo.UserInfo;
import com.example.weatherapi.weather.adapter.DailyWeatherSimpleAdapter;
import com.example.weatherapi.weather.adapter.HourWeatherSimpleAdapter;
import com.example.weatherapi.weather.adapter.VeryShortWeatherDetailAdapter;
import com.example.weatherapi.weather.model.MedWeatherModel;
import com.example.weatherapi.weather.model.ShortWeatherModel;
import com.example.weatherapi.weather.model.VeryShortWeatherModel;
import com.example.weatherapi.weather.util.Conversion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class getLocationActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseReference databaseReference;
    private String userId;
    private String friendUid;
//    private TextView locationPosition;

    private TextView tv_date;
    private TextView tv_today_weather;
    private TextView tv_now_temp;
    private TextView tv_perceived_temp;
    private TextView tv_highest_temp;
    private TextView tv_lowest_temp;
    //    private Button btn_finedust;
    private RecyclerView rv_very_short;
    private RecyclerView rv_hour_simple;
    private RecyclerView rv_daily_simple;
    private TextView locationText;
    private Point curPoint;

    // 중기 예보에 필요한 쿼리 변수
    private String weather_region_code;
    private String temp_region_code;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_weather);
//        setContentView(R.layout.location_test);

//        locationPosition=findViewById(R.id.location_position);

        tv_date = findViewById(R.id.tv_date);
        tv_today_weather = findViewById(R.id.tv_today_weather);
        tv_now_temp = findViewById(R.id.tv_now_temp);
        tv_perceived_temp = findViewById(R.id.tv_perceived_temp);
        tv_highest_temp = findViewById(R.id.tv_highest_temp);
        tv_lowest_temp = findViewById(R.id.tv_lowest_temp);
        locationText = findViewById(R.id.tv_location);
//        btn_finedust = findViewById(R.id.btn_finedust);
        rv_very_short = findViewById(R.id.rv_very_short);
        rv_very_short.setLayoutManager(new LinearLayoutManager(this));
        rv_hour_simple = findViewById(R.id.rv_hour_simple);
        rv_hour_simple.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_daily_simple = findViewById(R.id.rv_daily_simple);
        rv_daily_simple.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 현재 시간을 가져옵니다.
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        int hour = currentTime.getHour();
        int minute = currentTime.getMinute();

        // 요일을 포함한 날짜를 포맷팅합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 EEEE");
        String formattedDate = currentDate.format(formatter);

        // 시간을 오전 또는 오후로 변환합니다.
        String amPm;
        if (hour >= 12) {
            amPm = "오후";
            hour -= 12;
        } else {
            amPm = "오전";
        }

        tv_date.setText(formattedDate);

        // 대기질은 현재 위치의 미세먼지 정보 반환하므로 친구 기능에서는 제외
//        btn_finedust.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), FineDustActivity.class);
//                startActivity(intent);
//            }
//        });


        // Firebase 인증에서 현재 사용자의 UID를 가져옵니다.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendUid = getIntent().getStringExtra("friendUid");
        // Firebase Realtime Database 레퍼런스를 초기화합니다.
        databaseReference = FirebaseDatabase.getInstance().getReference("user");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 위치가 변경될 때마다 Firebase에 업데이트합니다.
                updateLocationInFirebase(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }

        monitorFriendLocation();
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

                    tv_today_weather.setText(weather);
                    tv_now_temp.setText(weatherArr[0].getTemp());

                    double T = Double.parseDouble(weatherArr[0].getTemp());
                    double V = Double.parseDouble(weatherArr[0].getWindSpeed());
                    double rst = 13.12 + 0.6251 * T - 11.37 * pow(V, 0.16) + 0.3965 * pow(V, 0.16) * T;

                    // 체감온도를 소수점 첫째 자리까지 형식화
                    String perceived_temp = String.format("%.1f", rst);
                    tv_perceived_temp.setText(perceived_temp + "°");


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

        // 단기예보 api 정보 받아오기
        short_call.enqueue(new retrofit2.Callback<WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    WEATHER weather = response.body();
//                    System.out.println("response : "+response);
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

                        int num = totalCount / 12 - diff;

                        ShortWeatherModel[] weatherArr = new ShortWeatherModel[num];

                        for (int i = 0; i < num; i++) {
                            weatherArr[i] = new ShortWeatherModel();
                        }

//                    System.out.println("단기예보 totalCount : " + totalCount);
                        for (int i = diff; i <= totalCount; i++) {
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

                        if (low_temp != null)
                            tv_lowest_temp.setText(low_temp + "°");
                        else
                            tv_lowest_temp.setText("error");

                        if (high_temp != null)
                            tv_highest_temp.setText(high_temp + "°");
                        else
                            tv_highest_temp.setText("error");

                        rv_hour_simple.setAdapter(new HourWeatherSimpleAdapter(weatherArr));

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
//        System.out.println("getLowSky : " + sky);
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

//                    System.out.println("중기 기온 : " + response);

                    // 1일, 2일후 최저,최고 기온은 어디서 받아올건데??? -> 일단 보류

                    // 3~10일 후
                    for (int i = 0; i < 7; i++) {
                        medWeatherArr[i].setLow_temp(getLowTemp(item, i + 4));
                        medWeatherArr[i].setHigh_temp(getHighTemp(item, i + 4));
//                        System.out.println(i+4+"번째 최저 기온 : "+medWeatherArr[i].getLow_temp());
//                        System.out.println((i+4+"번째 최고 기온  : "+medWeatherArr[i].getHigh_temp()));
                    }

                    rv_daily_simple.setAdapter(new DailyWeatherSimpleAdapter(medWeatherArr));

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

        try {
            InputStream is = getBaseContext().getResources().getAssets().open("region_code.xls");

            Workbook wb = Workbook.getWorkbook(is);
            Sheet sheet = wb.getSheet(0);

            if (sheet != null) {
                int rowTotal = sheet.getColumn(0).length;

                for (int row = 0; row < rowTotal; row++) {
                    String name = sheet.getCell(0, row).getContents();
                    String code = sheet.getCell(1, row).getContents();

                    if (region.contains(name)) {
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
    private void requestLocation(String lat, String lon) {
        double latitude = Double.parseDouble(lat);
        double longitude = Double.parseDouble(lon);

        curPoint = Conversion.dfs_xy_conv(latitude, longitude);
        setWeather(String.valueOf(curPoint.x), String.valueOf(curPoint.y)); // 단기, 초단기 예보

        List<Address> addressList = getAddress(latitude, longitude);

        if (!addressList.isEmpty()) {
            Address address = addressList.get(0);
            locationText.setText(address.getThoroughfare());
            weather_region_code = getWeatherRegionCode(address.getAdminArea(), longitude);
            temp_region_code = getTempRegionCode(address.getAdminArea());
            setMedWeather(String.valueOf(curPoint.x), String.valueOf(curPoint.y)); // 중기 예보
        } else {
            locationText.setText(latitude + ", " + longitude);
        }
    }

    private List<Address> getAddress(double lat, double lng) {
        List<Address> address = null;

        try {
            Geocoder geocoder = new Geocoder(getLocationActivity.this, Locale.KOREA);
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

private String getBaseTime2() {
        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis())));
        int minute = Integer.parseInt(new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis())));

        int baseHour = (hour / 3) * 3 + 2;

        if (hour % 3 == 2 && minute >= 10) {
            baseHour = (baseHour + 3) % 24;
        }

        return String.format("%02d00", baseHour);
    }

//     private String getBaseTime2() {
//        String hour = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis() - (1000 * 7200)));
//        String minute = new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis()));
//        int check = ((Integer.parseInt(hour) / 3) * 3) + 2;
//
//        // 10분 미만
//        if ((Integer.parseInt(hour) - 2) % 3 == 0 && Integer.parseInt(minute) < 10) {
//            check -= 3;
//            if (check == -1)
//                check = 23;
//        }
//
//        if (check < 10)
//            hour = "0" + check + "00";
//        else
//            hour = check + "00";
//        return hour;
//    }

    private String getBaseTime3() {
        String hour = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis()));
        String minute = new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis()));
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

    private void updateLocationInFirebase(Location location) {
        // UserInfo 객체에 위치 정보를 설정합니다.
        UserInfo userInfo = new UserInfo();
        userInfo.setLatitude(String.valueOf(location.getLatitude()));
        userInfo.setLongtitude(String.valueOf(location.getLongitude()));
        // Firebase에 위치 정보를 업데이트합니다.
        databaseReference.child(userId).setValue(userInfo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private void monitorFriendLocation() {
        DatabaseReference friendLocationRef = databaseReference.child(friendUid);
        friendLocationRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);
                    if (friendInfo != null) {
                        String locationText = "Latitude: " + friendInfo.getLatitude() +
                                ", Longitude: " + friendInfo.getLongtitude();
                        requestLocation(friendInfo.getLatitude(), friendInfo.getLongtitude());
//                        locationPosition.setText(locationText);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DBError", "Failed to read value.", databaseError.toException());
            }
        });
    }
}
