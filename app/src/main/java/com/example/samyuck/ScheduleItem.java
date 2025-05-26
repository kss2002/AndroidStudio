package com.example.samyuck;

import java.io.Serializable;

public class ScheduleItem implements Serializable {
    private String date;
    private String category;
    private int color;
    private String detail; // 세부 설명이나 메모

    // Firebase용 기본 생성자 필수
    public ScheduleItem() {}

    public ScheduleItem(String date, String category, int color) {
        this.date = date;
        this.category = category;
        this.color = color;
        this.detail = "";
    }

    // 모든 필드를 포함하는 생성자 (확장성용)
    public ScheduleItem(String date, String category, int color, String detail) {
        this.date = date;
        this.category = category;
        this.color = color;
        this.detail = detail;
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

    public String getDetail() {
        return detail != null ? detail : "";
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
