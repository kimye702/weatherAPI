package com.example.weatherapi.weather.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapi.R;
import com.example.weatherapi.weather.model.ShortWeatherModel;

// 단기예보 간단한 어댑터 - 완성
public class HourWeatherSimpleAdapter extends RecyclerView.Adapter<HourWeatherSimpleAdapter.ViewHolder> {
    private ShortWeatherModel[] items;

    public HourWeatherSimpleAdapter(ShortWeatherModel[] items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_hour_weather_simple, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ShortWeatherModel item = items[position];
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_hour;
        private LottieAnimationView lt_hour_weather;
        private TextView tv_hour_temp;
        private TextView tv_hour_rain;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_hour = itemView.findViewById(R.id.tv_hour);
            lt_hour_weather = itemView.findViewById(R.id.lt_hour_weather);
            tv_hour_temp = itemView.findViewById(R.id.tv_hour_temp);
            tv_hour_rain = itemView.findViewById(R.id.tv_hour_rain);
        }

        @SuppressLint("SetTextI18n")
        public void setItem(ShortWeatherModel item) {
            tv_hour.setText(item.getFcstTime());
//            iv_hour_weather.setImageResource(getSky(item.getSky()));
            lt_hour_weather.setAnimation(getLottie(item.getFcstTime(), item.getSky(), item.getRainType()));
            tv_hour_temp.setText(item.getHourTemp() + "°C");
            tv_hour_rain.setText(item.getRainPercent() + "%");
        }
    }

    public int getLottie(String time, String sky, String rainType) {

        boolean isAm = true; // 07~17 pm은 17~24, 00~05

        int hour = Integer.parseInt(time.substring(0,2));
        if (hour > 17 || hour < 5)
            isAm = false;

        switch (sky) {
            case "1":  // 맑음
                switch (rainType) {
                    case "0": // 강수 없음
                        if (isAm)
                            return R.raw.am_sunny;
                        else
                            return R.raw.pm_sunny;

                    case "1":
                    case "2":
                    case "4": // 비, 비 or 눈, 소나기
                        if (isAm)
                            return R.raw.am_cloud_rain;
                        else
                            return R.raw.pm_cloud_rain;

                    case "3": // 눈
                        if (isAm)
                            return R.raw.am_cloud_snow;
                        else
                            return R.raw.pm_cloud_snow;
                }
                break;

            case "3":  // 구름많음
                switch (rainType) {
                    case "0": // 강수 없음
                        if (isAm)
                            return R.raw.am_cloud;
                        else
                            return R.raw.pm_cloud;

                    case "1":
                    case "2":
                    case "4": // 비, 비 or 눈, 소나기
                        if (isAm)
                            return R.raw.am_cloud_rain;
                        else
                            return R.raw.pm_cloud_rain;

                    case "3": // 눈
                        if (isAm)
                            return R.raw.am_cloud_snow;
                        else
                            return R.raw.pm_cloud_snow;
                }
                break;

            case "4":  // 흐림
                switch (rainType) {
                    case "0": // 강수 없음
                        return R.raw.blur; // 기본 흐림

                    case "1":
                    case "2":
                    case "4": // 비, 비 or 눈, 소나기
                        if (isAm)
                            return R.raw.am_cloud_rain;
                        else
                            return R.raw.pm_cloud_rain;

                    case "3": // 눈
                        if (isAm)
                            return R.raw.am_cloud_snow;
                        else
                            return R.raw.pm_cloud_snow;
                }
                break;

            default:  // 오류
                return R.raw.warning;
        }

        return R.raw.warning;
    }
}

