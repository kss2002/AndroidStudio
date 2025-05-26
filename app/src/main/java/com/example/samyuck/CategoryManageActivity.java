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
    private FirebaseAuth mAuth;

    @Override
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
        DatabaseReference userRef = database.child("users").child(userId).child("categories");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryManageSection.removeAllViews();
                Log.d("Firebase", "카테고리 스냅샷: " + snapshot.getValue());

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    try {
                        String categoryName = categorySnapshot.child("category").getValue(String.class);
                        if (categoryName == null) {
                            categoryName = categorySnapshot.getKey();  // fallback
                        }

                        int color = 0;
                        if (categorySnapshot.child("color").getValue() != null) {
                            color = categorySnapshot.child("color").getValue(Integer.class);
                        }

                        ScheduleItem item = new ScheduleItem(null, categoryName, color);
                        addCategoryView(item);

                    } catch (Exception e) {
                        Log.e("Firebase", "카테고리 데이터 변환 오류", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "카테고리 로드 실패", error.toException());
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

        // 수정 아이콘
        ImageView editIcon = new ImageView(this);
        editIcon.setImageResource(R.drawable.edit);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(24),
                dpToPx(24)
        );
        iconParams.setMarginEnd(dpToPx(12));
        editIcon.setLayoutParams(iconParams);
        editIcon.setOnClickListener(view -> {
            Intent intent = new Intent(CategoryManageActivity.this, CategoryUpdateActivity.class);
            intent.putExtra("selectedCategory", item.getCategory());
            intent.putExtra("selectedColor", item.getColor());
            startActivity(intent);
        });

        // 삭제 아이콘
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(R.drawable.delete);
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));

        deleteIcon.setOnClickListener(view -> {
            String userId = mAuth.getCurrentUser().getUid();
            database.child("users").child(userId).child("categories")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String name = child.child("category").getValue(String.class);
                                if (name != null && name.equals(item.getCategory())) {
                                    // 일치하는 카테고리 삭제
                                    child.getRef().removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(CategoryManageActivity.this, "카테고리 삭제 완료", Toast.LENGTH_SHORT).show();
                                                loadCategories();  // UI 갱신
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("Firebase", "삭제 실패", e);
                                                Toast.makeText(CategoryManageActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                                            });
                                    break;  // 일치 항목 찾았으니 더 이상 반복 안 함
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "삭제 중 오류 발생", error.toException());
                        }
                    });

        });

        categoryLayout.addView(categoryText);
        categoryLayout.addView(editIcon);
        categoryLayout.addView(deleteIcon);

        categoryManageSection.addView(categoryLayout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
