package com.example.samyuck;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etName, etPwd;
    private CheckBox ageCheckbox, termsCheckbox;
    private Button btnSignup;
    private ImageButton backButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("UserAccount");

        etEmail = findViewById(R.id.emailInput);
        etName = findViewById(R.id.nameInput);
        etPwd = findViewById(R.id.passwordInput);
        ageCheckbox = findViewById(R.id.ageCheckbox);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        btnSignup = findViewById(R.id.signupButton);
        backButton = findViewById(R.id.backButton);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        // ✅ 뒤로가기 버튼 클릭 리스너
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 종료하고 이전 화면으로 돌아가기
            }
        });
    }

    // ✅ 시스템 뒤로가기 버튼 처리
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void createUser() {
        String email = etEmail.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "이메일, 이름, 비밀번호를 모두 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ageCheckbox.isChecked()) {
            Toast.makeText(this, "만 14세 이상 동의가 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser != null) {
                                UserAccount account = new UserAccount();
                                account.setIdToken(firebaseUser.getUid());
                                account.setEmailId(firebaseUser.getEmail());
                                account.setName(name);
                                account.setPassword(pwd);

                                mDatabase.child(firebaseUser.getUid()).setValue(account);

                                Toast.makeText(SignUpActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}