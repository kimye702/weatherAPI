package com.example.weatherapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;

import com.example.weatherapi.classInfo.UserInfo;
import com.example.weatherapi.weather.WeatherLocationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ChatFragment chatFragment;
    FriendFragment friendFragment;
    ProfileFragment profileFragment;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bottomNavigationView = findViewById(R.id.bottomnavi);
        chatFragment = new ChatFragment();
        friendFragment = new FriendFragment();
        profileFragment = new ProfileFragment();

        // Firebase 인증에서 현재 사용자의 UID를 가져옵니다.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };


        //위치 업데이트를 위한 권한 체크 및 요청
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } else {
            // 위치 업데이트 시작: 10초마다 업데이트
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
//                    case R.id.home:{
//                        startActivity(new Intent(MainActivity.this, WeatherLocationActivity.class));
//                    }
//
//                    case R.id.friend:
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.main_layout, friendFragment)
//                                .commit();
//                        break; // 각 경우에 break 문을 추가합니다.
//
//                    case R.id.setting:
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.main_layout, profileFragment)
//                                .commit();
//                        break; // 각 경우에 break 문을 추가합니다.
                }

                return false;
            }
        });
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


}
