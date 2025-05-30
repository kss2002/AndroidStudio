package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View;

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
    Button ColorUpdateBtn;
    EditText CategoryUpdate;
    public int selectedColorUpdate = Color.BLACK; // 기본값 (검정색)
    DatePicker datePicker;
    String selectedDateUpdate;
    String inputTextUpdate;
    private String scheduleItemKey;
    private FirebaseAuth mAuth;
    private ImageButton backButton;

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
        backButton = findViewById(R.id.backButton);
        scheduleItemKey = null;
        
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String userId = mAuth.getCurrentUser().getUid();
        int selectedColor = getIntent().getIntExtra("selectedColor", Color.BLACK);
        String selectedCategory = getIntent().getStringExtra("selectedCategory");

        // DatePicker 초기화
        datePicker.setVisibility(View.GONE); // DatePicker 숨기기

        String[] colors = {"빨강", "초록", "파랑", "노랑"};
        int defaultColor = Color.BLACK;
        ColorUpdateBtn.setBackgroundColor(selectedColor);
        CategoryUpdate.setHint(selectedCategory);

        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> {
            finish();
        });

        // 색상 선택 버튼
        ColorUpdateBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("색을 선택하세요")
                    .setItems(colors, (dialog, which) -> {
                        selectedColorUpdate = (which == 0) ? Color.RED :
                                (which == 1) ? Color.GREEN :
                                        (which == 2) ? Color.BLUE :
                                                (which == 3) ? Color.YELLOW :
                                                        defaultColor;

                        ColorUpdateBtn.setBackgroundColor(selectedColorUpdate);
                    });
            builder.show();
        });

        // 카테고리 데이터 가져오기
        database.child("users").child(userId).child("categories")
                .orderByChild("category").equalTo(selectedCategory)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            scheduleItemKey = snapshot.getKey();
                            Log.d("Firebase", "찾은 키: " + scheduleItemKey);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "데이터 가져오기 실패", databaseError.toException());
                    }
                });

        // 완료 버튼 클릭 시 데이터 저장
        findViewById(R.id.UpdateEnd).setOnClickListener(v -> {
            inputTextUpdate = CategoryUpdate.getText().toString();
            if (inputTextUpdate.isEmpty()) {
                CategoryUpdate.setError("카테고리명을 입력하세요");
                return;
            }

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("category", inputTextUpdate);
            updatedData.put("color", selectedColorUpdate);

            if (scheduleItemKey != null) {
                database.child("users").child(userId).child("categories").child(scheduleItemKey)
                        .updateChildren(updatedData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firebase", "카테고리 수정 성공!");
                            Intent intent = new Intent(CategoryUpdateActivity.this, CategoryManageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "카테고리 수정 실패", e);
                        });
            }
        });
    }
}