package com.example.weatherapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;

import com.example.weatherapi.weather.WeatherLocationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ChatFragment chatFragment;
    FriendFragment friendFragment;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bottomNavigationView=findViewById(R.id.bottomnavi);
        chatFragment=new ChatFragment();
        friendFragment=new FriendFragment();
        profileFragment=new ProfileFragment();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.home:{
                        startActivity(new Intent(MainActivity.this, WeatherLocationActivity.class));
                    }
//                    case R.id.chat_friend:{
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.main_layout, chatFragment)
//                                .commit();
//                        return true;
//                    }
                    case R.id.friend:{
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_layout, friendFragment)
                                .commit();
                        return true;
                    }

                    case R.id.setting:{
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_layout, profileFragment)
                                .commit();
                        return true;
                    }
                }

                return false;
            }
        });
    }
}