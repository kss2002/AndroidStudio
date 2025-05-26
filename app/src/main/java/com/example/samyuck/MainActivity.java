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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private LinearLayout categorySection;
    private DatabaseReference database;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private FirebaseAuth mAuth;
    private TextView userNameText;
    private TextView yearMonthText;  // 년월 표시를 위한 TextView
    private int currentYear;
    private int currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Auth 초기화
        mAuth = FirebaseAuth.getInstance();

        // 로그인 상태 확인
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 현재 날짜 가져오기
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        // 년월 텍스트뷰 초기화
        yearMonthText = findViewById(R.id.yearMonthText);
        updateYearMonthText();

        // 캘린더 초기화
        setupCalendar();

        // 카테고리 섹션 초기화
        categorySection = findViewById(R.id.categorySection);
        database = FirebaseDatabase.getInstance().getReference();

        // Firebase에서 카테고리 데이터 불러오기
        loadCategories();

        // 추가 버튼 설정
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CategoryManageActivity.class);
            startActivity(intent);
        });

        // 월 선택 버튼 설정
        Button monthButton = findViewById(R.id.monthButton);
        monthButton.setOnClickListener(v -> showMonthPickerDialog());

        // 이전/다음 월 버튼 설정
        ImageButton prevButton = findViewById(R.id.prevButton);
        ImageButton nextButton = findViewById(R.id.nextButton);

        prevButton.setOnClickListener(v -> {
            if (currentMonth == 0) {
                currentYear--;
                currentMonth = 11;
            } else {
                currentMonth--;
            }
            updateCalendar();
        });

        nextButton.setOnClickListener(v -> {
            if (currentMonth == 11) {
                currentYear++;
                currentMonth = 0;
            } else {
                currentMonth++;
            }
            updateCalendar();
        });
    }

    private void loadCategories() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = database.child("users").child(userId).child("scheduleList");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categorySection.removeAllViews();
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

    private void updateYearMonthText() {
        yearMonthText.setText(String.format("%d년 %d월", currentYear, currentMonth + 1));
    }

    private void showMonthPickerDialog() {
        String[] months = {"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};
        new AlertDialog.Builder(this)
                .setTitle("월 선택")
                .setItems(months, (dialog, which) -> {
                    currentMonth = which;
                    updateCalendar();
                })
                .show();
    }

    private void updateCalendar() {
        updateYearMonthText();
        calendarAdapter.setCalendarDays(currentYear, currentMonth);
    }

    private void setupCalendar() {
        calendarRecyclerView = findViewById(R.id.calendarGrid);
        calendarAdapter = new CalendarAdapter();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setNestedScrollingEnabled(false);

        updateCalendar();
    }

    private void addCategoryView(ScheduleItem item) {
        // 카테고리 레이아웃 생성
        LinearLayout categoryLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, dpToPx(8));
        categoryLayout.setLayoutParams(layoutParams);
        categoryLayout.setOrientation(LinearLayout.HORIZONTAL);
        categoryLayout.setGravity(Gravity.CENTER_VERTICAL);
        categoryLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        categoryLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        // Lock 아이콘
        ImageView lockIcon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
        lockIcon.setLayoutParams(iconParams);
        lockIcon.setImageResource(R.drawable.ic_lock);

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

        // Add 아이콘
        ImageView addIcon = new ImageView(this);
        addIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
        addIcon.setImageResource(R.drawable.ic_add);

        // 클릭 이벤트
        addIcon.setOnClickListener(v -> {
            String categoryName = item.getCategory();
            if (categoryName != null && !categoryName.isEmpty()) {
                Toast.makeText(MainActivity.this, categoryName + " 추가 버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });

        // 뷰들을 레이아웃에 추가
        categoryLayout.addView(lockIcon);
        categoryLayout.addView(categoryText);
        categoryLayout.addView(addIcon);

        // 카테고리 섹션에 추가
        categorySection.addView(categoryLayout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}