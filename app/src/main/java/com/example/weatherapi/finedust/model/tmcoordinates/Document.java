package com.example.weatherapi.finedust.model.tmcoordinates;

import com.google.gson.annotations.SerializedName;

public class Document {
    @SerializedName("x")
    private Double x;

    @SerializedName("y")
    private Double y;

    public Document(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
