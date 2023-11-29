package com.example.weatherapi.finedust;

import static com.example.weatherapi.finedust.Repository.getNearbyMonitoringStation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.weatherapi.R;
import com.example.weatherapi.databinding.ActivityFinedustBinding;
import com.example.weatherapi.databinding.AirQualityItemBinding;
import com.example.weatherapi.finedust.model.airquality.Grade;
import com.example.weatherapi.finedust.model.airquality.MeasuredValue;
import com.example.weatherapi.finedust.model.monitoringstation.MonitoringStation;
import com.example.weatherapi.finedust_test.KakaoApiTest;
import com.example.weatherapi.weather.util.Conversion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class FineDustActivity extends AppCompatActivity {

    private CancellationTokenSource cancellationTokenSource = null;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private ActivityFinedustBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFinedustBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariables();
        requestLocationPermission();
        binding.refresh.setOnRefreshListener(() -> fetchAirQualityData());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
    }

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
                    fetchAirQualityData();
                }
            }
        } else {
            if (!locationPermissionGranted) {
                finish();
            } else {
                fetchAirQualityData();
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private void showBackgroundLocationPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setMessage("홈 위젯을 사용하려면 위치 접근 권한이 " + getPackageManager().getBackgroundPermissionOptionLabel() + " 상태여야 합니다.")
                .setPositiveButton("설정하기", (dialog, which) -> {
                    requestBackgroundLocationPermissions();
                    dialog.dismiss();
                })
                .setNegativeButton("그냥두기", (dialog, which) -> {
                    fetchAirQualityData();
                    dialog.dismiss();
                })
                .show();
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private void requestBackgroundLocationPermissions() {
        int REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS = 101;
        ActivityCompat.requestPermissions(
            this,
            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS
        );
    }

    private void initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

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
//                monitoringStation = Repository.getNearbyMonitoringStation(location.getLatitude(), location.getLongitude());
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
//                                measuredValue = Repository.getLatestAirQualityData(monitoringStation.getStationName());
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private void fetchAirQualityData() {
        cancellationTokenSource = new CancellationTokenSource();

        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.getToken()
        ).addOnSuccessListener(location -> {
            binding.errorDescriptionTextView.setVisibility(View.GONE);
            new Thread(() -> {
                try {
                    MonitoringStation monitoringStation = Repository.getNearbyMonitoringStation(location.getLatitude(), location.getLongitude());
                    if (monitoringStation != null) {
                        MeasuredValue measuredValue = Repository.getLatestAirQualityData(monitoringStation.getStationName());
                        if (measuredValue != null) {
                            runOnUiThread(() -> displayAirQualityData(location, monitoringStation, measuredValue));
                        } else {
                            Log.e("fetchAirQualityData error", "Measured value is null");
                            showErrorMessage();
                        }
                    } else {
                        Log.e("fetchAirQualityData error", "Monitoring station is null");
                        showErrorMessage();
                    }
                } catch (IOException e) {
                    Log.e("fetchAirQualityData error", e.toString());
                    showErrorMessage();
                } finally {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.refresh.setRefreshing(false);
                    });
                }
            }).start();
        }).addOnFailureListener(e -> {
            Log.e("fetchAirQualityData error", "Failed to get location: " + e.toString());
            showErrorMessage();
        });
    }

    private List<Address> getAddress(double lat, double lng) {
        List<Address> address = null;

        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREA);
            address = geocoder.getFromLocation(lat, lng, 10);
        } catch (IOException e) {
            Toast.makeText(this, "주소를 가져 올 수 없습니다", Toast.LENGTH_SHORT).show();
        }

        return address;
    }

    public void displayAirQualityData(Location location, MonitoringStation monitoringStation, MeasuredValue measuredValue) {
        binding.contentsLayout.animate()
                .alpha(1f)
                .start();

        // 지오코더가 동작 안하는듯?
        List<Address> addressList = getAddress(location.getLatitude(), location.getLongitude());
        if (!addressList.isEmpty()) {
            Address address = addressList.get(0);
            binding.measuringStationNameTextView.setText(address.getThoroughfare());
        } else {
            binding.measuringStationNameTextView.setText(monitoringStation.getStationName());
        }

        binding.measuringStationAddress.setText("측정소 위치: " + monitoringStation.getAddr());

        // 계속 null로 반환됨 뭔가 이상함 전체 grade 모두 이상함
        Grade grade = measuredValue.getGrade(measuredValue.getKhaiGrade());
        binding.getRoot().setBackgroundResource(grade.getColorResId());
        binding.totalGraddeLabelTextView.setText(grade.getLabel());
        binding.totalGradeEmojiTextView.setText(grade.getEmoji());

        binding.fineDustInformationTextView.setText("미세먼지: " + measuredValue.getPm10Value() + " ㎍/㎥ " +
                (measuredValue.getGrade(measuredValue.getPm10Grade()).getEmoji()));
        binding.ultraFineDuistInformationTextView.setText("초미세먼지: " + measuredValue.getPm25Value() + " ㎍/㎥ " +
                (measuredValue.getGrade(measuredValue.getPm25Grade()).getEmoji()));

        bindAirQualityItem(binding.so2Item, "아황산가스", measuredValue.getGrade(measuredValue.getSo2Grade()), measuredValue.getSo2Value());
        bindAirQualityItem(binding.coItem, "일산화탄소", measuredValue.getGrade(measuredValue.getCoGrade()), measuredValue.getCoValue());
        bindAirQualityItem(binding.o3Item, "오존", measuredValue.getGrade(measuredValue.getO3Grade()), measuredValue.getO3Value());
        bindAirQualityItem(binding.no2Item, "이산화질소", measuredValue.getGrade(measuredValue.getNo2Grade()), measuredValue.getNo2Value());
    }

    private void bindAirQualityItem(AirQualityItemBinding item, String label, Grade grade, String value) {
        // AirQualityItemBinding에서 레이아웃 뷰를 가져옵니다.
        LinearLayout airQualityItemLayout = item.airQualityItemLayout;

        // 해당 뷰에서 하위 뷰(TextView 등)를 찾습니다.
        TextView labelTextView = airQualityItemLayout.findViewById(R.id.labelTextView);
        TextView gradeTextView = airQualityItemLayout.findViewById(R.id.gradeTextView);
        TextView valueTextView = airQualityItemLayout.findViewById(R.id.valueTextView);

        // 찾은 하위 뷰에 데이터를 설정합니다.
        labelTextView.setText(label);
        gradeTextView.setText(grade != null ? grade.toString() : Grade.UNKNOWN.toString());
        valueTextView.setText(value + " ppm");
    }

    private void showErrorMessage() {
        binding.errorDescriptionTextView.setVisibility(View.VISIBLE);
        binding.contentsLayout.setVisibility(View.GONE);
    }
}
