package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
    private TextView emptyText;
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

        categoryManageSection = findViewById(R.id.categoryContainer);
        emptyText = findViewById(R.id.emptyText);
        database = FirebaseDatabase.getInstance().getReference();

        loadCategories();

        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
        });

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
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

                int count = 0;
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

                        String visibility = "나만 보기";
                        if (categorySnapshot.child("visibility").getValue() != null) {
                            visibility = categorySnapshot.child("visibility").getValue(String.class);
                        }

                        ScheduleItem item = new ScheduleItem(null, categoryName, color, visibility);
                        addCategoryView(item);
                        count++;
                    } catch (Exception e) {
                        Log.e("Firebase", "카테고리 데이터 변환 오류", e);
                    }
                }
                if (count == 0) {
                    emptyText.setVisibility(View.VISIBLE);
                } else {
                    emptyText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "카테고리 로드 실패", error.toException());
            }
        });
    }

    private void addCategoryView(ScheduleItem item) {
        // 카드형 카테고리 레이아웃 생성
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, dpToPx(8), 0, dpToPx(8));
        card.setLayoutParams(cardParams);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.category_card);
        card.setElevation(dpToPx(3));

        // 색상 원
        View colorDot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
        dotParams.setMarginEnd(dpToPx(16));
        colorDot.setLayoutParams(dotParams);
        if (item.getColor() == Color.RED) colorDot.setBackgroundResource(R.drawable.circle_red);
        else if (item.getColor() == Color.GREEN) colorDot.setBackgroundResource(R.drawable.circle_green);
        else if (item.getColor() == Color.BLUE) colorDot.setBackgroundResource(R.drawable.circle_blue);
        else if (item.getColor() == Color.YELLOW) colorDot.setBackgroundResource(R.drawable.circle_yellow);
        else colorDot.setBackgroundResource(R.drawable.circle_black);

        // 텍스트(카테고리명 + 공개설정)
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView categoryText = new TextView(this);
        categoryText.setText(item.getCategory());
        categoryText.setTextSize(18);
        categoryText.setTextColor(Color.BLACK);
        categoryText.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView visibilityText = new TextView(this);
        visibilityText.setTextSize(13);
        visibilityText.setTextColor(Color.parseColor("#888888"));
        visibilityText.setText("공개설정: " + item.visibility);

        textCol.addView(categoryText);
        textCol.addView(visibilityText);

        // 아이콘 영역
        LinearLayout iconCol = new LinearLayout(this);
        iconCol.setOrientation(LinearLayout.HORIZONTAL);
        iconCol.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);

        // 수정 아이콘
        ImageView editIcon = new ImageView(this);
        editIcon.setImageResource(R.drawable.edit);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
        iconParams.setMarginEnd(dpToPx(8));
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
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28)));
        deleteIcon.setOnClickListener(view -> {
            String userId = mAuth.getCurrentUser().getUid();
            database.child("users").child(userId).child("categories")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
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
                                    break;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "삭제 중 오류 발생", error.toException());
                        }
                    });
        });

        iconCol.addView(editIcon);
        iconCol.addView(deleteIcon);

        // 카드에 요소 추가
        card.addView(colorDot);
        card.addView(textCol);
        card.addView(iconCol);

        categoryManageSection.addView(card);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static class ScheduleItem {
        private String date;
        private String category;
        private int color;
        public String visibility;
        public ScheduleItem(String date, String category, int color) {
            this(date, category, color, "나만 보기");
        }
        public ScheduleItem(String date, String category, int color, String visibility) {
            this.date = date;
            this.category = category;
            this.color = color;
            this.visibility = visibility;
        }
        public String getCategory() { return category; }
        public int getColor() { return color; }
    }
}
