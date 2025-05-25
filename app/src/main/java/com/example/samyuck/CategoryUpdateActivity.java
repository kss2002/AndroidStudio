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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CategoryUpdateActivity extends AppCompatActivity {
    Button ColorUpdateBtn, UpdateEnd,delete;
    EditText CategoryUpdate;
    public int selectedColorUpdate = Color.BLACK; // 기본값 (검정색)
    DatePicker datePicker;
    String selectedDateUpdate;
    String inputTextUpdate;
    private String scheduleItemKey;
    private FirebaseAuth mAuth;

    public class ScheduleItem implements Serializable {
        public String date;
        public String Category;
        public int color; // 색상 값 (예: #FF0000)

        public ScheduleItem(String date, String Category, int color) {
            this.date = date;
            this.Category = Category;
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

    ArrayList<CategoryUpdateActivity.ScheduleItem> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_update);
        mAuth = FirebaseAuth.getInstance();
        datePicker = findViewById(R.id.datePicker);
        ColorUpdateBtn = findViewById(R.id.ColorUpdateBtn);
        CategoryUpdate = findViewById(R.id.CategoryUpdate);
        UpdateEnd = findViewById(R.id.UpdateEnd);
        delete = findViewById(R.id.Delete);
        scheduleItemKey=null;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String userId = mAuth.getCurrentUser().getUid();
        int selectedColor = getIntent().getIntExtra("selectedColor",Color.BLACK);
        String selectedCategory = getIntent().getStringExtra("selectedCategory");

        int selectedYear = datePicker.getYear();
        int selectedMonth = datePicker.getMonth();
        int selectedDay = datePicker.getDayOfMonth();

        String[] colors = {"빨강", "초록", "파랑", "노랑"};
        int defaultColor = Color.BLACK; // 기본값을 검은색으로 설정
        ColorUpdateBtn.setBackgroundColor(selectedColor);
        CategoryUpdate.setHint(selectedCategory);

        ColorUpdateBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("색을 선택하세요")
                    .setItems(colors, (dialog, which) -> {
                        selectedColorUpdate = (which == 0) ? Color.RED :
                                (which == 1) ? Color.GREEN :
                                        (which == 2) ? Color.BLUE :
                                                (which == 3) ? Color.YELLOW :
                                                        defaultColor;

                        ColorUpdateBtn.setBackgroundColor(selectedColorUpdate); // 버튼 색상 변경
                    });
            builder.show();
        });

        database.child("users").child(userId).child("scheduleList")
                .orderByChild("Category").equalTo(selectedCategory) // 특정 필드 기준으로 조회

                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            scheduleItemKey = snapshot.getKey(); // 해당 항목의 키 가져오기
                            Log.d("Firebase", "찾은 키: " + scheduleItemKey);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "데이터 가져오기 실패", databaseError.toException());
                    }
                });


        // 완료 버튼 클릭 시 데이터 저장 후 MainActivity로 전달
        UpdateEnd.setOnClickListener(v -> {

            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();
            selectedDateUpdate = year + "-" + month + "-" + day;

            inputTextUpdate = CategoryUpdate.getText().toString();

            // Firebase에 데이터 저장

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("date",selectedDateUpdate ); // 변경할 필드
            updatedData.put("Category", inputTextUpdate); // 변경할 필드
            updatedData.put("color", selectedColorUpdate); // 변경할 필드
            updatedData.put("text", inputTextUpdate); // 변경할 필드


            database.child("users").child(userId).child("scheduleList").child(scheduleItemKey)
                    .updateChildren(updatedData)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "데이터 수정 성공!"))
                    .addOnFailureListener(e -> Log.e("Firebase", "데이터 수정 실패", e));

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        delete.setOnClickListener(view -> {
            database.child("users").child(userId).child("scheduleList").child(scheduleItemKey)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "데이터 삭제 성공"))
                    .addOnFailureListener(e -> Log.e("Firebase", "데이터 삭제 실패", e));

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    };
}