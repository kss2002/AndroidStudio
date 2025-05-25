package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CategoryManageActivity extends AppCompatActivity {
    private LinearLayout categoryManageSection;
    private DatabaseReference database;
    String selectedCategory;

    private FirebaseAuth mAuth;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manage);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        categoryManageSection = findViewById(R.id.categoryManageSection);
        database = FirebaseDatabase.getInstance().getReference();

        loadCategories();

        ImageButton addButton = findViewById(R.id.addCategoryButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
        });
    }
    private void loadCategories() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = database.child("users").child(userId).child("scheduleList");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryManageSection.removeAllViews();
                Log.d("Firebase", "데이터 스냅샷: " + snapshot.getValue());

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        // 필드명 대문자 "Category"로 수정
                        String date = dataSnapshot.child("date").getValue(String.class);
                        String category = dataSnapshot.child("Category").getValue(String.class);
                        int color = 0;
                        if (dataSnapshot.child("color").getValue() != null) {
                            color = dataSnapshot.child("color").getValue(Integer.class);
                        }
                        ScheduleItem item = new ScheduleItem(date, category, color);
                        Log.d("Firebase", "카테고리 로드: " + category);
                        addCategoryView(item);
                    } catch (Exception e) {
                        Log.e("Firebase", "데이터 변환 오류", e);
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "데이터 로드 실패", error.toException());
            }
        });
    }
    private void addCategoryView(ScheduleItem item) {
        // 카테고리 레이아웃 생성
        LinearLayout categoryLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        categoryManageSection.setPadding(0, dpToPx(80), 0, 0); // 부모 레이아웃 상단 패딩 추가
        layoutParams.setMargins(0, 12, 0, dpToPx(8));
        categoryLayout.setLayoutParams(layoutParams);
        categoryLayout.setOrientation(LinearLayout.HORIZONTAL);
        categoryLayout.setGravity(Gravity.CENTER_VERTICAL);
        categoryLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        categoryLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));


        // 카테고리 텍스트뷰
        TextView categoryText = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        textParams.setMarginStart(dpToPx(8));
        categoryText.setLayoutParams(textParams);
        categoryText.setText(item.getCategory());
        categoryText.setTextSize(16);
        categoryText.setTextColor(item.getColor());

        // 뷰들을 레이아웃에 추가
        categoryLayout.addView(categoryText);

        // 카테고리 섹션에 추가
        categoryManageSection.addView(categoryLayout);

        categoryText.setOnClickListener(view -> {
            Intent intent = new Intent(CategoryManageActivity.this, CategoryUpdateActivity.class);
            intent.putExtra("selectedDate", item.getDate());
            intent.putExtra("selectedColor", item.getColor());
            intent.putExtra("selectedCategory", item.getCategory());
            startActivity(intent);
        });



    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }



}
