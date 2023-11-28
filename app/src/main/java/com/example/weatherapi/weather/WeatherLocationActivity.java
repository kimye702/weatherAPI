package com.example.weatherapi.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.R;
import com.example.weatherapi.finedust.FineDustActivity;
import com.example.weatherapi.weather.adapter.HourWeatherSimpleAdapter;
import com.example.weatherapi.weather.adapter.VeryShortWeatherDetailAdapter;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private TextView tv_weather;
    private TextView tv_temprature;
    private Button btn_finedust;
    private Button btn_very_short_weather;
    private RecyclerView rv_very_short;
    private RecyclerView rv_hour_simple;
    private TextView locationText;
    private Point curPoint;

    // 중기 예보에 필요한 쿼리 변수
//    private String region_code;
//    private String region;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        init();
        checkLocation();
        requestLocation();

        tv_date = findViewById(R.id.tv_date);
        tv_weather = findViewById(R.id.tv_today_weather);
        tv_temprature = findViewById(R.id.tv_now_temp);
        locationText = findViewById(R.id.tv_location);
        btn_finedust = findViewById(R.id.btn_finedust);
        btn_very_short_weather = findViewById(R.id.btn_very_short_weather);
//        rv_very_short = findViewById(R.id.rv_very_short);
//        rv_very_short.setLayoutManager(new LinearLayoutManager(this));
        rv_hour_simple = findViewById(R.id.rv_hour_simple);
        rv_hour_simple.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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

        tv_date.setText(formattedDate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_very_short_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VeryShortWeatherActivity.class);
                intent.putExtra("y", String.valueOf(curPoint.y));
                intent.putExtra("x", String.valueOf(curPoint.x));
                startActivity(intent);
            }
        });

        btn_finedust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FineDustActivity.class);
                startActivity(intent);
            }
        });
    }

//     // 메뉴 리소스 XML의 내용을 앱바(App Bar)에 반영
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        return true;
//    }

    private void setWeather(String nx, String ny) {
        Calendar cal = Calendar.getInstance();
        String base_date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
        String timeH = new SimpleDateFormat("HH", Locale.getDefault()).format(cal.getTime());
        String timeM = new SimpleDateFormat("HH", Locale.getDefault()).format(cal.getTime());
        String base_time = getBaseTime(timeH, timeM);

        if (timeH.equals("00") && base_time.equals("2330")) {
            cal.add(Calendar.DATE, -1);
            base_date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
        }

        String base_date2 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
        String base_time2 = getBaseTime2();

        if (timeH.equals("00")||timeH.equals("01")||timeH.equals("02") && base_time2.equals("2300")) {
            cal.add(Calendar.DATE, -1);
            base_date2 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
        }

//        Call<WEATHER> very_short_call = ApiObject.retrofitService.GetVeryShortTermWeather(60, 1, "JSON", base_date, base_time, nx, ny);
        Call<WEATHER> short_call = ApiObject.retrofitService.GetShortTermWeather(1000, 1, "JSON", base_date2, base_time2, nx, ny);

//        System.out.println("base date : " + base_date+"base time : "+base_time);
//        System.out.println("base date : " + base_date+"base time2 : "+base_time2);

//        // 초단기 예보 정보 받아오기
//        very_short_call.enqueue(new retrofit2.Callback<WEATHER>() {
//            @Override
//            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
//                if (response.isSuccessful()) {
//                    List<ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;
//
//                    VeryShortWeatherModel[] weatherArr = new VeryShortWeatherModel[6];
//
//                    for (int i = 0; i < 6; i++) {
//                        weatherArr[i] = new VeryShortWeatherModel();
//                    }
//
//                    int index = 0;
//                    int totalCount = response.body().response.body.totalCount - 1;
////                    System.out.println("초단기예보 totalCount : "+totalCount);
//                    for (int i = 0; i <= totalCount; i++) {
//                        index %= 6;
//                        switch (items.get(i).category) {
//                            case "PTY": // 강수형태
//                                weatherArr[index].setRainType(items.get(i).fcstValue);
//                                break;
//                            case "REH": // 습도
//                                weatherArr[index].setHumidity(items.get(i).fcstValue);
//                                break;
//                            case "SKY": // 하늘상태
//                                weatherArr[index].setSky(items.get(i).fcstValue);
//                                break;
//                            case "T1H": // 기온
//                                weatherArr[index].setTemp(items.get(i).fcstValue);
//                                break;
//                            case "RN1": // 1시간 강수량
//                                weatherArr[index].setHourRain(items.get(i).fcstValue);
//                                break;
//                            case "WSD": // 풍속
//                                weatherArr[index].setWindSpeed(items.get(i).fcstValue);
//                                break;
//                            default:
//                                break;
//                        }
//                        index++;
//                    }
//
//                    for (int i = 0; i <= 5; i++) {
//                        String fcstTime = items.get(i).fcstTime;
//                        String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
//                        weatherArr[i].setFcstTime(formattedTime);
//                    }
//
//                    String sky = weatherArr[0].getSky();
//                    String weather;
//                    switch (sky) {
//                        case "1":
//                            weather = "맑음";
//                            break;
//                        case "3":
//                            weather = "구름 많음";
//                            break;
//                        case "4":
//                            weather = "흐림";
//                            break;
//                        default:
//                            weather = "오류 rainType : " + sky;
//                            break;
//                    }
//
//                    tv_weather.setText(weather);
//                    tv_temprature.setText(weatherArr[0].getTemp());
//
//
//                    rv_very_short.setAdapter(new VeryShortWeatherDetailAdapter(weatherArr));
//
//                    Toast.makeText(getApplicationContext(), "초단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
//                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
//            }
//        });

        // 단기예보 api 정보 받아오기
        short_call.enqueue(new retrofit2.Callback<WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
                if (response.isSuccessful()) {
//                    System.out.println("response : "+response);
                    List<ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;

                    int cnt = 0;
                    int index = 0;
                    int totalCount = response.body().response.body.totalCount - 1;

                    int num = totalCount/12;

                    ShortWeatherModel[] weatherArr = new ShortWeatherModel[num];

                    for (int i = 0; i < num; i++) {
                        weatherArr[i] = new ShortWeatherModel();
                    }

                    System.out.println("단기예보 totalCount : "+totalCount);
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

                        if(cnt%12==0&& !items.get(i).category.equals("TMX") && !items.get(i).category.equals("TMN"))
                        {
//                            System.out.println("fcstTime : "+items.get(i).fcstTime+"category : "+ items.get(i).category);
                            String fcstTime = items.get(i).fcstTime;
                            String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
                            weatherArr[index].setFcstTime(formattedTime);
                            index++;
                        }

                    }

                    rv_hour_simple.setAdapter(new HourWeatherSimpleAdapter(weatherArr));

                    Toast.makeText(getApplicationContext(), "단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

//    // 지역 코드 가져오기
//    private void getRegionCode(){
//
//    }

//    private void setMedWeather(String nx, String ny) {
//        Calendar cal = Calendar.getInstance();
//
//        String base_date3 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
//        String base_time3 = getBaseTime3();
//
//        if (base_time3.equals("-1800")) {
//            cal.add(Calendar.DATE, -1);
//            base_date3 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
//            base_time3 = "1800";
//        }
//
//        Call<med_WEATHER> med_weather_call = ApiObject.retrofitService.GetMediumTermWeather(1000, 1, "JSON", region_code, base_date3+base_time3);
//        Call<med_TEMP> med_temp_call = ApiObject.retrofitService.GetMediumTermTemp(1000, 1, "JSON", region_code, base_date3+base_time3);
//
//        // 초단기 예보 정보 받아오기
//        med_weather_call.enqueue(new retrofit2.Callback<med_WEATHER>() {
//            @Override
//            public void onResponse(@NonNull Call<med_WEATHER> call, @NonNull Response<med_WEATHER> response) {
//                if (response.isSuccessful()) {
//                    List<med_weather_ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;
//
//                    VeryShortWeatherModel[] weatherArr = new VeryShortWeatherModel[6];
//
//                    for (int i = 0; i < 6; i++) {
//                        weatherArr[i] = new VeryShortWeatherModel();
//                    }
//
//                    int index = 0;
//                    int totalCount = response.body().response.body.totalCount - 1;
////                    System.out.println("초단기예보 totalCount : "+totalCount);
//                    for (int i = 0; i <= totalCount; i++) {
//                        index %= 6;
//                        switch (items.get(i).category) {
//                            case "PTY": // 강수형태
//                                weatherArr[index].setRainType(items.get(i).fcstValue);
//                                break;
//                            case "REH": // 습도
//                                weatherArr[index].setHumidity(items.get(i).fcstValue);
//                                break;
//                            case "SKY": // 하늘상태
//                                weatherArr[index].setSky(items.get(i).fcstValue);
//                                break;
//                            case "T1H": // 기온
//                                weatherArr[index].setTemp(items.get(i).fcstValue);
//                                break;
//                            case "RN1": // 1시간 강수량
//                                weatherArr[index].setHourRain(items.get(i).fcstValue);
//                                break;
//                            case "WSD": // 풍속
//                                weatherArr[index].setWindSpeed(items.get(i).fcstValue);
//                                break;
//                            default:
//                                break;
//                        }
//                        index++;
//                    }
//
//                    for (int i = 0; i <= 5; i++) {
//                        String fcstTime = items.get(i).fcstTime;
//                        String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
//                        weatherArr[i].setFcstTime(formattedTime);
//                    }
//
//                    String sky = weatherArr[0].getSky();
//                    String weather;
//                    switch (sky) {
//                        case "1":
//                            weather = "맑음";
//                            break;
//                        case "3":
//                            weather = "구름 많음";
//                            break;
//                        case "4":
//                            weather = "흐림";
//                            break;
//                        default:
//                            weather = "오류 rainType : " + sky;
//                            break;
//                    }
//
//                    tv_weather.setText(weather);
//                    tv_temprature.setText(weatherArr[0].getTemp());
//
//
//                    rv_very_short.setAdapter(new VeryShortWeatherDetailAdapter(weatherArr));
//
//                    Toast.makeText(getApplicationContext(), "초단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onFailure(@NonNull Call<med_WEATHER> call, @NonNull Throwable t) {
//                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
//            }
//        });
//
//        // 중기예보 기온 가져오기
//        med_temp_call.enqueue(new retrofit2.Callback<med_TEMP>() {
//            @Override
//            public void onResponse(@NonNull Call<med_TEMP> call, @NonNull Response<med_TEMP> response) {
//                if (response.isSuccessful()) {
////                    System.out.println("response : "+response);
//                    List<med_temp_ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;
//
//                    int cnt = 0;
//                    int index = 0;
//                    int totalCount = response.body().response.body.totalCount - 1;
//
//                    int num = totalCount/12;
//
//                    ShortWeatherModel[] weatherArr = new ShortWeatherModel[num];
//
//                    for (int i = 0; i < num; i++) {
//                        weatherArr[i] = new ShortWeatherModel();
//                    }
//
//                    System.out.println("단기예보 totalCount : "+totalCount);
//                    for (int i = 0; i <= totalCount; i++) {
//                        switch (items.get(i).category) {
//                            case "POP": // 강수확률
//                                weatherArr[index].setRainPercent(items.get(i).fcstValue);
//                                break;
//                            case "PTY": // 강수형태
//                                weatherArr[index].setRainType(items.get(i).fcstValue);
//                                break;
//                            case "PCP": // 1시간 강수량
//                                weatherArr[index].setHourRain(items.get(i).fcstValue);
//                                break;
//                            case "REH": // 습도
//                                weatherArr[index].setHumidity(items.get(i).fcstValue);
//                                break;
//                            case "SKY": // 하늘상태
//                                weatherArr[index].setSky(items.get(i).fcstValue);
//                                break;
//                            case "TMP": // 1시간 기온
//                                weatherArr[index].setHourTemp(items.get(i).fcstValue);
//                                break;
//                            case "TMN": // 일 최저기온
//                                weatherArr[index].setLowTemp(items.get(i).fcstValue);
//                                cnt--;
//                                break;
//                            case "TMX": // 일 최고기온
//                                weatherArr[index].setHighTemp(items.get(i).fcstValue);
//                                cnt--;
//                                break;
//                            case "WSD": // 풍속
//                                weatherArr[index].setWindSpeed(items.get(i).fcstValue);
//                                break;
//                            default:
//                                break;
//                        }
//                        cnt++;
//
//                        if(cnt%12==0&& !items.get(i).category.equals("TMX") && !items.get(i).category.equals("TMN"))
//                        {
////                            System.out.println("fcstTime : "+items.get(i).fcstTime+"category : "+ items.get(i).category);
//                            String fcstTime = items.get(i).fcstTime;
//                            String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
//                            weatherArr[index].setFcstTime(formattedTime);
//                            index++;
//                        }
//
//                    }
//
//                    rv_hour_simple.setAdapter(new HourWeatherSimpleAdapter(weatherArr));
//
//                    Toast.makeText(getApplicationContext(), "단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onFailure(@NonNull Call<med_TEMP> call, @NonNull Throwable t) {
//                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
//            }
//        });
//    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void requestLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation()
            .addOnSuccessListener(location -> {
                if (location != null) {
                    curPoint = Conversion.dfs_xy_conv(location.getLatitude(), location.getLongitude());
                    setWeather(String.valueOf(curPoint.x), String.valueOf(curPoint.y));
                    List<Address> addressList = getAddress(location.getLatitude(), location.getLongitude());
                    if (!addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        locationText.setText(address.getThoroughfare());
//                        region = address.getAdminArea();
//                        System.out.println("현재 지역 : "+region);
//                        getRegionCode();
//                        setMedWeather(String.valueOf(curPoint.x), String.valueOf(curPoint.y));
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

    private String getBaseTime2() {
        String hour = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis() - (1000 * 7200)));
        String minute = new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis()));
        int check = ((Integer.parseInt(hour) / 3) * 3) + 2;

        // 10분 미만
        if((Integer.parseInt(hour)-2)%3==0&&Integer.parseInt(minute)<10)
        {
            check-=3;
            if(check==-1)
                check=23;
        }

        if(check<10)
            hour = "0"+check+"00";
        else
            hour = check + "00";

        return hour;
    }

    private String getBaseTime3() {
        String hour = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis()));
        String minute = new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis()));
        int check = Integer.parseInt(hour);

        if(check<6){ // 전날 18시
            hour = "-1800";
        }else if(check<18){ // 6시
            hour = "0600";
        }else if(check<23){ // 18시
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
                for (String permission:permissions) {
                    if ("android.permission.ACCESS_FINE_LOCATION".equals(permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("알림");
                        builder.setMessage("위치 정보 권한이 필요합니다.\n\n[설정]->[권한]에서 '위치' 항목을 사용으로 설정해 주세요.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
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
