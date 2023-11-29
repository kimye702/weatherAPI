package com.example.weatherapi.weather.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapi.R;
import com.example.weatherapi.weather.model.MedWeatherModel;
import com.example.weatherapi.weather.model.ShortWeatherModel;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

import jxl.write.DateTime;

// 중기예보 간단 정보 어댑터
public class DailyWeatherSimpleAdapter extends RecyclerView.Adapter<DailyWeatherSimpleAdapter.ViewHolder> {
    private MedWeatherModel[] items;

    public DailyWeatherSimpleAdapter(MedWeatherModel[] items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_daily_weather_simple, parent, false);
        return new ViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MedWeatherModel item = items[position];
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_daily_day;
        private TextView tv_daily_date;
        private LottieAnimationView lt_daily_weather;
        private TextView tv_daily_weather;
        private TextView tv_daily_low_temp;
        private TextView tv_daily_high_temp;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_daily_day = itemView.findViewById(R.id.tv_daily_day);
            tv_daily_date = itemView.findViewById(R.id.tv_daily_date);
            lt_daily_weather = itemView.findViewById(R.id.lt_daily_weather);
            tv_daily_weather = itemView.findViewById(R.id.tv_daily_weather);
            tv_daily_low_temp = itemView.findViewById(R.id.tv_daily_low_temp);
            tv_daily_high_temp = itemView.findViewById(R.id.tv_daily_high_temp);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void setItem(MedWeatherModel item) {
            tv_daily_day.setText(getDay(item.getDate()));
            tv_daily_date.setText(getDate(item.getDate()));
            lt_daily_weather.setAnimation(getLottie(isAm(),getSky(isAm(), item)));
            tv_daily_weather.setText(getSky(isAm(), item));
            tv_daily_low_temp.setText(item.getLow_temp());
            tv_daily_high_temp.setText(item.getHigh_temp());
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isAm(){
//        LocalDate now = LocalDate.now(); // 현재 날짜
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh");
//        int hour = Integer.parseInt(now.format(formatter).toString());

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour > 17 || hour < 5)
            return false;
        else
            return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDay(LocalDate date){
        LocalDate now = LocalDate.now(); // 현재 날짜

        if(now==date)
            return "오늘";
        else {
            DayOfWeek dayOfWeek = date.getDayOfWeek(); // 요일 얻기
            return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()); // 짧은 형태의 요일 반환
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDate(LocalDate date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault());
        return date.format(dateFormatter);
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
    public int getLottie(boolean isAm, String sky) {
        if (sky == null) {
            // sky가 null이면 기본값으로 처리 또는 예외를 throw 등의 적절한 처리를 추가할 수 있습니다.
            return R.raw.warning;
        }

        switch (sky) {
            case "맑음":
                if (isAm)
                    return R.raw.am_sunny;
                else
                    return R.raw.pm_sunny;
            case "구름많음":
                if (isAm)
                    return R.raw.am_cloud;
                else
                    return R.raw.pm_cloud;
            case "구름많고 비":
            case "구름많고 비/눈":
            case "구름많고 소나기":
            case "흐리고 비":
            case "흐리고 비/눈":
            case "흐리고 소나기":
            case "소나기":
                if (isAm)
                    return R.raw.am_cloud_rain;
                else
                    return R.raw.pm_cloud_rain;
            case "구름많고 눈":
            case "흐리고 눈":
                if (isAm)
                    return R.raw.am_cloud_snow;
                else
                    return R.raw.pm_cloud_snow;
            case "흐림":
                return R.raw.blur;
            default:
                return R.raw.warning;
        }
    }

    public String getSky(boolean isAm, MedWeatherModel item) {


        if(isAm)
            if(item.getAm_sky()==null)
                return item.getSky();
            else
                return item.getAm_sky();
        else
            if(item.getPm_sky()==null)
                return item.getSky();
            else
                return item.getPm_sky();
    }
}
