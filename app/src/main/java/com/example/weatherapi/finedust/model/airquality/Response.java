package com.example.weatherapi.finedust.model.airquality;

import com.google.gson.annotations.SerializedName;

public class Response {
    @SerializedName("body")
    private Body body;
    @SerializedName("header")
    private Header header;

    public Response(Body body, Header header) {
        this.body = body;
        this.header = header;
    }

    public Body getBody() {
        return body;
    }

    public Header getHeader() {
        return header;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}
