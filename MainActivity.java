package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import android.widget.LinearLayout;



import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    LinearLayout containerLayout;
    public class ScheduleItem implements Serializable {
        public String date;
        public String category;
        public int color;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerLayout = findViewById(R.id.containerLayout);


        // 전달받은 데이터 가져오기
        Intent intent = getIntent();
        ArrayList<ScheduleItem> scheduleList = (ArrayList<ScheduleItem>) getIntent().getSerializableExtra("scheduleList");

        if (scheduleList == null) {
            scheduleList = new ArrayList<>(); // Null 방지: 빈 리스트로 초기화
        }

        if (scheduleList != null) {
            for (ScheduleItem item : scheduleList) {
                Log.d("MainActivity", "Date: " + item.date + ", Category: " + item.category + ", Color: " + item.color);
            }
        }


        // 반복문 실행 (텍스트 추가 + 색상 적용을 함께 처리)
        for (ScheduleItem item : scheduleList) {
            TextView newTextView = new TextView(this);
            newTextView.setText("날짜: " + item.date + "카테고리: " + item.category);
            newTextView.setTextSize(18);
            newTextView.setPadding(10, 10, 10, 10);
            newTextView.setTextColor(item.color);

            containerLayout.addView(newTextView); // 새로운 TextView 추가
        }


        Button create = findViewById(R.id.Create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NextActivity.class);
                startActivity(intent);
            }
        });
    }
}