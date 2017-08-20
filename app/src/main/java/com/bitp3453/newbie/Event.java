package com.bitp3453.newbie;

/**
 * Created by Timmy Ho on 5/19/2017.
 */

public class Event {
    public static final String fcmUrl = "http://www.utemupass.16mb.com/WebService/fcm.php";

    private String id;
    private String name;
    private String desc;
    private String location;
    private String date;
    private String startTime;
    private String endTime;
    private String userId;
    private String category;

    public Event(){

    }

    public Event(String id, String name, String desc, String location, String date, String startTime, String endTime, String userId, String category) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userId = userId;
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
