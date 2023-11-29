package com.example.weatherapi.weather.adapter;

import static java.lang.Math.pow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapi.R;
import com.example.weatherapi.weather.model.VeryShortWeatherModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// 초단기 예보 활용 6시간이내 디테일
public class VeryShortWeatherDetailAdapter extends RecyclerView.Adapter<VeryShortWeatherDetailAdapter.ViewHolder> {
    private VeryShortWeatherModel[] items;

    public VeryShortWeatherDetailAdapter(VeryShortWeatherModel[] items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_very_short_weather_detail, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VeryShortWeatherModel item = items[position];
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;
        private TextView tvRainType;
        private TextView tvPerceivedTemp;
        private TextView tvWind;
        private LottieAnimationView lt_very_short_detail;
        private TextView tvHumidity;
        private TextView tvSky;
        private TextView tvTemp;
        private TextView tvRecommends;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRainType = itemView.findViewById(R.id.tvRainType);
            tvPerceivedTemp = itemView.findViewById(R.id.tvPerceivedTemp);
            tvWind = itemView.findViewById(R.id.tvWind);
            lt_very_short_detail = itemView.findViewById(R.id.lt_very_short_detail);
            tvHumidity = itemView.findViewById(R.id.tvHumidity);
            tvSky = itemView.findViewById(R.id.tvSky);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvRecommends = itemView.findViewById(R.id.tvRecommends);
        }

        public void setItem(VeryShortWeatherModel item) {
            tvTime.setText(item.getFcstTime());
            tvRainType.setText(getRainType(item.getRainType()));
            tvPerceivedTemp.setText(getPerceivedTemp(item.getTemp(), item.getWindSpeed()));
            tvWind.setText(item.getWindSpeed()+"m/s");
            lt_very_short_detail.setAnimation(getLottie(item.getFcstTime(),item.getSky(), item.getRainType()));
            tvHumidity.setText(item.getHumidity()+"%");
            tvSky.setText(getSky(item.getSky(), item.getRainType()));
            tvTemp.setText(item.getTemp() + "°");
            tvRecommends.setText(getRecommends(Integer.parseInt(item.getTemp())));
        }
    }

    public String getPerceivedTemp(String temp, String wind) {
        double T = Double.parseDouble(temp);
        double V = Double.parseDouble(wind);

        // 체감온도 공식
        double result = 13.12+0.6251*T-11.37*pow(V,0.16)+0.3965*pow(V,0.16)*T;

        // 체감온도를 소수점 첫째 자리까지 형식화
        String formattedResult = String.format("%.1f", result);

        return formattedResult+"°";
    }

    public int getLottie(String time, String sky, String rain) {
        boolean isAm = true; // 07~17 pm은 17~24, 00~05

        int hour = Integer.parseInt(time.substring(0, 2));
        if (hour > 17 || hour < 5)
            isAm = false;

        switch (sky) {
            case "1":  // 맑음
                switch (rain) {
                    case "0": // 강수 없음
                        if (isAm)
                            return R.raw.am_sunny;
                        else
                            return R.raw.pm_sunny;

                    case "1":
                    case "2":
                    case "5":
                    case "6":
                        if (isAm)
                            return R.raw.am_cloud_rain;
                        else
                            return R.raw.pm_cloud_rain;

                    case "3": // 눈
                    case "7":
                        if (isAm)
                            return R.raw.am_cloud_snow;
                        else
                            return R.raw.pm_cloud_snow;
                }
                break;

            case "3":  // 구름많음
                switch (rain) {
                    case "0": // 강수 없음
                        if (isAm)
                            return R.raw.am_cloud;
                        else
                            return R.raw.pm_cloud;

                    case "1":
                    case "2":
                    case "5":
                    case "6":
                        if (isAm)
                            return R.raw.am_cloud_rain;
                        else
                            return R.raw.pm_cloud_rain;

                    case "3": // 눈
                    case "7":
                        if (isAm)
                            return R.raw.am_cloud_snow;
                        else
                            return R.raw.pm_cloud_snow;
                }
                break;

            case "4":  // 흐림
                switch (rain) {
                    case "0": // 강수 없음
                        return R.raw.blur; // 기본 흐림

                    case "1":
                    case "2":
                    case "5":
                    case "6":
                        if (isAm)
                            return R.raw.am_cloud_rain;
                        else
                            return R.raw.pm_cloud_rain;

                    case "3": // 눈
                    case "7":
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

    public String getRainType(String rainType) {
        switch (rainType) {
            case "0":
                return "없음";
            case "1":
                return "비";
            case "2":
                return "비/눈";
            case "3":
                return "눈";
            case "5":
                return "빗방울";
            case "6":
                return "빗방울, 눈날림";
            case "7":
                return "눈날림";
            default:
                return "오류 rainType : " + rainType;
        }
    }

    public String getSky(String sky, String rain) {
        switch (sky) {
            case "1":  // 맑음
                switch (rain) {
                    case "0": // 강수 없음
                        return "맑음";

                    case "1":
                    case "2":
                        return "비";

                    case "3":
                        return "눈";

                    case "5":
                    case "6":
                    case "7":
                        return "대체로 맑음";
                }
                break;

            case "3":  // 구름많음
                switch (rain) {
                    case "0": // 강수 없음
                        return "구름 많음";

                    case "1":
                    case "2":
                        return "구름 많고 비";

                    case "3": // 눈
                       return "구름 많고 눈";

                    case "5":
                    case "6":
                    case "7":
                        return "대체로 구름 많음";
                }
                break;

            case "4":  // 흐림
                switch (rain) {
                    case "0": // 강수 없음
                        return "흐림";

                    case "1":
                    case "2":
                        return "흐리고 비";

                    case "3": // 눈
                       return "흐리고 눈";

                    case "5":
                    case "6":
                    case "7":
                        return "대체로 흐림";
                }
                break;

            default:  // 오류
                return "알 수 없음";
        }
        return "알 수 없음";
    }

    public String getRecommends(int temp) {
        if (temp >= 5 && temp <= 8) {
            return "울 코트, 가죽 옷, 기모";
        } else if (temp >= 9 && temp <= 11) {
            return "트렌치 코트, 야상, 점퍼";
        } else if (temp >= 12 && temp <= 16) {
            return "자켓, 가디건, 청자켓";
        } else if (temp >= 17 && temp <= 19) {
            return "니트, 맨투맨, 후드, 긴바지";
        } else if (temp >= 20 && temp <= 22) {
            return "블라우스, 긴팔 티, 슬랙스";
        } else if (temp >= 23 && temp <= 27) {
            return "얇은 셔츠, 반바지, 면바지";
        } else if (temp >= 28 && temp <= 50) {
            return "민소매, 반바지, 린넨 옷";
        } else {
            return "패딩, 누빔 옷, 목도리";
        }
    }
}

