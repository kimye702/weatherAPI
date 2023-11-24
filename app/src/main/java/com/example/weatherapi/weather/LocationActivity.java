package com.example.weatherapi.weather;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapi.R;
import com.example.weatherapi.weather.util.RequestPermission;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        new RequestPermission(this).requestLocation(); // 위치 권한 요청
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        TextView locationText = findViewById(R.id.locationText);
        Button locationButton = findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(locationText);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation(final TextView textView) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        List<Address> addressList = getAddress(location.getLatitude(), location.getLongitude());
                        if (!addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            textView.setText(location.getLatitude() + ", " + location.getLongitude() + "\n" + address.getAdminArea() + " " + address.getLocality() + " " + address.getThoroughfare());
                        }
                        else{
                            textView.setText(location.getLatitude() + ", " + location.getLongitude());
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    textView.setText(e.getLocalizedMessage());
                }
            });
    }

    private List<Address> getAddress(double lat, double lng) {
        List<Address> address = null;

        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREA);
            address = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            Toast.makeText(this, "주소를 가져 올 수 없습니다", Toast.LENGTH_SHORT).show();
        }

        return address;
    }
}
