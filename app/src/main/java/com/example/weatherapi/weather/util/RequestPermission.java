package com.example.weatherapi.weather.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RequestPermission {
    private Context context;

    private static final int REQUEST_LOCATION = 1;

    /** 위치 권한 SDK 버전 29 이상 **/
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private String[] permissionsLocationUpApi29Impl = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    /** 위치 권한 SDK 버전 29 이하 **/
    @TargetApi(Build.VERSION_CODES.P)
    private String[] permissionsLocationDownApi29Impl = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public RequestPermission(Context mContext) {
        context = mContext;
    }

    /** 위치정보 권한 요청 **/
    public void requestLocation() {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ActivityCompat.checkSelfPermission(context, permissionsLocationUpApi29Impl[0]) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, permissionsLocationUpApi29Impl[1]) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, permissionsLocationUpApi29Impl[2]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, permissionsLocationUpApi29Impl, REQUEST_LOCATION);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, permissionsLocationDownApi29Impl[0]) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, permissionsLocationDownApi29Impl[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, permissionsLocationDownApi29Impl, REQUEST_LOCATION);
            }
        }
    }

    /** 위치권한 허용 여부 검사 **/
    public boolean isLocationPermitted() {
        if (Build.VERSION.SDK_INT >= 29) {
            for (String perm : permissionsLocationUpApi29Impl) {
                if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        } else {
            for (String perm : permissionsLocationDownApi29Impl) {
                if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }
}