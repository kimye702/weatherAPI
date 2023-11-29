package com.example.weatherapi.classInfo;

import java.util.HashMap;
import java.util.Map;

public class ChatInfo {
    public Map<String, Boolean> users=new HashMap<>();
    public Map<String, Comment> comments=new HashMap<>();

    public static class Comment{
        String uid;
        String message;
    }
}
