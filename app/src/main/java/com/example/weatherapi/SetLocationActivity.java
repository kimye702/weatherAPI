package com.example.weatherapi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.weatherapi.classInfo.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetLocationActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseReference databaseReference;
    private String userId;
    private TextView setLocationText;
    private Button setLocationButton;

    private boolean isLocationUpdated = false; // 추가

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_location);

        setLocationButton = findViewById(R.id.setLocationButton);
        setLocationText = findViewById(R.id.setLocationText);

        // Firebase 인증에서 현재 사용자의 UID를 가져옵니다.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Firebase Realtime Database 레퍼런스를 초기화합니다.
        databaseReference = FirebaseDatabase.getInstance().getReference("user");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 위치 권한을 체크하고 권한이 있을 때 위치 업데이트를 시작합니다.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } else {
            // 위치 리스너를 초기화하고 위치 업데이트를 시작합니다.
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // 위치가 변경될 때마다 Firebase에 업데이트합니다.
                    updateLocationInFirebase(location);

                    // 위치 업데이트를 한 번만 받은 후 중지합니다.
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }



    private void updateLocationInFirebase(Location location) {
        // UserInfo 객체에 위치 정보를 설정합니다.
        UserInfo userInfo = new UserInfo();
        userInfo.setLatitude(String.valueOf(location.getLatitude()));
        userInfo.setLongtitude(String.valueOf(location.getLongitude()));
        // Firebase에 위치 정보를 업데이트합니다.
        databaseReference.child(userId).setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    setLocationText.setText("위치 등록이 완료되었습니다!\n이제 시작해볼까요?");
                    setLocationButton.setVisibility(View.VISIBLE);

                    setLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(SetLocationActivity.this, MainActivity.class);
                            startActivity(intent);
                            //finish();
                        }
                    });
                }
                else{
                    setLocationText.setText("위치가 등록되지 않았습니다\n다시 시도해주세요");
                }

                //finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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
}
