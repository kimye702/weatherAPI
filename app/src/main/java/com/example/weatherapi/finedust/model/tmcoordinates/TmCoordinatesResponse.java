package com.example.weatherapi.finedust.model.tmcoordinates;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TmCoordinatesResponse {
    @SerializedName("documents")
    private List<Document> documents;

    @SerializedName("meta")
    private Meta meta;

    public List<Document> getDocuments() {
        return documents;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
