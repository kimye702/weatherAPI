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
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import com.example.weatherapi.weather.util.Conversion;
import com.example.weatherapi.weather.util.RequestPermission;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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

    private TextView tv_location;
    private TextView tv_date;
    private TextView tv_weather;
    private TextView tv_temprature;
//    private Button btn_refresh;
    private RecyclerView weatherRecyclerView;
    private TextView locationText;

    private Point curPoint;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_location);

        init();
        checkLocation();
        requestLocation();

        tv_location = findViewById(R.id.tv_location);
        tv_date = findViewById(R.id.tv_date);
        tv_weather = findViewById(R.id.tv_today_weather);
        tv_temprature = findViewById(R.id.tv_today_temprature);
//        btn_refresh = findViewById(R.id.btn_refresh);
        locationText = findViewById(R.id.tv_location);
        weatherRecyclerView = findViewById(R.id.rv_weather);
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        // 날짜 텍스트를 설정합니다.
//        tv_date.setText(formattedDate + " " + amPm + " " + hour + ":" + String.format(Locale.getDefault(), "%02d", minute));
        tv_date.setText(formattedDate);

//        btn_refresh.setOnClickListener(v -> {
//            requestLocation();
//        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        Call<WEATHER> call = ApiObject.retrofitService.GetWeather(60, 1, "JSON", base_date, base_time, nx, ny);

        call.enqueue(new retrofit2.Callback<WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    List<ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;

                    ModelWeather[] weatherArr = new ModelWeather[6];

                    for (int i = 0; i < 6; i++) {
                        weatherArr[i] = new ModelWeather();
                    }

                    int index = 0;
                    int totalCount = response.body().response.body.totalCount - 1;
                    for (int i = 0; i <= totalCount; i++) {
                        index %= 6;
                        switch (items.get(i).category) {
                            case "PTY":
                                weatherArr[index].setRainType(items.get(i).fcstValue);
                                break;
                            case "REH":
                                weatherArr[index].setHumidity(items.get(i).fcstValue);
                                break;
                            case "SKY":
                                weatherArr[index].setSky(items.get(i).fcstValue);
                                break;
                            case "T1H":
                                weatherArr[index].setTemp(items.get(i).fcstValue);
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

                    tv_weather.setText(weather);
                    tv_temprature.setText(weatherArr[0].getTemp());


                    weatherRecyclerView.setAdapter(new WeatherAdapter(weatherArr));

                    Toast.makeText(getApplicationContext(), items.get(0).fcstDate + ", " + items.get(0).fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

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
