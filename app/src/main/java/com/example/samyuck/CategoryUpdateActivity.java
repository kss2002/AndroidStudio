package com.example.samyuck;

import android.content.Intent;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;

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
    EditText CategoryUpdate;
    public int selectedColorUpdate; // 기본값 (검정색)
    private View colorUpdateCircle;
    private ImageView colorDropdown;
    private Spinner visibilitySpinner;
    private ImageView visibilityIcon;
    private String selectedVisibilityUpdate;
    String inputTextUpdate;
    private String scheduleItemKey;
    private FirebaseAuth mAuth;
    private ImageButton backButton;

    public class CategoryItem implements Serializable {
        public String Visibility;
        public String Category;
        public int color; // 색상 값 (예: #FF0000)

        public CategoryItem(String Visibility, String Category, int color) {
            this.Visibility = Visibility;
            this.Category = Category;
            this.color = color;
        }

        // Getter & Setter
        public String getVisibility() {
            return Visibility;
        }

        public String getText() {
            return Category;
        }

        public int getColor() {
            return color;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_update);
        mAuth = FirebaseAuth.getInstance();
        visibilitySpinner = findViewById(R.id.visibilitySpinner);
        visibilityIcon = findViewById(R.id.visibilityIcon);
        colorUpdateCircle = findViewById(R.id.colorUpdateCircle);
        colorDropdown = findViewById(R.id.colorDropdown);
        CategoryUpdate = findViewById(R.id.CategoryUpdate);
        backButton = findViewById(R.id.backButton);
        scheduleItemKey = null;
        
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String userId = mAuth.getCurrentUser().getUid();
        String selectedCategory = getIntent().getStringExtra("selectedCategory");
        String selectedVisibility = getIntent().getStringExtra("selectedVisibility");
        String selectedColor = getIntent().getStringExtra("selectedColor");
        int selectedColorCircle = Color.parseColor(selectedColor);

        CategoryUpdate.setText(selectedCategory);
        colorUpdateCircle.setBackgroundColor(selectedColorCircle);
        selectedColorUpdate = selectedColorCircle;


        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> {
            finish();
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.visibility_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibilitySpinner.setAdapter(adapter);
        if (selectedVisibility != null) {
            int position = adapter.getPosition(selectedVisibility); // 문자열을 인덱스로 변환
            visibilitySpinner.setSelection(position); // 선택된 위치 적용
        }


        // Spinner 선택에 따라 아이콘 변경
        visibilitySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedVisibility = parent.getItemAtPosition(position).toString();
                if (selectedVisibility.equals("나만 보기")) {
                    visibilityIcon.setImageResource(R.drawable.ic_lock);
                } else {
                    visibilityIcon.setImageResource(R.drawable.ic_public);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        View.OnClickListener colorSelectListener = v -> {
            final String[] colorsUpdate = {"검정", "빨강", "초록", "파랑", "노랑"};
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
                    .setItems(colorsUpdate, (dialog, which) -> {
                        selectedColorUpdate = colorValues[which];
                        colorUpdateCircle.setBackgroundResource(circleDrawables[which]);
                    });
            builder.show();
        };
        colorUpdateCircle.setOnClickListener(colorSelectListener);
        colorDropdown.setOnClickListener(colorSelectListener);

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
            selectedVisibilityUpdate = visibilitySpinner.getSelectedItem().toString();
            if (inputTextUpdate.isEmpty()) {
                CategoryUpdate.setError("카테고리명을 입력하세요");
                return;
            }

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("category", inputTextUpdate);
            updatedData.put("visibility", selectedVisibilityUpdate);
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