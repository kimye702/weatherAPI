package com.example.weatherapi.finedust_test;

import com.example.weatherapi.finedust.Repository;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.weatherapi.BuildConfig;
import com.example.weatherapi.R;
import com.example.weatherapi.finedust.model.airquality.MeasuredValue;
import com.example.weatherapi.finedust.model.monitoringstation.MonitoringStation;
import com.example.weatherapi.finedust.model.tmcoordinates.Document;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class KakaoApiTest extends AppCompatActivity {

    private CancellationTokenSource cancellationTokenSource = null;
    private FusedLocationProviderClient fusedLocationProviderClient;

//    private ActivityFinedustBinding binding;
    private TextView tv_kakao;
    private TextView tv_airstation;
    private TextView tv_airdata;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kakao_api_test);

//        binding = ActivityFinedustBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

        tv_kakao = findViewById(R.id.tv_kakao);
        tv_airstation = findViewById(R.id.tv_airstation);
        tv_airdata = findViewById(R.id.tv_airdata);

        initVariables();
        requestLocationPermission();

        // 해시키 등록하기 위한 코드
        Log.d("getKeyHash", ""+getKeyHash(KakaoApiTest.this));
    }

    public static String getKeyHash(final Context context){
        PackageManager pm = context.getPackageManager();
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            if(packageInfo == null)
                return null;

            for (Signature signature:packageInfo.signatures){
                try{
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                }  catch (NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
            }
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
    }

    // 위치정보 요청 결과
    @RequiresApi(Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean locationPermissionGranted =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!locationPermissionGranted) {
                finish();
            } else {
                boolean backgroundLocationPermissionGranted =
                        ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED;
                boolean shouldShowBackgroundPermissionRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

                if (!backgroundLocationPermissionGranted && shouldShowBackgroundPermissionRationale) {
                    showBackgroundLocationPermissionRationaleDialog();
                } else {
//                    fetchAirQualityData();
                    testKakaoAPI();
                }
            }
        } else {
            if (!locationPermissionGranted) {
                finish();
            } else {
//                fetchAirQualityData();
                testKakaoAPI();
            }
        }
    }

    // 백그라운드 위치 권한 요청 거부 -> 요청 팝업
    @RequiresApi(Build.VERSION_CODES.R)
    private void showBackgroundLocationPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setMessage("홈 위젯을 사용하려면 위치 접근 권한이 " + getPackageManager().getBackgroundPermissionOptionLabel() + " 상태여야 합니다.")
                .setPositiveButton("설정하기", (dialog, which) -> {
                    requestBackgroundLocationPermissions();
                    dialog.dismiss();
                })
                .setNegativeButton("그냥두기", (dialog, which) -> {
//                    fetchAirQualityData();
                    testKakaoAPI();
                    dialog.dismiss();
                })
                .show();
    }

    // 백그라운드 위치 권한 요청
    @RequiresApi(Build.VERSION_CODES.R)
    private void requestBackgroundLocationPermissions() {
        int REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS = 101;
        ActivityCompat.requestPermissions(
            this,
            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS
        );
    }

    // 위치 정보 받아오는 부분 초기화
    private void initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    // 위치 권한 요청
    @RequiresApi(Build.VERSION_CODES.R)
    private void requestLocationPermission() {
        int REQUEST_ACCESS_LOCATION_PERMISSIONS = 100;
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                REQUEST_ACCESS_LOCATION_PERMISSIONS
        );
    }

    // 위도, 경도 -> TM 좌표로 변환
    public Document getLocationTM(double longitude, double latitude) {
        Document document = null;
        String apiKey = BuildConfig.KAKAO_API_KEY;

        OkHttpClient client = new OkHttpClient();
        String apiUrl = "https://dapi.kakao.com/v2/local/geo/transcoord.json?x=" + longitude + "&y=" + latitude + "&input_coord=WGS84&output_coord=TM";

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "KakaoAK " + apiKey)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

            // 파싱 결과에서 TM 좌표를 추출
            double tmX = jsonObject.get("documents").getAsJsonArray().get(0).getAsJsonObject().get("x").getAsDouble();
            double tmY = jsonObject.get("documents").getAsJsonArray().get(0).getAsJsonObject().get("y").getAsDouble();

            Log.d("tmlocation","TM X 좌표: "+ tmX+"TM Y 좌표" + tmY);

            document = new Document(tmX, tmY);

            return document;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    // TM 좌표 변환 테스트
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void testKakaoAPI() {
        cancellationTokenSource = new CancellationTokenSource();

        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.getToken()
        ).addOnSuccessListener(location -> {
            // 네트워크 요청을 백그라운드 스레드에서 실행
            new Thread(() -> {
                try {
                    String tmLocation = Repository.getTMLocation(location.getLongitude(), location.getLatitude());
//                    Document tmLocation = getLocationTM(location.getLongitude(), location.getLatitude());
                    // UI 업데이트를 위해 메인 스레드에서 실행
                    runOnUiThread(() -> tv_kakao.setText(tmLocation));
//                    runOnUiThread(() -> tv_kakao.setText("tmX : "+ tmLocation.getX()+"\ntmY : "+tmLocation.getY()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();


            new Thread(() -> {
                try {
                    MonitoringStation monitoringStation = Repository.getNearbyMonitoringStation(location.getLatitude(), location.getLongitude());
                    runOnUiThread(() -> tv_airstation.setText(monitoringStation.getStationName()+"\n"+monitoringStation.getAddr()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();


            new Thread(() -> {
                try {
                    MonitoringStation monitoringStation = Repository.getNearbyMonitoringStation(location.getLatitude(), location.getLongitude());
                    MeasuredValue measuredValue = Repository.getLatestAirQualityData(monitoringStation.getStationName());
                    String result = Repository.getLatestAirQualityDataError(monitoringStation.getStationName());
                    runOnUiThread(() -> tv_airdata.setText(measuredValue.getDataTime()+"\n"+measuredValue.getKhaiValue()+"\n"+measuredValue.getKhaiGrade()+result));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

//            if (monitoringStation!=null && measuredValue != null) {
//                displayAirQualityData(measuredValue);
//            } else {
//                showErrorMessage();
//            }
        });
    }

//    @SuppressLint("MissingPermission")
//    private void fetchAirQualityData() {
//        cancellationTokenSource = new CancellationTokenSource();
//
//        fusedLocationProviderClient.getCurrentLocation(
//                LocationRequest.PRIORITY_HIGH_ACCURACY,
//                cancellationTokenSource.getToken()
//        ).addOnSuccessListener(location -> {
//            MonitoringStation monitoringStation = null;
//            try {
//                monitoringStation = getNearbyMonitoringStation(location.getLatitude(), location.getLongitude());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            MeasuredValue measuredValue = null;
//            try {
//                measuredValue = Repository.getLatestAirQualityData("your_station_name_here");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            if (monitoringStation!=null && measuredValue != null) {
//                displayAirQualityData(measuredValue);
//            } else {
//                showErrorMessage();
//            }
//        });
//    }

//    @SuppressLint("MissingPermission")
//    private void fetchAirQualityData() {
//        cancellationTokenSource = new CancellationTokenSource();
//
//        fusedLocationProviderClient.getCurrentLocation(
//            LocationRequest.PRIORITY_HIGH_ACCURACY,
//            cancellationTokenSource.getToken()
//        ).addOnSuccessListener(location -> {
//            getNearbyMonitoringStation(location.getLatitude(), location.getLongitude(), new Repository.NearbyMonitoringStationCallback() {
//                @Override
//                public void onMonitoringStationResult(MonitoringStation monitoringStation) {
//                    if (monitoringStation != null) {
//                        // MonitoringStation을 가져온 후, MeasuredValue를 가져오는 비동기 작업을 수행
//                        new FetchAirQualityDataTask(new FetchAirQualityDataTask.FetchAirQualityDataCallback() {
//                            @Override
//                            public void onFetchSuccess(MeasuredValue measuredValue) throws IOException {
//                                measuredValue = Repository.getLatestAirQualityData("your_station_name_here");
//                                if (measuredValue != null) {
//                                    displayAirQualityData(measuredValue);
//                                } else {
//                                    showErrorMessage();
//                                }
//                            }
//
//                            @Override
//                            public void onFetchFailure() {
//                                showErrorMessage();
//                            }
//                        }).execute();
//                    } else {
//                        showErrorMessage();
//                    }
//                }
//            });
//        });
//    }

//    @SuppressLint("SetTextI18n")
//    public void displayAirQualityData(MeasuredValue measuredValue) {
//        binding.contentsLayout.setVisibility(View.VISIBLE);
//
//        Grade grade = measuredValue.getKhaiGrade() != null ? measuredValue.getKhaiGrade() : Grade.UNKNOWN;
//        binding.getRoot().setBackgroundResource(grade.getColorResId());
//        binding.totalGraddeLabelTextView.setText(grade.getLabel());
//        binding.totalGradeEmojiTextView.setText(grade.getEmoji());
//
//        binding.fineDustInformationTextView.setText("미세먼지: " + measuredValue.getPm10Value() + " ㎍/㎥ " + (measuredValue.getPm10Grade() != null ? measuredValue.getPm10Grade().getEmoji() : Grade.UNKNOWN.getEmoji()));
//        binding.ultraFineDuistInformationTextView.setText("초미세먼지: " + measuredValue.getPm25Value() + " ㎍/㎥ " + (measuredValue.getPm25Grade() != null ? measuredValue.getPm25Grade().getEmoji() : Grade.UNKNOWN.getEmoji()));
//
//        bindAirQualityItem(binding.so2Item, "아황산가스", measuredValue.getSo2Grade(), measuredValue.getSo2Value());
//        bindAirQualityItem(binding.coItem, "일산화탄소", measuredValue.getCoGrade(), measuredValue.getCoValue());
//        bindAirQualityItem(binding.o3Item, "오존", measuredValue.getO3Grade(), measuredValue.getO3Value());
//        bindAirQualityItem(binding.no2Item, "이산화질소", measuredValue.getNo2Grade(), measuredValue.getNo2Value());
//    }
//
//    private void bindAirQualityItem(AirQualityItemBinding item, String label, Grade grade, String value) {
//        // AirQualityItemBinding에서 레이아웃 뷰를 가져옵니다.
//        LinearLayout airQualityItemLayout = item.airQualityItemLayout;
//
//        // 해당 뷰에서 하위 뷰(TextView 등)를 찾습니다.
//        TextView labelTextView = airQualityItemLayout.findViewById(R.id.labelTextView);
//        TextView gradeTextView = airQualityItemLayout.findViewById(R.id.gradeTextView);
//        TextView valueTextView = airQualityItemLayout.findViewById(R.id.valueTextView);
//
//        // 찾은 하위 뷰에 데이터를 설정합니다.
//        labelTextView.setText(label);
//        gradeTextView.setText(grade != null ? grade.toString() : Grade.UNKNOWN.toString());
//        valueTextView.setText(value + " ppm");
//    }
//
//    private void showErrorMessage() {
//        binding.errorDescriptionTextView.setVisibility(View.VISIBLE);
//        binding.contentsLayout.setVisibility(View.GONE);
//    }

//    private void showErrorMessage() {
//        binding.errorDescriptionTextView.setVisibility(View.VISIBLE);
//        binding.contentsLayout.setVisibility(View.GONE);
//    }
}
