package com.example.weatherapi.classInfo;

import java.util.ArrayList;
import java.util.Map;

public class UserInfo {
    String uid;
    String id;
    String password;
    String name;
    String profile;
    ArrayList<String> friendList;
    ArrayList<String> chatList;
    String latitude;
    String longtitude;

    public UserInfo(){}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public ArrayList<String> getChatList() {
        return chatList;
    }

    public void setChatList(ArrayList<String> chatList) {
        this.chatList = chatList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<String> friendList) {
        this.friendList = friendList;
    }

    public void add_friend(String friend_id){
        friendList.add(friend_id);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }
}
