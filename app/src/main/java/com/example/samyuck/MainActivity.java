package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private LinearLayout categorySection;
    private DatabaseReference database;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private FirebaseAuth mAuth;
    private TextView yearMonthText, username;
    private int currentYear;
    private int currentMonth;
    private String selectedDate = "";
    private String targetUserId;
    private boolean isOwnSchedule = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String currentUserId = currentUser.getUid();
        targetUserId = getIntent().getStringExtra("targetUserId");
        isOwnSchedule = (targetUserId == null || targetUserId.equals(currentUserId));
        String userIdToUse = isOwnSchedule ? currentUserId : targetUserId;
        if (targetUserId == null) targetUserId = currentUserId;

        username = findViewById(R.id.username);

        // 이름 설정 로직
        if (isOwnSchedule) {
            // 내 피드인 경우, 현재 유저 이름 가져오기
            database = FirebaseDatabase.getInstance().getReference();
            database.child("UserAccount").child(currentUserId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String myName = snapshot.getValue(String.class);
                    username.setText(myName != null ? myName : "내 피드");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    username.setText("내 피드");
                }
            });
        } else {
            // 타인 피드
            String name = getIntent().getStringExtra("targetName");
            username.setText(name != null ? name : "친구 피드");
        }


        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        yearMonthText = findViewById(R.id.yearMonthText);
        updateYearMonthText();
        setupCalendar();

        categorySection = findViewById(R.id.categorySection);
        database = FirebaseDatabase.getInstance().getReference();

        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CategoryManageActivity.class);
            startActivity(intent);
        });

        Button monthButton = findViewById(R.id.monthButton);
        monthButton.setOnClickListener(v -> showMonthPickerDialog());

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

        loadCategoriesForDate(selectedDate, userIdToUse);

        LinearLayout feedNav = findViewById(R.id.feedNav);
        LinearLayout exploreNav = findViewById(R.id.exploreNav);
        LinearLayout friendNav = findViewById(R.id.friendNav);

        feedNav.setOnClickListener(v -> {
            // 내 피드일 때만 새로고침 동작 등 추가 가능
        });

        exploreNav.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExploreActivity.class);
            startActivity(intent);
        });

        friendNav.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FriendActivity.class);
            startActivity(intent);
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        if (!isOwnSchedule) {
            logoutButton.setVisibility(View.GONE);
        } else {
            logoutButton.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadCategoriesForDate(String date, String userId) {
        DatabaseReference userRef = database.child("users").child(userId);
        userRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                categorySection.removeAllViews();
                for (DataSnapshot category : categorySnapshot.getChildren()) {
                    String categoryName = category.child("category").getValue(String.class);
                    if (categoryName == null) categoryName = category.getKey();
                    int color = category.child("color").getValue(Integer.class) != null ? category.child("color").getValue(Integer.class) : Color.BLACK;

                    DatabaseReference detailRef = userRef.child("scheduleList").child(date).child(categoryName);
                    String finalCategoryName = categoryName;
                    detailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot detailSnapshot) {
                            String detail = detailSnapshot.child("detail").getValue(String.class);
                            ScheduleItem item = new ScheduleItem(date, finalCategoryName, color, detail != null ? detail : "");
                            addCategoryView(item);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
                }).show();
    }

    private void updateCalendar() {
        updateYearMonthText();
        calendarAdapter.setCalendarDays(currentYear, currentMonth);
    }

    private void setupCalendar() {
        calendarRecyclerView = findViewById(R.id.calendarGrid);
        calendarAdapter = new CalendarAdapter();
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setNestedScrollingEnabled(false);
        calendarAdapter.setCalendarDays(currentYear, currentMonth);
        calendarAdapter.setOnDateClickListener(date -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(date);
            loadCategoriesForDate(selectedDate, targetUserId);
        });
    }

    private void addCategoryView(ScheduleItem item) {
        LinearLayout categoryLayout = new LinearLayout(this);
        categoryLayout.setOrientation(LinearLayout.HORIZONTAL);
        categoryLayout.setGravity(Gravity.CENTER_VERTICAL);
        categoryLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        categoryLayout.setBackgroundResource(R.drawable.category_background);

        TextView categoryText = new TextView(this);
        categoryText.setText(item.getCategory());
        categoryText.setTextSize(16);
        categoryText.setTextColor(item.getColor());
        categoryText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ImageView addIcon = new ImageView(this);
        addIcon.setImageResource(R.drawable.ic_add);
        addIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
        addIcon.setVisibility(isOwnSchedule ? View.VISIBLE : View.GONE);

        TextView detailText = new TextView(this);
        detailText.setText(item.getDetail());
        detailText.setVisibility(item.getDetail().isEmpty() ? View.GONE : View.VISIBLE);

        EditText inputField = new EditText(this);
        inputField.setHint("세부 내용 입력");
        inputField.setVisibility(View.GONE);

        Button saveButton = new Button(this);
        saveButton.setText("저장");
        saveButton.setVisibility(View.GONE);

        addIcon.setOnClickListener(v -> {
            inputField.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
        });

        saveButton.setOnClickListener(v -> {
            String detailInput = inputField.getText().toString().trim();
            if (!detailInput.isEmpty()) {
                DatabaseReference detailRef = database.child("users").child(targetUserId)
                        .child("scheduleList").child(item.getDate()).child(item.getCategory()).child("detail");

                detailRef.setValue(detailInput).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "저장됨: " + detailInput, Toast.LENGTH_SHORT).show();
                    inputField.setText("");
                    inputField.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);
                    detailText.setText(detailInput);
                    detailText.setVisibility(View.VISIBLE);
                    loadCategoriesForDate(item.getDate(), targetUserId);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

        categoryLayout.addView(categoryText);
        if (isOwnSchedule) categoryLayout.addView(addIcon);

        LinearLayout wrapperLayout = new LinearLayout(this);
        wrapperLayout.setOrientation(LinearLayout.VERTICAL);
        wrapperLayout.setPadding(0, 0, 0, dpToPx(8));
        wrapperLayout.addView(categoryLayout);
        wrapperLayout.addView(detailText);
        wrapperLayout.addView(inputField);
        wrapperLayout.addView(saveButton);

        categorySection.addView(wrapperLayout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}