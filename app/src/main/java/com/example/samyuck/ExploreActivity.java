package com.example.samyuck;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;

public class ExploreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        LinearLayout feedNav = findViewById(R.id.feedNav);
        LinearLayout exploreNav = findViewById(R.id.exploreNav);

        feedNav.setOnClickListener(v -> {
            Intent intent = new Intent(ExploreActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        exploreNav.setOnClickListener(v -> {
            // 현재 ExploreActivity이므로 아무 동작 없음
        });
    }
} 