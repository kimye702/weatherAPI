package com.example.weatherapi.classInfo;

public class ChatListInfo {
    String name;
    String message;
    String date;

    public ChatListInfo() {
        // 빈(default) 생성자 코드
    }


    public ChatListInfo(String name, String message, String date){
        this.name=name;
        this.message=message;
        this.date=date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
