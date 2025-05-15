package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.io.Serializable;
import java.util.ArrayList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class NextActivity extends AppCompatActivity {

    Button ColorBtn, end;
    EditText Category;
    public int selectedColor=Color.BLACK; // 기본값 (검정색)
    DatePicker datePicker;
    String selectedDate;
    String inputText;


    public class ScheduleItem implements Serializable {
        public String date;
        public String Category;
        public int color; // 색상 값 (예: #FF0000)

        public ScheduleItem(String date, String text, int color) {
            this.date = date;
            this.Category = text;
            this.color = color;
        }

        // Getter & Setter
        public String getDate() {
            return date;
        }

        public String getText() {
            return Category;
        }

        public int getColor() {
            return color;
        }
    }

    ArrayList<ScheduleItem> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        datePicker=findViewById(R.id.datePicker);
        ColorBtn = findViewById(R.id.ColorBtn);
        Category = findViewById(R.id.Category);
        end = findViewById(R.id.end);

        int selectedYear = datePicker.getYear();
        int selectedMonth = datePicker.getMonth();
        int selectedDay = datePicker.getDayOfMonth();

        String[] colors = {"빨강", "초록", "파랑", "노랑"};
        int defaultColor = Color.BLACK; // 기본값을 검은색으로 설정

        ColorBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("색을 선택하세요")
                    .setItems(colors, (dialog, which) -> {
                        int selectedColor = (which == 0) ? Color.RED :
                                (which == 1) ? Color.GREEN :
                                        (which == 2) ? Color.BLUE :
                                                (which == 3) ? Color.YELLOW :
                                                        defaultColor; // 기본값 적용
                        ColorBtn.setBackgroundColor(selectedColor);
                    });
            builder.show();
        });

        // 완료 버튼 클릭 시 데이터 저장 후 MainActivity로 전달
        end.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();
            selectedDate = year + "-" + month + "-" + day;

            inputText = Category.getText().toString();

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            ScheduleItem scheduleItem = new ScheduleItem(selectedDate, inputText, selectedColor);
            database.child("scheduleList").push().setValue(scheduleItem)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "데이터 저장 성공!"))
                    .addOnFailureListener(e -> Log.e("Firebase", "데이터 저장 실패", e));

            //scheduleList.add(new ScheduleItem(selectedDate, inputText, selectedColor));
            Intent intent = new Intent(NextActivity.this, MainActivity.class);
            intent.putExtra("scheduleList", scheduleList);
            startActivity(intent);
        });

    };
}


