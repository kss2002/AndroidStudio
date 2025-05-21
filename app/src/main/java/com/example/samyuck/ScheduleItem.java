package com.example.samyuck;

import android.graphics.Color;

import java.io.Serializable;

public class ScheduleItem implements Serializable {
    private String date;
    private String category;
    private int color;

    // 기본 생성자
    public ScheduleItem() {
        this.date = "";
        this.category = "";
        this.color = getColor();
    }

    public ScheduleItem(String date, String category, int color) {
        this.date = date != null ? date : "";
        this.category = category != null ? category : "";
        this.color = color;
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}