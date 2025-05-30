package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity{

    private EditText categoryInput;
    private Spinner visibilitySpinner;
    private ImageView visibilityIcon;
    private View colorCircle;
    private ImageView colorDropdown;
    private TextView completeButton;
    private ImageButton backButton;
    private int selectedColor = Color.BLACK;
    private String selectedVisibility = "나만 보기";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        mAuth = FirebaseAuth.getInstance();

        categoryInput = findViewById(R.id.categoryInput);
        visibilitySpinner = findViewById(R.id.visibilitySpinner);
        visibilityIcon = findViewById(R.id.visibilityIcon);
        colorCircle = findViewById(R.id.colorCircle);
        colorDropdown = findViewById(R.id.colorDropdown);
        completeButton = findViewById(R.id.completeButton);
        backButton = findViewById(R.id.backButton);

        // Spinner 어댑터 설정 (혹시 xml에서 entries가 안 먹히면)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.visibility_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibilitySpinner.setAdapter(adapter);


        // Spinner 선택에 따라 아이콘 변경
        visibilitySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedVisibility = (String) parent.getItemAtPosition(position);
                if (selectedVisibility.equals("나만 보기")) {
                    visibilityIcon.setImageResource(R.drawable.ic_lock);
                } else {
                    visibilityIcon.setImageResource(R.drawable.ic_public);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 색상 선택 다이얼로그
        View.OnClickListener colorSelectListener = v -> {
            final String[] colors = {"검정", "빨강", "초록", "파랑", "노랑"};
            final int[] colorValues = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
            final int[] circleDrawables = {
                R.drawable.circle_black,
                R.drawable.circle_red,
                R.drawable.circle_green,
                R.drawable.circle_blue,
                R.drawable.circle_yellow
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("색을 선택하세요")
                    .setItems(colors, (dialog, which) -> {
                        selectedColor = colorValues[which];
                        colorCircle.setBackgroundResource(circleDrawables[which]);
                    });
            builder.show();
        };
        colorCircle.setOnClickListener(colorSelectListener);
        colorDropdown.setOnClickListener(colorSelectListener);

        // 완료 버튼 클릭 시 데이터 저장
        completeButton.setOnClickListener(v -> {
            String userId = mAuth.getCurrentUser().getUid();
            String category = categoryInput.getText().toString().trim();
            if (category.isEmpty()) {
                categoryInput.setError("카테고리명을 입력하세요");
                return;
            }
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            // 카테고리 객체에 공개설정도 포함
            CategoryItem item = new CategoryItem(category, selectedColor, selectedVisibility);
            database.child("users").child(userId).child("categories").push()
                    .setValue(item)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "카테고리 저장 성공!");
                        Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Log.e("Firebase", "카테고리 저장 실패", e));
        });

        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> finish());
    }

    // 카테고리 객체(공개설정 포함)
    public static class CategoryItem implements Serializable {
        public String category;
        public int color;
        public String visibility;
        public CategoryItem() {}
        public CategoryItem(String category, int color, String visibility) {
            this.category = category;
            this.color = color;
            this.visibility = visibility;
        }
    }
}


