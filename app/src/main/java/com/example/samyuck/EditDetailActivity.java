package com.example.samyuck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditDetailActivity extends AppCompatActivity {
    private EditText editDetail;
    private String userId, date, category, detailId, originalDetail;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_detail);

        editDetail = findViewById(R.id.editDetail);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete); // 삭제 버튼 추가

        database = FirebaseDatabase.getInstance().getReference();

        // 인텐트로 전달받은 데이터
        userId = getIntent().getStringExtra("userId");
        date = getIntent().getStringExtra("date");
        category = getIntent().getStringExtra("category");
        detailId = getIntent().getStringExtra("detailId");
        originalDetail = getIntent().getStringExtra("detail");

        editDetail.setText(originalDetail);

        // 수정 저장
        btnSave.setOnClickListener(v -> {
            String newDetail = editDetail.getText().toString().trim();
            if (!newDetail.isEmpty()) {
                DatabaseReference detailRef = database.child("users").child(userId)
                        .child("scheduleList").child(date).child(category).child(detailId).child("detail");
                detailRef.setValue(newDetail)
                        .addOnSuccessListener(aVoid -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updated", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 삭제 기능
        btnDelete.setOnClickListener(v -> {
                        DatabaseReference detailRef = database.child("users").child(userId)
                                .child("scheduleList").child(date).child(category).child(detailId);
                        detailRef.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("deleted", true);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });

        };
    }


