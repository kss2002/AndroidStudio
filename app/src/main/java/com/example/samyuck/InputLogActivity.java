package com.example.samyuck;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class InputLogActivity extends AppCompatActivity {

    private EditText etEmail, etPwd;
    private Button btnLogin;
    private ImageButton backButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputlog);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("UserAccount");

        etEmail = findViewById(R.id.emailInput);
        etPwd = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.loginButton);
        backButton = findViewById(R.id.backButton);

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // 뒤로가기 버튼 클릭
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // 시스템 뒤로가기 버튼 처리
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(InputLogActivity.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();

                        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserAccount account = snapshot.getValue(UserAccount.class);
                                String name = account != null ? account.getName() : "사용자";

                                Toast.makeText(InputLogActivity.this, name + "님 환영합니다!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(InputLogActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(InputLogActivity.this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(InputLogActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

