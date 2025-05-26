package com.example.samyuck;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailInputActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private EditText detailEditText;
    private Button saveButton;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_input);

        categorySpinner = findViewById(R.id.spinner_category);
        detailEditText = findViewById(R.id.editText_detail);
        saveButton = findViewById(R.id.button_save);

        selectedDate = getIntent().getStringExtra("selectedDate");

        // 예시 카테고리 (Firebase에서 불러오는 로직은 이후 추가 가능)
        String[] categories = {"운동", "공부", "식사"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> {
            String category = categorySpinner.getSelectedItem().toString();
            String detailText = detailEditText.getText().toString();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("users").child(userId)
                    .child("details").child(selectedDate).child(category)
                    .push().setValue(detailText)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "세부 내용 저장 성공");
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "세부 내용 저장 실패", e);
                    });
        });
    }
}
