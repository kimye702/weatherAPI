package com.example.weatherapi.finedust.model.airquality;

import androidx.annotation.ColorRes;
import com.example.weatherapi.R;

public enum Grade {
    GOOD("좋음", "☺️", R.color.blue),
    NORMAL("보통", "🙂", R.color.green),
    BAD("나쁨", "☹️", R.color.yellow),
    AWFUL("매우나쁨", "😡", R.color.red),
    UNKNOWN("미측정", "🧐", R.color.grey);

    private final String label;
    private final String emoji;
    @ColorRes
    private final int colorResId;

    Grade(String label, String emoji, int colorResId) {
        this.label = label;
        this.emoji = emoji;
        this.colorResId = colorResId;
    }

    public String getLabel() {
        return label;
    }

    public String getEmoji() {
        return emoji;
    }

    @ColorRes
    public int getColorResId() {
        return colorResId;
    }

    @Override
    public String toString() {
        return label + " " + emoji;
    }
}
