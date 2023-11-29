package com.example.weatherapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapi.classInfo.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class getLocationActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseReference databaseReference;
    private String userId;
    private String friendUid;
    private TextView locationPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_test);

        locationPosition=findViewById(R.id.location_position);

        // Firebase 인증에서 현재 사용자의 UID를 가져옵니다.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendUid=getIntent().getStringExtra("friendUid");
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }

        monitorFriendLocation();
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
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserInfo friendInfo = dataSnapshot.getValue(UserInfo.class);
                    if (friendInfo != null) {
                        String locationText = "Latitude: " + friendInfo.getLatitude() +
                                ", Longitude: " + friendInfo.getLongtitude();
                        locationPosition.setText(locationText);
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
