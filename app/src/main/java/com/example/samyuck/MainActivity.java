package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
    private TextView yearMonthText,username;
    private int currentYear;
    private int currentMonth;
    private String selectedDate = "";

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
        username=findViewById(R.id.username);
        String name = getIntent().getStringExtra("name");
        username.setText(name);

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
        loadCategoriesForDate(selectedDate);
    }

    private void loadCategoriesForDate(String date) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = database.child("users").child(userId);

        userRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                categorySection.removeAllViews();
                for (DataSnapshot category : categorySnapshot.getChildren()) {
                    String categoryName = category.child("category").getValue(String.class);
                    if (categoryName == null) {
                        categoryName = category.getKey(); // fallback
                    }
                    int color = category.child("color").getValue(Integer.class) != null ?
                            category.child("color").getValue(Integer.class) : Color.BLACK;

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
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setNestedScrollingEnabled(false);

        calendarAdapter.setCalendarDays(currentYear, currentMonth);


        calendarAdapter.setOnDateClickListener(new CalendarAdapter.OnDateClickListener() {
            @Override
            public void onDateClick(Date date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(date);
                loadCategoriesForDate(selectedDate);
            }
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

        TextView detailText = new TextView(this);
        detailText.setText(item.getDetail());
        detailText.setVisibility(item.getDetail().isEmpty() ? View.GONE : View.VISIBLE);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("동적 체크박스");
        checkBox.setChecked(false); // 기본 체크 상태 설정
        checkBox.setVisibility(View.GONE);


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
                String userId = mAuth.getCurrentUser().getUid();
                DatabaseReference detailRef = database.child("users").child(userId)
                        .child("scheduleList").child(item.getDate()).child(item.getCategory()).child("detail");

                detailRef.setValue(detailInput).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "저장됨: " + detailInput, Toast.LENGTH_SHORT).show();
                    inputField.setText("");
                    inputField.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);
                    checkBox.setVisibility(View.VISIBLE);
                    detailText.setText(detailInput);
                    detailText.setVisibility(View.VISIBLE);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                loadCategoriesForDate(item.getDate()); // 저장 후 화면 다시 로딩
            }
        });

        categoryLayout.addView(categoryText);
        categoryLayout.addView(addIcon);

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
