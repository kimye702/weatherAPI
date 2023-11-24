package com.example.weatherapi.weather;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

// 메인 액티비티
public class WeatherActivity extends AppCompatActivity{

    // 날씨 정보
    private RecyclerView weatherRecyclerView;
    private String base_date = "20231012"; // 발표 일자
    private String base_time = "2300"; // 발표 시각
    private String nx = "55"; // 예보지점 X 좌표
    private String ny = "127"; // 예보지점 Y 좌표

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        TextView tvDate = findViewById(R.id.tvDate); // 오늘 날짜 텍스트뷰
        weatherRecyclerView = findViewById(R.id.weatherRecyclerView); // 날씨 리사이클러 뷰
        Button btnRefresh = findViewById(R.id.btnRefresh); // 새로고침 버튼

        // 리사이클러 뷰 매니저 설정
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 오늘 날짜 텍스트뷰 설정
        tvDate.setText(new SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(Calendar.getInstance().getTime()) + "날씨");

        // nx, ny 지점의 날씨 가져와서 설정하기
        setWeather(nx, ny);

        // <새로고침> 버튼 누를 때 날씨 정보 다시 가져오기
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWeather(nx, ny);
            }
        });
    }

    // 날씨 가져와서 설정하기
    private void setWeather(String nx, String ny) {
        // 준비 단계: base_date(발표 일자), base_time(발표 시각)
        // 현재 날짜, 시간 정보 가져오기
        Calendar cal = Calendar.getInstance();
        base_date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime()); // 현재 날짜
        String timeH = new SimpleDateFormat("HH", Locale.getDefault()).format(cal.getTime()); // 현재 시각
        String timeM = new SimpleDateFormat("HH", Locale.getDefault()).format(cal.getTime()); // 현재 분

        // API 가져오기 적당하게 변환
        base_time = getBaseTime(timeH, timeM);

        // 현재 시각이 00시이고 45분 이하여서 baseTime이 2330이면 어제 정보 받아오기
        if (timeH.equals("00") && base_time.equals("2330")) {
            cal.add(Calendar.DATE, -1);
            base_date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
        }

        Log.e("base date", base_date.toString());
        Log.e("base time", base_time.toString());

        // 날씨 정보 가져오기
        // (한 페이지 결과 수 = 60, 페이지 번호 = 1, 응답 자료 형식-"JSON", 발표 날짜, 발표 시각, 예보지점 좌표)
        Call<WEATHER> call = ApiObject.retrofitService.GetWeather(60, 1, "JSON", base_date, base_time, nx, ny);

        // 비동기적으로 실행하기
        call.enqueue(new retrofit2.Callback<WEATHER>() {
            // 응답 성공 시
            @Override
            public void onResponse(Call<WEATHER> call, Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    // 날씨 정보 가져오기
                    List<ITEM> items = response.body().response.body.items.item;

                    Log.e("Response Info", "Response body is not null");
                    Log.e("Response Info", items.toString());

                    // 현재 시각부터 1시간 뒤의 날씨 6개를 담을 배열
                    ModelWeather[] weatherArr = {new ModelWeather(), new ModelWeather(), new ModelWeather(), new ModelWeather(), new ModelWeather(), new ModelWeather()};

                    // 배열 채우기
                    int index = 0;
                    int totalCount = response.body().response.body.totalCount - 1;
                    for (int i = 0; i <= totalCount; i++) {
                        index %= 6;
                        switch (items.get(i).category) {
                            case "PTY":
                                weatherArr[index].setRainType(items.get(i).fcstValue);
                                break;
                            case "REH":
                                weatherArr[index].setHumidity(items.get(i).fcstValue); // 습도
                                break;
                            case "SKY":
                                weatherArr[index].setSky(items.get(i).fcstValue); // 하늘 상태
                                break;
                            case "T1H":
                                weatherArr[index].setTemp(items.get(i).fcstValue); // 기온
                                break;
                            default:
                                break;
                        }
                        index++;
                    }

                    // 각 날짜 배열 시간 설정
                    for (int i = 0; i <= 5; i++) {
                        weatherArr[i].setFcstTime(items.get(i).fcstTime);
                    }

                    // 리사이클러 뷰에 데이터 연결
                    weatherRecyclerView.setAdapter(new WeatherAdapter(weatherArr));

                    // 토스트 띄우기
                    Toast.makeText(getApplicationContext(), items.get(0).fcstDate + ", " + items.get(0).fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WEATHER> call, Throwable t) {
                TextView tvError = findViewById(R.id.tvError);
                tvError.setText("api fail : " + t.getMessage() + "\n 다시 시도해주세요.");
                tvError.setVisibility(View.VISIBLE);
                Log.d("api fail", t.getMessage());
            }
        });
    }

    // baseTime 설정하기
    private String getBaseTime(String h, String m) {
        String result = "";

        // 45분 전이면
        if (Integer.parseInt(m) < 45) {
            // 0시면 2330
            if (h.equals("00")) result = "2330";
            // 아니면 1시간 전 날씨 정보 부르기
            else {
                int resultH = Integer.parseInt(h) - 1;
                // 1자리면 0 붙여서 2자리로 만들기
                if (resultH < 10) result = "0" + resultH + "30";
                // 2자리면 그대로
                else result = resultH + "30";
            }
        }
        // 45분 이후면 바로 정보 받아오기
        else result = h + "30";

        return result;
    }
}