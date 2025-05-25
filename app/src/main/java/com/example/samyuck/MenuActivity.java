package com.example.samyuck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button addButton = findViewById(R.id.addButton);
        Button editButton = findViewById(R.id.editButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        // 추가 버튼 클릭 시 CategoryActivity로 이동
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CategoryActivity.class);
            startActivity(intent);
        });

        // 수정 버튼 클릭 시 (추후 구현)
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CategoryManageActivity.class);
            startActivity(intent);
        });

        // 삭제 버튼 클릭 시 (추후 구현)
        deleteButton.setOnClickListener(v -> {
            // TODO: 삭제 기능 구현
        });
    }
} 