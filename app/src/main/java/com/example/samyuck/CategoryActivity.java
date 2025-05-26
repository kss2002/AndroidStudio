package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity{

    Button ColorBtn, end;
    EditText Category;
    public int selectedColor= Color.BLACK; // 기본값 (검정색)
    DatePicker datePicker;
    String selectedDate;
    String category;
    private FirebaseAuth mAuth;

    public class ScheduleItem implements Serializable {
        public String category;
        public int color;

        public ScheduleItem(String category, int color) {
            this.category = category;
            this.color = color;
        }

        public String getCategory() { return category; }
        public int getColor() { return color; }
    }


    ArrayList<ScheduleItem> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        mAuth = FirebaseAuth.getInstance();
        ColorBtn = findViewById(R.id.ColorBtn);
        Category = findViewById(R.id.Category);
        end = findViewById(R.id.end);

        String[] colors = {"빨강", "초록", "파랑", "노랑"};
        int defaultColor = Color.BLACK; // 기본값을 검은색으로 설정

        ColorBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("색을 선택하세요")
                    .setItems(colors, (dialog, which) -> {
                        selectedColor = (which == 0) ? Color.RED :
                                (which == 1) ? Color.GREEN :
                                        (which == 2) ? Color.BLUE :
                                                (which == 3) ? Color.YELLOW :
                                                        defaultColor;

                        ColorBtn.setBackgroundColor(selectedColor); // 버튼 색상 변경
                    });
            builder.show();
        });


        // 완료 버튼 클릭 시 데이터 저장 후 MainActivity로 전달
        end.setOnClickListener(v -> {
            String userId = mAuth.getCurrentUser().getUid();


            category = Category.getText().toString();

            // Firebase에 데이터 저장
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            ScheduleItem scheduleItem = new ScheduleItem(category, selectedColor);

            database.child("users").child(userId).child("categories").push()
                    .setValue(scheduleItem)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "데이터 저장 성공!");
                        Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Log.e("Firebase", "데이터 저장 실패", e));
        });
    };
}


